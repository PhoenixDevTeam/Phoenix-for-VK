package biz.dealnote.messenger.longpoll;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.model.longpoll.InputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsResetUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsSetUpdate;
import biz.dealnote.messenger.api.model.longpoll.OutputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOfflineUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOnlineUpdate;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.api.model.longpoll.WriteTextInDialogUpdate;
import biz.dealnote.messenger.longpoll.model.AbsRealtimeAction;
import biz.dealnote.messenger.longpoll.model.MessageFlagsReset;
import biz.dealnote.messenger.longpoll.model.MessageFlagsSet;
import biz.dealnote.messenger.longpoll.model.MessagesRead;
import biz.dealnote.messenger.longpoll.model.RefreshListeningRequest;
import biz.dealnote.messenger.longpoll.model.UserOffline;
import biz.dealnote.messenger.longpoll.model.UserOnline;
import biz.dealnote.messenger.longpoll.model.WriteText;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.realtime.Processors;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class LongpollService extends Service implements Longpoll.Callback {

    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_UNREGISTER = "unregister";

    public static final String WHAT_REALTIME_ACTIONS = "realtime_actions";
    public static final String EXTRA_REALTIME_ACTIONS = "realtime_actions_list";

    private static final String TAG = LongpollService.class.getSimpleName();

    private static final int INTERVAL = 60 * 1000;

    private SparseArray<Longpoll> mActiveLongpolls;
    private SparseArray<Set<Integer>> mRegisteredPeers;

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private Observable<Long> mShutdownObservable;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mRegisteredPeers = new SparseArray<>(2);
        this.mActiveLongpolls = new SparseArray<>(1);

        Logger.d(TAG, "Service created");

        mCompositeDisposable.add(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));

        mShutdownObservable = Observable.interval(INTERVAL, INTERVAL, TimeUnit.MILLISECONDS,
                Injection.provideMainThreadScheduler());
        restartShutdownDelay();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        Bundle extras = intent == null ? null : intent.getExtras();

        Logger.d(TAG, "onStartCommand, action: " + action + ", startId: " + startId);

        if (ACTION_REGISTER.equals(action) && extras != null) {
            Integer oldAid = extras.containsKey("unreg_aid") ? extras.getInt("unreg_aid") : null;
            Integer oldPeerId = extras.containsKey("unreg_pid") ? extras.getInt("unreg_pid") : null;

            register(extras.getInt(Extra.ACCOUNT_ID), extras.getInt(Extra.PEER_ID), oldAid, oldPeerId);
            restartShutdownDelay();
        }

        if (ACTION_UNREGISTER.equals(action) && extras != null) {
            unregister(extras.getInt(Extra.ACCOUNT_ID), extras.getInt(Extra.PEER_ID));
            restartShutdownDelay();
        }

        return hasRegisteredPeers() ? START_STICKY : START_NOT_STICKY;
    }

    private boolean hasRegisteredPeers() {
        for (int i = 0; i < mRegisteredPeers.size(); i++) {
            int accountId = mRegisteredPeers.keyAt(i);
            Set<Integer> peers = mRegisteredPeers.get(accountId);
            if (safeCountOf(peers) > 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Проверяем текущее состояние регистраций и активных Лонгполов
     * Если какой-то из лонгполов уже не нужен - останавливаем наблюдение
     *
     * @return true если служба больше не используется и может быть уничтожена
     */
    private boolean incpectCurrentState() {
        Logger.d(TAG, "incpectCurrentState, mRegisteredPeers: " + mRegisteredPeers);

        for (int i = 0; i < mActiveLongpolls.size(); i++) {
            int accountId = mActiveLongpolls.keyAt(i);
            Set<Integer> peers = mRegisteredPeers.get(accountId);

            boolean needListen = safeCountOf(peers) > 0;

            if (!needListen) {
                mRegisteredPeers.remove(accountId);
                mActiveLongpolls.get(accountId).shutdown();
            }
        }

        return mRegisteredPeers.size() == 0;
    }

    /**
     * Обновляем регистрацию слушателей
     * Сбрасываем все регистрации и отправляем сообщение
     * чтобы нужные слушатели заново подписались
     * Сделано для того, чтобы избежать зависших слушателей
     */
    private void updateRegistration() {
        mRegisteredPeers.clear();

        Intent intent = new Intent(WHAT_REALTIME_ACTIONS);
        intent.putParcelableArrayListExtra(EXTRA_REALTIME_ACTIONS,
                new ArrayList<>(Collections.singletonList(new RefreshListeningRequest())));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Logger.d(TAG, "updateRegistration");
    }

    private Disposable mShutdownDisposable;

    private void restartShutdownDelay() {
        if (nonNull(mShutdownDisposable) && !mShutdownDisposable.isDisposed()) {
            mShutdownDisposable.dispose();
        }

        mShutdownDisposable = mShutdownObservable.subscribe(ignore -> onShutdownExpired());

        Logger.d(TAG, "restartShutdownDelay");
    }

    private void onShutdownExpired() {
        boolean canStop = incpectCurrentState();
        Logger.d(TAG, "onShutdownExpired, canStop: " + canStop);

        if (canStop) {
            releaseAllLongpolls();
            stopSelf();
        } else {
            restartShutdownDelay();
            updateRegistration();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void onListenersChanges() {
        Logger.d(TAG, "onListenersChanges, mRegisteredPeers: " + mRegisteredPeers);

        for (int i = 0; i < mRegisteredPeers.size(); i++) {
            int accountId = mRegisteredPeers.keyAt(i);
            Set<Integer> value = mRegisteredPeers.get(accountId);

            boolean needLongpoll = !safeIsEmpty(value);
            if (needLongpoll) {
                prepareLongpollFor(accountId).connect();
            }
        }
    }

    private void register(int accountId, int peerId, Integer oldAid, Integer oldPeerId) {
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        Set<Integer> set = prepareRegisteredForAccount(accountId);
        set.add(peerId);

        if (oldAid != null && oldPeerId != null) {
            Set<Integer> forOldAccount = mRegisteredPeers.get(oldAid);
            if (forOldAccount != null) {
                forOldAccount.remove(oldPeerId);
            }
        }

        onListenersChanges();
    }

    private Set<Integer> prepareRegisteredForAccount(int accountId) {
        Set<Integer> set = mRegisteredPeers.get(accountId);
        if (set == null) {
            set = new HashSet<>(1);
            mRegisteredPeers.put(accountId, set);
        }

        return set;
    }

    /**
     * Отмена регистрации слушателя событий
     *
     * @param accountId идентификатор аккаунта
     * @param peerId    идентификатор диалога (если все диалоги - передавать 0)
     */
    private void unregister(int accountId, int peerId) {
        Set<Integer> peers = mRegisteredPeers.get(accountId);
        if (nonNull(peers)) {
            boolean removed = peers.remove(peerId);

            if (peers.isEmpty()) {
                mRegisteredPeers.remove(accountId);
            }

            if (removed) {
                onListenersChanges();
            }
        }
    }

    private void onAccountChange(int currentAccountId) {
        for (int i = 0; i < mActiveLongpolls.size(); i++) {
            int accountId = mActiveLongpolls.keyAt(i);

            Longpoll longpoll = mActiveLongpolls.get(accountId);

            if (accountId != currentAccountId) {
                longpoll.shutdown();
                Logger.d(TAG, "longpoll for " + accountId + " was stopped");
            } else {
                longpoll.connect();
                Logger.d(TAG, "longpoll for " + accountId + " was started");
            }
        }
    }

    @NonNull
    private Longpoll prepareLongpollFor(int aid) {
        Longpoll longpoll = mActiveLongpolls.get(aid);
        if (longpoll == null) {
            longpoll = new Longpoll(this, Injection.provideNetworkInterfaces(), aid, this);
            mActiveLongpolls.put(aid, longpoll);
        }

        return longpoll;
    }

    private static final Scheduler MONO_SCHEDULER = Schedulers.from(Executors.newFixedThreadPool(1));

    @Override
    public void onUpdates(int aid, VkApiLongpollUpdates updates) {
        Logger.d(TAG, "onUpdates, aid: " + aid + ", updates: " + updates);

        if (nonEmpty(updates.getAddMessageUpdates())) {
            Processors.realtimeMessages()
                    .process(aid, updates.getAddMessageUpdates());
        }

        mCompositeDisposable.add(new LongPollEventSaver().save(this, aid, updates)
                .subscribeOn(MONO_SCHEDULER)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(() -> onUpdatesSaved(aid, updates)));
    }

    private void onUpdatesSaved(int accountId, VkApiLongpollUpdates updates) {
        LongPollNotificationHelper.fireUpdates(this, accountId, updates);

        ArrayList<AbsRealtimeAction> actions = createActions(accountId, updates);

        if (nonEmpty(actions)) {
            Intent intent = new Intent(WHAT_REALTIME_ACTIONS);
            intent.putParcelableArrayListExtra(EXTRA_REALTIME_ACTIONS, actions);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private ArrayList<AbsRealtimeAction> createActions(int accountId, VkApiLongpollUpdates updates) {
        ArrayList<AbsRealtimeAction> actions = new ArrayList<>();

        if (nonEmpty(updates.getUserIsOnlineUpdates())) {
            for (UserIsOnlineUpdate update : updates.getUserIsOnlineUpdates()) {
                actions.add(new UserOnline(accountId, update.getUserId()));
            }
        }

        if (nonEmpty(updates.getUserIsOfflineUpdates())) {
            for (UserIsOfflineUpdate update : updates.getUserIsOfflineUpdates()) {
                actions.add(new UserOffline(accountId, update.getUserId(), update.getFlags() != 0));
            }
        }

        if (nonEmpty(updates.getWriteTextInDialogUpdates())) {
            for (WriteTextInDialogUpdate update : updates.getWriteTextInDialogUpdates()) {
                actions.add(new WriteText(accountId, update.getUserId(), Peer.fromUserId(update.getUserId())));
            }
        }

        if (nonEmpty(updates.getMessageFlagsSetUpdates())) {
            for (MessageFlagsSetUpdate update : updates.getMessageFlagsSetUpdates()) {
                actions.add(new MessageFlagsSet(accountId, update.getMessageId(), update.getPeerId(), update.getMask()));
            }
        }

        if (nonEmpty(updates.getMessageFlagsResetUpdates())) {
            for (MessageFlagsResetUpdate update : updates.getMessageFlagsResetUpdates()) {
                actions.add(new MessageFlagsReset(accountId, update.getMessageId(), update.getPeerId(), update.getMask()));
            }
        }

        if (nonEmpty(updates.getInputMessagesSetReadUpdates())) {
            for (InputMessagesSetReadUpdate update : updates.getInputMessagesSetReadUpdates()) {
                actions.add(new MessagesRead(accountId, update.getPeerId(), update.getLocalId(), false));
            }
        }

        if (nonEmpty(updates.getOutputMessagesSetReadUpdates())) {
            for (OutputMessagesSetReadUpdate update : updates.getOutputMessagesSetReadUpdates()) {
                actions.add(new MessagesRead(accountId, update.getPeerId(), update.getLocalId(), true));
            }
        }

        return actions;
    }

    private void releaseAllLongpolls() {
        for (int i = 0; i < mActiveLongpolls.size(); i++) {
            int accountId = mActiveLongpolls.keyAt(i);
            Longpoll longpoll = mActiveLongpolls.get(accountId);
            longpoll.shutdown();
        }

        mActiveLongpolls.clear();
    }

    @Override
    public void onDestroy() {
        if (nonNull(mShutdownDisposable) && !mShutdownDisposable.isDisposed()) {
            mShutdownDisposable.dispose();
        }

        mCompositeDisposable.dispose();
        releaseAllLongpolls();

        super.onDestroy();
        Logger.d(TAG, "Service destroyed");
    }
}
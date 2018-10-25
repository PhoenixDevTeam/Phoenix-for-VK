package biz.dealnote.messenger.longpoll;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.longpoll.InputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsResetUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsSetUpdate;
import biz.dealnote.messenger.api.model.longpoll.OutputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOfflineUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOnlineUpdate;
import biz.dealnote.messenger.api.model.longpoll.VkApiGroupLongpollUpdates;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.api.model.longpoll.WriteTextInDialogUpdate;
import biz.dealnote.messenger.longpoll.model.AbsRealtimeAction;
import biz.dealnote.messenger.longpoll.model.MessageFlagsReset;
import biz.dealnote.messenger.longpoll.model.MessageFlagsSet;
import biz.dealnote.messenger.longpoll.model.MessagesRead;
import biz.dealnote.messenger.longpoll.model.UserOffline;
import biz.dealnote.messenger.longpoll.model.UserOnline;
import biz.dealnote.messenger.longpoll.model.WriteText;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.realtime.IRealtimeMessagesProcessor;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class AndroidLongpollManager implements ILongpollManager, UserLongpoll.Callback, GroupLongpoll.Callback {

    private final Context app;
    private final SparseArray<LongpollEntry> map;
    private final INetworker networker;
    private final PublishProcessor<Integer> keepAlivePublisher;
    private final PublishProcessor<List<AbsRealtimeAction>> actionsPublisher;
    private final IRealtimeMessagesProcessor messagesProcessor;
    private final Object lock = new Object();

    private final static String TAG = AndroidLongpollManager.class.getSimpleName();

    private static final Scheduler MONO_SCHEDULER = Schedulers.from(Executors.newFixedThreadPool(1));

    AndroidLongpollManager(Context context, INetworker networker, IRealtimeMessagesProcessor messagesProcessor) {
        this.app = context.getApplicationContext();
        this.networker = networker;
        this.messagesProcessor = messagesProcessor;
        this.keepAlivePublisher = PublishProcessor.create();
        this.actionsPublisher = PublishProcessor.create();
        this.map = new SparseArray<>(1);
    }

    @Override
    public Flowable<List<AbsRealtimeAction>> observe() {
        return actionsPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<Integer> observeKeepAlive() {
        return keepAlivePublisher.onBackpressureBuffer();
    }

    private ILongpoll createLongpoll(int accountId) {
        //return accountId > 0 ? new UserLongpoll(networker, accountId, this) : new GroupLongpoll(networker, Math.abs(accountId), this);
        return new UserLongpoll(networker, accountId, this);
    }

    @Override
    public void forceDestroy(int accountId) {
        Logger.d(TAG, "forceDestroy, accountId: " + accountId);
        synchronized (lock) {
            LongpollEntry entry = map.get(accountId);
            if (nonNull(entry)) {
                entry.destroy();
            }
        }
    }

    @Override
    public void keepAlive(int accountId) {
        Logger.d(TAG, "keepAlive, accountId: " + accountId);
        synchronized (lock) {
            LongpollEntry entry = map.get(accountId);
            if (nonNull(entry)) {
                entry.deferDestroy();
            } else {
                entry = new LongpollEntry(createLongpoll(accountId), this);
                map.put(accountId, entry);
                entry.connect();
            }
        }
    }

    void notifyDestroy(LongpollEntry entry) {
        Logger.d(TAG, "destroyed, accountId: " + entry.getAccountId());
        synchronized (lock) {
            map.remove(entry.getAccountId());
        }
    }

    void notifyPreDestroy(LongpollEntry entry) {
        Logger.d(TAG, "pre-destroy, accountId: " + entry.getAccountId());
        keepAlivePublisher.onNext(entry.getAccountId());
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onUpdates(int accountId, @NonNull VkApiLongpollUpdates updates) {
        Logger.d(TAG, "updates, accountId: " + accountId);

        if (nonEmpty(updates.getAddMessageUpdates())) {
            messagesProcessor.process(accountId, updates.getAddMessageUpdates());
        }

        compositeDisposable.add(new LongPollEventSaver().save(app, accountId, updates)
                .subscribeOn(MONO_SCHEDULER)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(() -> onUpdatesSaved(accountId, updates), RxUtils.ignore()));
    }

    private void onUpdatesSaved(int accountId, VkApiLongpollUpdates updates) {
        LongPollNotificationHelper.fireUpdates(app, accountId, updates);

        List<AbsRealtimeAction> actions = createActions(accountId, updates);
        if (nonEmpty(actions)) {
            actionsPublisher.onNext(actions);
        }
    }

    private List<AbsRealtimeAction> createActions(int accountId, VkApiLongpollUpdates updates) {
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
                actions.add(new MessagesRead(accountId, update.getPeerId(), update.getLocalId(), false, update.getUnreadCount()));
            }
        }

        if (nonEmpty(updates.getOutputMessagesSetReadUpdates())) {
            for (OutputMessagesSetReadUpdate update : updates.getOutputMessagesSetReadUpdates()) {
                actions.add(new MessagesRead(accountId, update.getPeerId(), update.getLocalId(), true, update.getUnreadCount()));
            }
        }

        return actions;
    }

    @Override
    public void onUpdates(int groupId, @NonNull VkApiGroupLongpollUpdates updates) {

    }

    private static final class LongpollEntry {

        final ILongpoll longpoll;
        final SocketHandler handler;
        boolean released;
        final WeakReference<AndroidLongpollManager> managerReference;
        final int accountId;

        LongpollEntry(ILongpoll longpoll, AndroidLongpollManager manager) {
            this.longpoll = longpoll;
            this.accountId = longpoll.getAccountId();
            this.managerReference = new WeakReference<>(manager);
            this.handler = new SocketHandler(this);
        }

        void connect() {
            longpoll.connect();
            handler.restartPreDestroy();
        }

        void destroy() {
            handler.release();
            longpoll.shutdown();
            released = true;

            AndroidLongpollManager manager = managerReference.get();
            if (nonNull(manager)) {
                manager.notifyDestroy(this);
            }
        }

        void deferDestroy() {
            handler.restartPreDestroy();
        }

        int getAccountId() {
            return accountId;
        }

        void firePreDestroy() {
            AndroidLongpollManager manager = managerReference.get();
            if (nonNull(manager)) {
                manager.notifyPreDestroy(this);
            }
        }
    }

    private static final class SocketHandler extends android.os.Handler {

        final static int PRE_DESTROY = 2;
        final static int DESTROY = 3;

        final WeakReference<LongpollEntry> reference;

        SocketHandler(AndroidLongpollManager.LongpollEntry holder) {
            super(Looper.getMainLooper());
            this.reference = new WeakReference<>(holder);
        }

        void restartPreDestroy() {
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
            sendEmptyMessageDelayed(PRE_DESTROY, 30_000L);
        }

        void postDestroy() {
            sendEmptyMessageDelayed(DESTROY, 30_000L);
        }

        void release() {
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
        }

        @Override
        public void handleMessage(Message msg) {
            LongpollEntry holder = reference.get();
            if (holder != null && !holder.released) {
                switch (msg.what) {
                    case PRE_DESTROY:
                        postDestroy();
                        holder.firePreDestroy();
                        break;

                    case DESTROY:
                        holder.destroy();
                        break;
                }
            }
        }
    }
}
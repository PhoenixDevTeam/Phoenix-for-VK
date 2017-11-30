package biz.dealnote.messenger.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.RemoteInput;
import android.widget.Toast;

import java.util.Collection;
import java.util.concurrent.Executors;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.model.SentMsg;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SendService extends Service {

    public static final String TAG = SendService.class.getSimpleName();

    public static final String ACTION_ADD_MESSAGE = "SendService.ACTION_ADD_MESSAGE";

    private Scheduler mSenderScheduler;
    private IMessagesInteractor messagesInteractor;
    private Collection<Integer> registeredAccounts;

    @Override
    public void onCreate() {
        super.onCreate();
        this.messagesInteractor = InteractorFactory.createMessagesInteractor();
        this.mSenderScheduler = Schedulers.from(Executors.newFixedThreadPool(1));
        this.registeredAccounts = Settings.get().accounts().getRegistered();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "destroyed");
        this.mCompositeDisposable.dispose();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_ADD_MESSAGE.equals(action)) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);

            Bundle msg = RemoteInput.getResultsFromIntent(intent);
            if (msg != null) {
                CharSequence body = msg.getCharSequence(Extra.BODY);
                addMessage(accountId, peerId, body);
            }
        } else {
            send();
        }

        return START_NOT_STICKY;
    }

    public static Intent intentForAddMessage(Context context, int accountId, int peerId) {
        Intent intent = new Intent(context, SendService.class);
        intent.setAction(ACTION_ADD_MESSAGE);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        return intent;
    }

    private void addMessage(int accountId, int peerId, CharSequence body) {
        final IMessagesInteractor messagesInteractor = InteractorFactory.createMessagesInteractor();

        SaveMessageBuilder builder = new SaveMessageBuilder(accountId, peerId)
                .setBody(body.toString());

        messagesInteractor.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(message -> send(), Analytics::logUnexpectedError);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean mNowSending;

    /**
     * Отправить первое неотправленное сообщение
     */
    private void send() {
        if (this.mNowSending) {
            Logger.d(TAG, "Now sending, send aborted");
            return;
        }

        this.mNowSending = true;
        sendMessage(this.registeredAccounts);
    }

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @SuppressWarnings("unused")
    private void onMessageSent(SentMsg msg){
        this.mNowSending = false;

        NotificationHelper.tryCancelNotificationForPeer(this, msg.getAccountId(), msg.getPeerId());

        send();
    }

    private void onMessageSendError(Throwable t){
        Throwable cause = Utils.getCauseIfRuntime(t);

        this.mNowSending = false;

        if(cause instanceof NotFoundException){
            // no unsent messages
            stopSelf();
            return;
        }

        Toast.makeText(SendService.this, ErrorLocalizer.localizeThrowable(this, cause), Toast.LENGTH_LONG).show();
    }

    private void sendMessage(Collection<Integer> accountIds) {
        this.mNowSending = true;
        this.mCompositeDisposable.add(messagesInteractor.sendUnsentMessage(accountIds)
                .subscribeOn(mSenderScheduler)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onMessageSent, this::onMessageSendError));
    }
}
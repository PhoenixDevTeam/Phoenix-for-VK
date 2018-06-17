package biz.dealnote.messenger.service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.Executors;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.SentMsg;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SendService {

    private Scheduler senderScheduler;
    private IMessagesInteractor messagesInteractor;
    private Collection<Integer> registeredAccounts;
    private final Context app;

    SendService(Context context, ISettings.IAccountsSettings settings){
        this.app = context.getApplicationContext();
        this.registeredAccounts = settings.getRegistered();
        this.senderScheduler = Schedulers.from(Executors.newFixedThreadPool(1));

        compositeDisposable.add(settings.observeRegistered()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountsChanged, RxUtils.ignore()));
    }

    public void setMessagesInteractor(IMessagesInteractor messagesInteractor) {
        this.messagesInteractor = messagesInteractor;
    }

    private void onAccountsChanged(ISettings.IAccountsSettings settings){
        registeredAccounts = settings.getRegistered();
    }

    private final InternalHandler handler = new InternalHandler(Looper.getMainLooper(), this);

    private static final class InternalHandler extends Handler {

        final WeakReference<SendService> reference;

        InternalHandler(Looper looper, SendService service) {
            super(looper);
            this.reference = new WeakReference<>(service);
        }

        static final int SEND = 1;

        void runSend(){
            sendEmptyMessage(SEND);
        }

        @Override
        public void handleMessage(Message msg) {
            SendService service = reference.get();
            if(service != null){
                switch (msg.what){
                    case SEND:
                        service.send();
                        break;
                }
            }
        }
    }

    private boolean nowSending;

    public void runSendingQueue(){
        handler.runSend();
    }

    /**
     * Отправить первое неотправленное сообщение
     */
    @MainThread
    private void send() {
        if (nowSending) {
            return;
        }

        nowSending = true;
        sendMessage(registeredAccounts);
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private void onMessageSent(SentMsg msg){
        nowSending = false;
        NotificationHelper.tryCancelNotificationForPeer(app, msg.getAccountId(), msg.getPeerId());
        send();
    }

    private void onMessageSendError(Throwable t){
        Throwable cause = Utils.getCauseIfRuntime(t);
        nowSending = false;

        if(cause instanceof NotFoundException){
            // no unsent messages
            return;
        }

        Toast.makeText(app, ErrorLocalizer.localizeThrowable(app, cause), Toast.LENGTH_LONG).show();
    }

    private void sendMessage(Collection<Integer> accountIds) {
        if(messagesInteractor == null){
            throw new IllegalStateException("'messagesInteractor' was not initialized");
        }

        nowSending = true;
        compositeDisposable.add(messagesInteractor.sendUnsentMessage(accountIds)
                .subscribeOn(senderScheduler)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onMessageSent, this::onMessageSendError));
    }
}
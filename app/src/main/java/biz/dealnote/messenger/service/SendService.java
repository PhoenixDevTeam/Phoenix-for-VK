package biz.dealnote.messenger.service;

import android.content.Context;
import android.os.Message;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import android.widget.Toast;

import java.util.Collection;
import java.util.concurrent.Executors;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.domain.IMessagesRepository;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.SentMsg;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.WeakMainLooperHandler;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SendService {

    private Scheduler senderScheduler;
    private IMessagesRepository messagesInteractor;
    private Collection<Integer> registeredAccounts;
    private final Context app;

    SendService(Context context, ISettings.IAccountsSettings settings) {
        this.app = context.getApplicationContext();
        this.registeredAccounts = settings.getRegistered();
        this.senderScheduler = Schedulers.from(Executors.newFixedThreadPool(1));

        compositeDisposable.add(settings.observeRegistered()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountsChanged, RxUtils.ignore()));
    }

    public void setMessagesInteractor(IMessagesRepository messagesInteractor) {
        this.messagesInteractor = messagesInteractor;
    }

    private void onAccountsChanged(ISettings.IAccountsSettings settings) {
        registeredAccounts = settings.getRegistered();
    }

    private final InternalHandler handler = new InternalHandler(this);

    private static final class InternalHandler extends WeakMainLooperHandler<SendService> {

        static final int SEND = 1;

        InternalHandler(SendService service) {
            super(service);
        }

        void runSend() {
            sendEmptyMessage(SEND);
        }

        @Override
        public void handleMessage(@NonNull SendService service, @NonNull Message msg) {
            switch (msg.what) {
                case SEND:
                    service.send();
                    break;
            }
        }
    }

    private boolean nowSending;

    public void runSendingQueue() {
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

    private void onMessageSent(SentMsg msg) {
        nowSending = false;
        NotificationHelper.tryCancelNotificationForPeer(app, msg.getAccountId(), msg.getPeerId());
        send();
    }

    private void onMessageSendError(Throwable t) {
        Throwable cause = Utils.getCauseIfRuntime(t);
        nowSending = false;

        if (cause instanceof NotFoundException) {
            // no unsent messages
            return;
        }

        Toast.makeText(app, ErrorLocalizer.localizeThrowable(app, cause), Toast.LENGTH_LONG).show();
    }

    private void sendMessage(Collection<Integer> accountIds) {
        if (messagesInteractor == null) {
            throw new IllegalStateException("'messagesInteractor' was not initialized");
        }

        nowSending = true;
        compositeDisposable.add(messagesInteractor.sendUnsentMessage(accountIds)
                .subscribeOn(senderScheduler)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onMessageSent, this::onMessageSendError));
    }
}
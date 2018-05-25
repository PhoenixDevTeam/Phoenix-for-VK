package biz.dealnote.messenger.service;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.settings.Settings;

public final class MessageSender {

    private MessageSender(){

    }

    private static volatile SendService sendService;

    public static SendService getSendService() {
        if(sendService == null){
            synchronized (MessageSender.class){
                if(sendService == null){
                    sendService = new SendService(App.getInstance(), InteractorFactory.createMessagesInteractor(), Settings.get().accounts());
                }
            }
        }
        return sendService;
    }
}
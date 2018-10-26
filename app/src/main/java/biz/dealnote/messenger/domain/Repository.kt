package biz.dealnote.messenger.domain

import biz.dealnote.messenger.App
import biz.dealnote.messenger.Injection
import biz.dealnote.messenger.domain.impl.MessagesRepository
import biz.dealnote.messenger.settings.Settings

object Repository {
    val messages: IMessagesRepository by lazy {
        MessagesRepository(App.getInstance(),
                Settings.get().accounts(),
                Injection.provideNetworkInterfaces(),
                InteractorFactory.createOwnerInteractor(),
                Injection.provideStores(),
                Injection.provideUploadManager())
    }
}
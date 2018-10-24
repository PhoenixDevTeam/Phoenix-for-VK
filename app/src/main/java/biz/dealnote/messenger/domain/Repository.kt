package biz.dealnote.messenger.domain

import biz.dealnote.messenger.Injection
import biz.dealnote.messenger.domain.impl.MessagesRepository

object Repository {
    val messages: IMessagesRepository by lazy {
        MessagesRepository(Injection.provideNetworkInterfaces(),
                InteractorFactory.createOwnerInteractor(), Injection.provideStores(), Injection.provideUploadManager())
    }
}
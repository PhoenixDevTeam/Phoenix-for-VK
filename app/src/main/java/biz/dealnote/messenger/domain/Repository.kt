package biz.dealnote.messenger.domain

import biz.dealnote.messenger.App
import biz.dealnote.messenger.Injection
import biz.dealnote.messenger.db.Stores
import biz.dealnote.messenger.domain.impl.MessagesRepository
import biz.dealnote.messenger.domain.impl.OwnersRepository
import biz.dealnote.messenger.domain.impl.WallsRepository
import biz.dealnote.messenger.settings.Settings

object Repository {
    val owners: IOwnersRepository by lazy {
        OwnersRepository(Injection.provideNetworkInterfaces(), Stores.getInstance().owners())
    }

    val walls: IWallsRepository by lazy {
        WallsRepository(Injection.provideNetworkInterfaces(), Stores.getInstance(), owners)
    }

    val messages: IMessagesRepository by lazy {
        MessagesRepository(App.getInstance(),
                Settings.get().accounts(),
                Injection.provideNetworkInterfaces(),
                owners,
                Injection.provideStores(),
                Injection.provideUploadManager())
    }
}
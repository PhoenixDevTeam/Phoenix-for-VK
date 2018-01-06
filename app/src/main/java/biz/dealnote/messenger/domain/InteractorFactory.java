package biz.dealnote.messenger.domain;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.domain.impl.AccountsInteractor;
import biz.dealnote.messenger.domain.impl.AudioInteractor;
import biz.dealnote.messenger.domain.impl.BoardInteractor;
import biz.dealnote.messenger.domain.impl.CommunitiesInteractor;
import biz.dealnote.messenger.domain.impl.DatabaseInteractor;
import biz.dealnote.messenger.domain.impl.DialogsInteractor;
import biz.dealnote.messenger.domain.impl.DocsInteractor;
import biz.dealnote.messenger.domain.impl.FaveInteractor;
import biz.dealnote.messenger.domain.impl.FeedInteractor;
import biz.dealnote.messenger.domain.impl.FeedbackInteractor;
import biz.dealnote.messenger.domain.impl.GroupSettingsInteractor;
import biz.dealnote.messenger.domain.impl.LikesInteractor;
import biz.dealnote.messenger.domain.impl.MessagesInteractor;
import biz.dealnote.messenger.domain.impl.NewsfeedInteractor;
import biz.dealnote.messenger.domain.impl.OwnersInteractor;
import biz.dealnote.messenger.domain.impl.PhotosInteractor;
import biz.dealnote.messenger.domain.impl.PollInteractor;
import biz.dealnote.messenger.domain.impl.RelationshipInteractor;
import biz.dealnote.messenger.domain.impl.StickersInteractor;
import biz.dealnote.messenger.domain.impl.UtilsInteractor;
import biz.dealnote.messenger.domain.impl.VideosInteractor;
import biz.dealnote.messenger.settings.Settings;

/**
 * Created by Ruslan Kolbasa on 26.06.2017.
 * phoenix
 */
public class InteractorFactory {

    public static INewsfeedInteractor createNewsfeedInteractor(){
        return new NewsfeedInteractor(Injection.provideNetworkInterfaces(), createOwnerInteractor());
    }

    public static IStickersInteractor createStickersInteractor(){
        return new StickersInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().stickers());
    }

    public static IPollInteractor createPollInteractor(){
        return new PollInteractor(Injection.provideNetworkInterfaces());
    }

    public static IDocsInteractor createDocsInteractor(){
        return new DocsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().docs());
    }

    public static ILikesInteractor createLikesInteractor(){
        return new LikesInteractor(Injection.provideNetworkInterfaces());
    }

    public static IFeedbackInteractor createFeedbackInteractor(){
        return new FeedbackInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces());
    }

    public static IDatabaseInteractor createDatabaseInteractor(){
        return new DatabaseInteractor(Injection.provideStores().database(), Injection.provideNetworkInterfaces());
    }

    public static ICommunitiesInteractor createCommunitiesInteractor(){
        return new CommunitiesInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IBoardInteractor createBoardInteractor(){
        return new BoardInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IUtilsInteractor createUtilsInteractor(){
        return new UtilsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IRelationshipInteractor createRelationshipInteractor(){
        return new RelationshipInteractor(Injection.provideStores(), Injection.provideNetworkInterfaces());
    }

    public static IFeedInteractor createFeedInteractor(){
        return new FeedInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores(), Settings.get().other());
    }

    public static IMessagesInteractor createMessagesInteractor(){
        return new MessagesInteractor(Injection.provideNetworkInterfaces(), createOwnerInteractor(), Injection.provideStores());
    }

    public static IGroupSettingsInteractor createGroupSettingsInteractor(){
        return new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners());
    }

    public static IDialogsInteractor createDialogsInteractor(){
        return new DialogsInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IVideosInteractor createVideosInteractor(){
        return new VideosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IAccountsInteractor createAccountInteractor(){
        return new AccountsInteractor(
                Injection.provideStores(),
                Injection.provideNetworkInterfaces(),
                Injection.provideSettings().accounts(),
                Injection.provideBlacklistRepository()
        );
    }

    public static IPhotosInteractor createPhotosInteractor(){
        return new PhotosInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IOwnersInteractor createOwnerInteractor() {
        return new OwnersInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores().owners());
    }

    public static IFaveInteractor createFaveInteractor(){
        return new FaveInteractor(Injection.provideNetworkInterfaces(), Injection.provideStores());
    }

    public static IAudioInteractor createAudioInteractor() {
        return new AudioInteractor(App.getInstance(), Injection.provideNetworkInterfaces());
    }
}
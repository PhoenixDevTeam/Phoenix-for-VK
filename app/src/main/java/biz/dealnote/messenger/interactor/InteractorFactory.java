package biz.dealnote.messenger.interactor;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.interactor.impl.AccountsInteractor;
import biz.dealnote.messenger.interactor.impl.BoardInteractor;
import biz.dealnote.messenger.interactor.impl.CommunitiesInteractor;
import biz.dealnote.messenger.interactor.impl.DatabaseInteractor;
import biz.dealnote.messenger.interactor.impl.DialogsInteractor;
import biz.dealnote.messenger.interactor.impl.FaveInteractor;
import biz.dealnote.messenger.interactor.impl.FeedInteractor;
import biz.dealnote.messenger.interactor.impl.FeedbackInteractor;
import biz.dealnote.messenger.interactor.impl.GroupSettingsInteractor;
import biz.dealnote.messenger.interactor.impl.LikesInteractor;
import biz.dealnote.messenger.interactor.impl.MessagesInteractor;
import biz.dealnote.messenger.interactor.impl.OwnersInteractor;
import biz.dealnote.messenger.interactor.impl.PhotosInteractor;
import biz.dealnote.messenger.interactor.impl.RelationshipInteractor;
import biz.dealnote.messenger.interactor.impl.UtilsInteractor;
import biz.dealnote.messenger.interactor.impl.VideosInteractor;
import biz.dealnote.messenger.settings.Settings;

/**
 * Created by Ruslan Kolbasa on 26.06.2017.
 * phoenix
 */
public class InteractorFactory {

    public static ILikesInteractor createLikesInteractor(){
        return new LikesInteractor(Injection.provideNetworkInterfaces());
    }

    public static IFeedbackInteractor createFeedbackInteractor(){
        return new FeedbackInteractor(Injection.provideRepositories(), Injection.provideNetworkInterfaces());
    }

    public static IDatabaseInteractor createDatabaseInteractor(){
        return new DatabaseInteractor(Injection.provideRepositories().database(), Injection.provideNetworkInterfaces());
    }

    public static ICommunitiesInteractor createCommunitiesInteractor(){
        return new CommunitiesInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IBoardInteractor createBoardInteractor(){
        return new BoardInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IUtilsInteractor createUtilsInteractor(){
        return new UtilsInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IRelationshipInteractor createRelationshipInteractor(){
        return new RelationshipInteractor(Injection.provideRepositories(), Injection.provideNetworkInterfaces());
    }

    public static IFeedInteractor createFeedInteractor(){
        return new FeedInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories(), Settings.get().other());
    }

    public static IMessagesInteractor createMessagesInteractor(){
        return new MessagesInteractor(Injection.provideNetworkInterfaces(), createOwnerInteractor(), Injection.provideRepositories());
    }

    public static IGroupSettingsInteractor createGroupSettingsInteractor(){
        return new GroupSettingsInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories().owners());
    }

    public static IDialogsInteractor createDialogsInteractor(){
        return new DialogsInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IVideosInteractor createVideosInteractor(){
        return new VideosInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IAccountsInteractor createAccountInteractor(){
        return new AccountsInteractor(Injection.provideRepositories(), Injection.provideNetworkInterfaces(), Settings.get().accounts());
    }

    public static IPhotosInteractor createPhotosInteractor(){
        return new PhotosInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }

    public static IOwnersInteractor createOwnerInteractor() {
        return new OwnersInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories().owners());
    }

    public static IFaveInteractor createFaveInteractor(){
        return new FaveInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories());
    }
}
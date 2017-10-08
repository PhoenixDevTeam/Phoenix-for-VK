package biz.dealnote.messenger.domain;

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
        return new StickersInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories().stickers());
    }

    public static IPollInteractor createPollInteractor(){
        return new PollInteractor(Injection.provideNetworkInterfaces());
    }

    public static IDocsInteractor createDocsInteractor(){
        return new DocsInteractor(Injection.provideNetworkInterfaces(), Injection.provideRepositories().docs());
    }

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

    public static IAudioInteractor createAudioInteractor() {
        return new AudioInteractor(Injection.provideNetworkInterfaces());
    }
}
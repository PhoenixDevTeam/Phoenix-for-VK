package biz.dealnote.messenger.api.interfaces;

/**
 * Created by ruslan.kolbasa on 30.12.2016.
 * phoenix
 */
public interface IAccountApis {

    IMessagesApi messages();

    IPhotosApi photos();

    IFriendsApi friends();

    IWallApi wall();

    IDocsApi docs();

    INewsfeedApi newsfeed();

    ICommentsApi comments();

    INotificationsApi notifications();

    IVideoApi video();

    IBoardApi board();

    IUsersApi users();

    IGroupsApi groups();

    IAccountApi account();

    IDatabaseApi database();

    IAudioApi audio();

    IStatusApi status();

    ILikesApi likes();

    IPagesApi pages();

    IStoreApi store();

    IFaveApi fave();

    IPollsApi polls();

    IUtilsApi utils();

    IOtherApi other();
}

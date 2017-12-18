package biz.dealnote.messenger.place;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.activity.VideoPlayerActivity;
import biz.dealnote.messenger.dialog.ResolveDomainDialog;
import biz.dealnote.messenger.fragment.AbsWallFragment;
import biz.dealnote.messenger.fragment.AudioPlayerFragment;
import biz.dealnote.messenger.fragment.BrowserFragment;
import biz.dealnote.messenger.fragment.ChatUsersFragment;
import biz.dealnote.messenger.fragment.CommentsFragment;
import biz.dealnote.messenger.fragment.CreatePhotoAlbumFragment;
import biz.dealnote.messenger.fragment.CreatePollFragment;
import biz.dealnote.messenger.fragment.DocPreviewFragment;
import biz.dealnote.messenger.fragment.DocsFragment;
import biz.dealnote.messenger.fragment.FeedFragment;
import biz.dealnote.messenger.fragment.FeedbackFragment;
import biz.dealnote.messenger.fragment.FwdsFragment;
import biz.dealnote.messenger.fragment.GifPagerFragment;
import biz.dealnote.messenger.fragment.LikesFragment;
import biz.dealnote.messenger.fragment.MessagesLookFragment;
import biz.dealnote.messenger.fragment.PhotoPagerFragment;
import biz.dealnote.messenger.fragment.PlaylistFragment;
import biz.dealnote.messenger.fragment.PollFragment;
import biz.dealnote.messenger.fragment.PreferencesFragment;
import biz.dealnote.messenger.fragment.TopicsFragment;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.fragment.VideoPreviewFragment;
import biz.dealnote.messenger.fragment.VideosFragment;
import biz.dealnote.messenger.fragment.VideosTabsFragment;
import biz.dealnote.messenger.fragment.WallPostFragment;
import biz.dealnote.messenger.fragment.attachments.PostCreateFragment;
import biz.dealnote.messenger.fragment.attachments.RepostFragment;
import biz.dealnote.messenger.fragment.conversation.ConversationFragmentFactory;
import biz.dealnote.messenger.fragment.fave.FaveTabsFragment;
import biz.dealnote.messenger.fragment.friends.FriendsTabsFragment;
import biz.dealnote.messenger.fragment.search.SeachTabsFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.SingleTabSeachFragment;
import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.FriendsCounters;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoAlbumEditor;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.WallEditorAttrs;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

public class PlaceFactory {

    public static Place getDrawerEditPlace(){
        return new Place(Place.DRAWER_EDIT);
    }

    public static Place getProxyAddPlace(){
        return new Place(Place.PROXY_ADD);
    }

    public static Place getUserBlackListPlace(int accountId){
        return new Place(Place.USER_BLACKLIST)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getRequestExecutorPlace(int accountId){
        return new Place(Place.REQUEST_EXECUTOR)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getCommunityManagerEditPlace(int accountId, int groupId, Manager manager){
        return new Place(Place.COMMUNITY_MANAGER_EDIT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId)
                .withParcelableExtra(Extra.MANAGER, manager);
    }

    public static Place getCommunityManagerAddPlace(int accountId, int groupId, ArrayList<User> users){
        Place place = new Place(Place.COMMUNITY_MANAGER_ADD)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId);

        place.getArgs().putParcelableArrayList(Extra.USERS, users);
        return place;
    }

    public static Place getTmpSourceGalleryPlace(int accountId, @NonNull TmpSource source, int index){
        return new Place(Place.VK_PHOTO_TMP_SOURCE)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.INDEX, index)
                .withParcelableExtra(Extra.SOURCE, source);
    }

    public static Place getCommunityAddBanPlace(int accountId, int groupId, ArrayList<User> users){
       Place place = new Place(Place.COMMUNITY_ADD_BAN)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId);
        place.getArgs().putParcelableArrayList(Extra.USERS, users);
        return place;
    }

    public static Place getCommunityBanEditPlace(int accountId, int groupId, Banned banned){
        return new Place(Place.COMMUNITY_BAN_EDIT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.GROUP_ID, groupId)
                .withParcelableExtra(Extra.BANNED, banned);
    }

    public static Place getCommunityControlPlace(int accountId, Community community, GroupSettings settings){
        return new Place(Place.COMMUNITY_CONTROL)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.SETTINGS, settings)
                .withParcelableExtra(Extra.OWNER, community);
    }

    public static Place getNewsfeedCommentsPlace(int accountId){
        return new Place(Place.NEWSFEED_COMMENTS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId);
    }

    public static Place getSingleTabSearchPlace(int accountId, @SearchContentType int type, @Nullable BaseSearchCriteria criteria){
        return new Place(Place.SINGLE_SEARCH)
                .setArguments(SingleTabSeachFragment.buildArgs(accountId, type, criteria));
    }

    public static Place getLogsPlace(){
        return new Place(Place.LOGS);
    }

    public static Place getLocalImageAlbumPlace(LocalImageAlbum album){
        return new Place(Place.LOCAL_IMAGE_ALBUM)
                .withParcelableExtra(Extra.ALBUM, album);
    }

    public static Place getCommentCreatePlace(int accountId, int commentId, int sourceOwnerId, String body){
        return new Place(Place.COMMENT_CREATE)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.COMMENT_ID, commentId)
                .withIntExtra(Extra.OWNER_ID, sourceOwnerId)
                .withStringExtra(Extra.BODY, body);
    }

    public static Place getPhotoAlbumGalleryPlace(int accountId, int albumId, int ownerId, Integer focusPhotoId){
        return new Place(Place.VK_PHOTO_ALBUM_GALLERY)
                .setArguments(PhotoPagerFragment.buildArgsForAlbum(accountId, albumId, ownerId, focusPhotoId));
    }

    public static Place getSimpleGalleryPlace(int accountId, ArrayList<Photo> photos, int position, boolean needRefresh){
        return new Place(Place.SIMPLE_PHOTO_GALLERY)
                .setArguments(PhotoPagerFragment.buildArgsForSimpleGallery(accountId, position, photos, needRefresh));
    }

    public static Place getFavePhotosGallery(int accountId, ArrayList<Photo> photos, int position){
        return new Place(Place.FAVE_PHOTOS_GALLERY)
                .setArguments(PhotoPagerFragment.buildArgsForFave(accountId, photos, position));
    }

    public static Place getCreatePollPlace(int accountId, int ownerId){
        return new Place(Place.CREATE_POLL).setArguments(CreatePollFragment.buildArgs(accountId, ownerId));
    }

    public static Place getPollPlace(int accountId, @NonNull Poll poll){
        return new Place(Place.POLL).setArguments(PollFragment.buildArgs(accountId, poll));
    }

    public static Place getGifPagerPlace(int accountId, @NonNull ArrayList<Document> documents, int index){
        Place place = new Place(Place.GIF_PAGER);
        place.setArguments(GifPagerFragment.buildArgs(accountId, documents, index));
        return place;
    }

    public static Place getPlaylistPlace(){
        Place place = new Place(Place.AUDIO_CURRENT_PLAYLIST);
        place.setArguments(PlaylistFragment.buildArgs((ArrayList<Audio>) MusicUtils.getQueue()));
        return place;
    }

    public static Place getMessagesLookupPlace(int aid, int peerId, int focusMessageId){
        return new Place(Place.MESSAGE_LOOKUP)
                .setArguments(MessagesLookFragment.buildArgs(aid, peerId, focusMessageId));
    }

    public static Place getEditPhotoAlbumPlace(int aid, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor){
        return new Place(Place.EDIT_PHOTO_ALBUM)
                .setArguments(CreatePhotoAlbumFragment.buildArgsForEdit(aid, album, editor));
    }

    public static Place getCreatePhotoAlbumPlace(int aid, int ownerId){
        return new Place(Place.CREATE_PHOTO_ALBUM)
                .setArguments(CreatePhotoAlbumFragment.buildArgsForCreate(aid, ownerId));
    }

    public static Place getNotificationSettingsPlace(){
        return new Place(Place.NOTIFICATION_SETTINGS);
    }

    public static Place getSecuritySettingsPlace(){
        return new Place(Place.SECURITY);
    }

    public static Place getVkInternalPlayerPlace(Video video, int size){
        Place place = new Place(Place.VK_INTERNAL_PLAYER);
        place.prepareArguments().putParcelable(VideoPlayerActivity.EXTRA_VIDEO, video);
        place.prepareArguments().putInt(VideoPlayerActivity.EXTRA_SIZE, size);
        return place;
    }

    public static Place getResolveDomainPlace(int aid, String url, String domain){
        return new Place(Place.RESOLVE_DOMAIN).setArguments(ResolveDomainDialog.buildArgs(aid, url, domain));
    }

    public static Place getBookmarksPlace(int aid, int tab){
        return new Place(Place.BOOKMARKS).setArguments(FaveTabsFragment.buildArgs(aid, tab));
    }

    public static Place getNotificationsPlace(int aid){
        return new Place(Place.NOTIFICATIONS).setArguments(FeedbackFragment.buildArgs(aid));
    }

    public static Place getFeedPlace(int aid){
        return new Place(Place.FEED).setArguments(FeedFragment.buildArgs(aid));
    }

    public static Place getDocumentsPlace(int aid, int ownerId, String action){
        return new Place(Place.DOCS).setArguments(DocsFragment.buildArgs(aid, ownerId, action));
    }

    public static Place getPreferencesPlace(int aid){
        return new Place(Place.PREFERENCES).setArguments(PreferencesFragment.buildArgs(aid));
    }

    public static Place getDialogsPlace(int accountId, int dialogsOwnerId, @Nullable String subtitle){
        return new Place(Place.DIALOGS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, dialogsOwnerId)
                .withStringExtra(Extra.SUBTITLE, subtitle);
    }

    public static Place getChatPlace(int accountId, int messagesOwnerId, @NonNull Peer peer){
        return new Place(Place.CHAT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, messagesOwnerId)
                .withParcelableExtra(Extra.PEER, peer);
    }

    public static Place getVKPhotosAlbumPlace(int accountId, int ownerId, int albumId, String action){
        return new Place(Place.VK_PHOTO_ALBUM).setArguments(VKPhotosFragment.buildArgs(accountId, ownerId, albumId, action));
    }

    public static Place getVKPhotoAlbumsPlace(int accountId, int ownerId, String action, ParcelableOwnerWrapper ownerWrapper){
        return new Place(Place.VK_PHOTO_ALBUMS)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.OWNER_ID, ownerId)
                .withStringExtra(Extra.ACTION, action)
                .withParcelableExtra(Extra.OWNER, ownerWrapper);
    }

    public static Place getWikiPagePlace(int accountId, String url){
        return new Place(Place.WIKI_PAGE).setArguments(BrowserFragment.buildArgs(accountId, url));
    }

    public static Place getExternalLinkPlace(int accountId, String url){
        return new Place(Place.EXTERNAL_LINK).setArguments(BrowserFragment.buildArgs(accountId, url));
    }

    public static Place getRepostPlace(int accountId, Integer gid, Post post){
        return new Place(Place.REPOST).setArguments(RepostFragment.buildArgs(accountId, gid, post));
    }

    public static Place getEditCommentPlace(int accountId, Comment comment){
        return new Place(Place.EDIT_COMMENT)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.COMMENT, comment);
    }

    public static Place getAudiosPlace(int accountId, int ownerId){
        return new Place(Place.AUDIOS).withIntExtra(Extra.ACCOUNT_ID, accountId).withIntExtra(Extra.OWNER_ID, ownerId);
    }

    public static Place getPlayerPlace(int accountId){
        return new Place(Place.PLAYER).setArguments(AudioPlayerFragment.buildArgs(accountId));
    }

    public static Place getVideosPlace(int accountId, int ownerId, String action){
        return new Place(Place.VIDEOS).setArguments(VideosTabsFragment.buildArgs(accountId, ownerId, action));
    }

    public static Place getVideoAlbumPlace(int accoutnId, int ownerId, int albumId, String action, @Nullable String albumTitle){
        return new Place(Place.VIDEO_ALBUM)
                .setArguments(VideosFragment.buildArgs(accoutnId, ownerId, albumId, action, albumTitle));
    }

    public static Place getVideoPreviewPlace(int accountId, @NonNull Video video){
        return new Place(Place.VIDEO_PREVIEW)
                .setArguments(VideoPreviewFragment.buildArgs(accountId, video.getOwnerId(), video.getId(), video));
    }

    public static Place getVideoPreviewPlace(int accountId, int ownerId, int videoId, @Nullable Video video){
        return new Place(Place.VIDEO_PREVIEW)
                .setArguments(VideoPreviewFragment.buildArgs(accountId, ownerId, videoId, video));
    }

    public static Place getLikesCopiesPlace(int accountId, String type, int ownerId, int itemId, String filter){
        return new Place(Place.LIKES_AND_COPIES)
                .setArguments(LikesFragment.buildArgs(accountId, type, ownerId, itemId, filter));
    }

    public static Place getCommunitiesPlace(int accountId, int userId){
        return new Place(Place.COMMUNITIES)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withIntExtra(Extra.USER_ID, userId);
    }

    public static Place getFriendsFollowersPlace(int accountId, int userId, int tab, FriendsCounters counters){
        return new Place(Place.FRIENDS_AND_FOLLOWERS)
                .setArguments(FriendsTabsFragment.buildArgs(accountId, userId, tab, counters));
    }

    public static Place getChatMembersPlace(int accountId, int chatId){
        return new Place(Place.CHAT_MEMBERS).setArguments(ChatUsersFragment.buildArgs(accountId, chatId));
    }

    public static Place getOwnerWallPlace(int accountId, @NonNull Owner owner){
        int ownerId = owner.getOwnerId();
        return getOwnerWallPlace(accountId, ownerId, owner);
    }

    public static Place getOwnerWallPlace(int accountId, int ownerId, @Nullable Owner owner){
        return new Place(Place.WALL).setArguments(AbsWallFragment.buildArgs(accountId, ownerId, owner));
    }

    public static Place getTopicsPlace(int accountId, int ownerId){
        return new Place(Place.TOPICS).setArguments(TopicsFragment.buildArgs(accountId, ownerId));
    }

    public static Place getSearchPlace(int accountId, int tab, @Nullable BaseSearchCriteria criteria){
        return new Place(Place.SEARCH).setArguments(SeachTabsFragment.buildArgs(accountId, tab, criteria));
    }

    public static Place getCreatePostPlace(int accountId, int ownerId, @EditingPostType int editingType,
                                           @Nullable List<AbsModel> input, @NonNull WallEditorAttrs attrs){
        ModelsBundle bundle = new ModelsBundle(Utils.safeCountOf(input));
        if(Objects.nonNull(input)){
            bundle.append(input);
        }

        return new Place(Place.BUILD_NEW_POST)
                .setArguments(PostCreateFragment.buildArgs(accountId, ownerId, editingType, bundle, attrs));
    }

    public static Place getForwardMessagesPlace(int accountId, ArrayList<Message> messages){
        return new Place(Place.FORWARD_MESSAGES).setArguments(FwdsFragment.buildArgs(accountId, messages));
    }

    public static Place getEditPostPlace(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs){
        return new Place(Place.EDIT_POST)
                .withIntExtra(Extra.ACCOUNT_ID, accountId)
                .withParcelableExtra(Extra.POST, post)
                .withParcelableExtra(Extra.ATTRS, attrs);
    }

    public static Place getPostPreviewPlace(int accountId, int postId, int ownerId){
        return getPostPreviewPlace(accountId, postId, ownerId, null);
    }

    public static Place getPostPreviewPlace(int accountId, int postId, int ownerId, Post post){
        return new Place(Place.WALL_POST)
                .setArguments(WallPostFragment.buildArgs(accountId, postId, ownerId, post));
    }

    public static Place getDocPreviewPlace(int accountId, int docId, int ownerId, @Nullable Document document){
        Place place = new Place(Place.DOC_PREVIEW);
        place.setArguments(DocPreviewFragment.buildArgs(accountId, docId, ownerId, document));
        return place;
    }

    public static Place getDocPreviewPlace(int accountId, @NonNull Document document){
        return getDocPreviewPlace(accountId, document.getId(), document.getOwnerId(), document);
    }

    public static Place getConversationAttachmentsPlace(int accountId, int peerId, String type){
        return new Place(Place.CONVERSATION_ATTACHMENTS)
                .setArguments(ConversationFragmentFactory.buildArgs(accountId, peerId, type));
    }

    public static Place getCommentsPlace(int accountId, Commented commented, Integer focusToCommentId){
        return new Place(Place.COMMENTS)
                .setArguments(CommentsFragment.buildArgs(accountId, commented, focusToCommentId));
    }

    private PlaceFactory(){

    }
}
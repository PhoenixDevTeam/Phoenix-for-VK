package biz.dealnote.messenger.link;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.fragment.fave.FaveTabsFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.link.types.AbsLink;
import biz.dealnote.messenger.link.types.AudiosLink;
import biz.dealnote.messenger.link.types.BoardLink;
import biz.dealnote.messenger.link.types.DialogLink;
import biz.dealnote.messenger.link.types.DocLink;
import biz.dealnote.messenger.link.types.DomainLink;
import biz.dealnote.messenger.link.types.FaveLink;
import biz.dealnote.messenger.link.types.FeedSearchLink;
import biz.dealnote.messenger.link.types.OwnerLink;
import biz.dealnote.messenger.link.types.PageLink;
import biz.dealnote.messenger.link.types.PhotoAlbumLink;
import biz.dealnote.messenger.link.types.PhotoAlbumsLink;
import biz.dealnote.messenger.link.types.PhotoLink;
import biz.dealnote.messenger.link.types.TopicLink;
import biz.dealnote.messenger.link.types.VideoLink;
import biz.dealnote.messenger.link.types.WallCommentLink;
import biz.dealnote.messenger.link.types.WallLink;
import biz.dealnote.messenger.link.types.WallPostLink;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.CommentedType;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.place.PlaceFactory;

import static biz.dealnote.messenger.util.Utils.singletonArrayList;

public class LinkHelper {

    public static void openUrl(Activity context, int accountId, String link) {
        if (!openVKlink(context, accountId, link)) {
            PlaceFactory.getExternalLinkPlace(accountId, link).tryOpenWith(context);
        }
    }

    public static boolean openVKLink(Activity activity, int accountId, AbsLink link) {
        switch (link.type) {
            case AbsLink.WALL_COMMENT:
                WallCommentLink wallCommentLink = (WallCommentLink) link;

                Commented commented = new Commented(wallCommentLink.getPostId(), wallCommentLink.getOwnerId(), CommentedType.POST, null);
                PlaceFactory.getCommentsPlace(accountId, commented, wallCommentLink.getCommentId()).tryOpenWith(activity);
                break;

            case AbsLink.DIALOGS:
                PlaceFactory.getDialogsPlace(accountId, accountId, null).tryOpenWith(activity);
                break;

            case AbsLink.PHOTO:
                PhotoLink photoLink = (PhotoLink) link;

                Photo photo = new Photo()
                        .setId(photoLink.id)
                        .setOwnerId(photoLink.ownerId);


                PlaceFactory.getSimpleGalleryPlace(accountId, singletonArrayList(photo), 0, true).tryOpenWith(activity);
                break;

            case AbsLink.PHOTO_ALBUM:
                PhotoAlbumLink photoAlbumLink = (PhotoAlbumLink) link;
                PlaceFactory.getVKPhotosAlbumPlace(accountId, photoAlbumLink.ownerId,
                        photoAlbumLink.albumId, null).tryOpenWith(activity);
                break;

            case AbsLink.PROFILE:
            case AbsLink.GROUP:
                OwnerLink ownerLink = (OwnerLink) link;
                PlaceFactory.getOwnerWallPlace(accountId, ownerLink.ownerId, null).tryOpenWith(activity);
                break;

            case AbsLink.TOPIC:
                TopicLink topicLink = (TopicLink) link;
                PlaceFactory.getCommentsPlace(accountId, new Commented(topicLink.topicId, topicLink.ownerId,
                        CommentedType.TOPIC, null), null).tryOpenWith(activity);
                break;

            case AbsLink.WALL_POST:
                WallPostLink wallPostLink = (WallPostLink) link;
                PlaceFactory.getPostPreviewPlace(accountId, wallPostLink.postId, wallPostLink.ownerId)
                        .tryOpenWith(activity);
                break;

            case AbsLink.ALBUMS:
                PhotoAlbumsLink photoAlbumsLink = (PhotoAlbumsLink) link;
                PlaceFactory.getVKPhotoAlbumsPlace(accountId, photoAlbumsLink.ownerId, VKPhotosFragment.ACTION_SHOW_PHOTOS, null).tryOpenWith(activity);
                break;

            case AbsLink.DIALOG:
                DialogLink dialogLink = (DialogLink) link;
                Peer peer = new Peer(dialogLink.peerId);
                PlaceFactory.getChatPlace(accountId, accountId, peer).tryOpenWith(activity);
                break;

            case AbsLink.WALL:
                WallLink wallLink = (WallLink) link;
                PlaceFactory.getOwnerWallPlace(accountId, wallLink.ownerId, null).tryOpenWith(activity);
                break;

            case AbsLink.VIDEO:
                VideoLink videoLink = (VideoLink) link;
                PlaceFactory.getVideoPreviewPlace(accountId, videoLink.ownerId, videoLink.videoId, null)
                        .tryOpenWith(activity);
                break;

            case AbsLink.AUDIOS:
                AudiosLink audiosLink = (AudiosLink) link;
                PlaceFactory.getAudiosPlace(accountId, audiosLink.ownerId).tryOpenWith(activity);
                break;

            case AbsLink.DOMAIN:
                DomainLink domainLink = (DomainLink) link;
                PlaceFactory.getResolveDomainPlace(accountId, domainLink.fullLink, domainLink.domain)
                        .tryOpenWith(activity);
                break;

            case AbsLink.PAGE:
                PlaceFactory.getWikiPagePlace(accountId, ((PageLink) link).getLink()).tryOpenWith(activity);
                break;

            case AbsLink.DOC:
                DocLink docLink = (DocLink) link;
                PlaceFactory.getDocPreviewPlace(accountId, docLink.docId, docLink.ownerId, null).tryOpenWith(activity);
                break;

            case AbsLink.FAVE:
                FaveLink faveLink = (FaveLink) link;
                int targetTab = FaveTabsFragment.getTabByLinkSection(faveLink.section);
                if (targetTab == FaveTabsFragment.TAB_UNKNOWN) {
                    return false;
                }

                PlaceFactory.getBookmarksPlace(accountId, targetTab).tryOpenWith(activity);
                break;

            case AbsLink.BOARD:
                BoardLink boardLink = (BoardLink) link;
                PlaceFactory.getTopicsPlace(accountId, -Math.abs(boardLink.getGroupId())).tryOpenWith(activity);
                break;

            case AbsLink.FEED_SEARCH:
                FeedSearchLink feedSearchLink = (FeedSearchLink) link;
                NewsFeedCriteria criteria = new NewsFeedCriteria(feedSearchLink.getQ());
                PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.NEWS, criteria).tryOpenWith(activity);
                break;

            default:
                return false;
        }

        return true;
    }

    private static boolean openVKlink(Activity activity, int accoutnId, String url) {
        AbsLink link = VkLinkParser.parse(url);
        return link != null && openVKLink(activity, accoutnId, link);
    }

    public static void openLinkInBrowser(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.error_activity_not_found, Toast.LENGTH_LONG).show();
        }
    }

    public static Commented findCommentedFrom(String url) {
        AbsLink link = VkLinkParser.parse(url);
        Commented commented = null;
        if (link != null) {
            switch (link.type) {
                case AbsLink.WALL_POST:
                    WallPostLink wallPostLink = (WallPostLink) link;
                    commented = new Commented(wallPostLink.postId, wallPostLink.ownerId, CommentedType.POST, null);
                    break;
                case AbsLink.PHOTO:
                    PhotoLink photoLink = (PhotoLink) link;
                    commented = new Commented(photoLink.id, photoLink.ownerId, CommentedType.PHOTO, null);
                    break;
                case AbsLink.VIDEO:
                    VideoLink videoLink = (VideoLink) link;
                    commented = new Commented(videoLink.videoId, videoLink.ownerId, CommentedType.VIDEO, null);
                    break;
                case AbsLink.TOPIC:
                    TopicLink topicLink = (TopicLink) link;
                    commented = new Commented(topicLink.topicId, topicLink.ownerId, CommentedType.TOPIC, null);
                    break;
            }
        }

        return commented;
    }
}

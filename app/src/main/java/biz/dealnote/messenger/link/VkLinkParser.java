package biz.dealnote.messenger.link;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.dealnote.messenger.link.types.AbsLink;
import biz.dealnote.messenger.link.types.AudiosLink;
import biz.dealnote.messenger.link.types.AwayLink;
import biz.dealnote.messenger.link.types.DialogLink;
import biz.dealnote.messenger.link.types.DialogsLink;
import biz.dealnote.messenger.link.types.DocLink;
import biz.dealnote.messenger.link.types.DomainLink;
import biz.dealnote.messenger.link.types.FaveLink;
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
import biz.dealnote.messenger.model.Peer;

public class VkLinkParser {

    private static final Pattern PATTERN_PHOTOS = Pattern.compile("vk\\.com/photos(-?\\d*)");
    private static final Pattern PATTERN_PROFILE_ID = Pattern.compile("vk\\.com//id(\\d+)$");
    private static final Pattern PATTERN_DOMAIN = Pattern.compile("vk\\.com/([\\w.]+)");
    private static final Pattern PATTERN_WALL_POST = Pattern.compile("vk.com/(?:[\\w.\\d]+\\?(?:[\\w=&]+)?w=)?wall(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_AWAY = Pattern.compile("vk\\.com/away(\\.php)?\\?(.*)");
    private static final Pattern PATTERN_DIALOG = Pattern.compile("vk\\.com/im\\?sel=(c?)(\\d+)");
    private static final Pattern PATTERN_ALBUMS = Pattern.compile("vk\\.com/albums(-?\\d+)");
    private static final Pattern PATTERN_AUDIOS = Pattern.compile("vk\\.com/audios(-?\\d+)");
    private static final Pattern PATTERN_ALBUM = Pattern.compile("vk\\.com/album(-)?(\\d*)_(\\d*)");
    private static final Pattern PATTERN_WALL = Pattern.compile("vk\\.com/wall(-?\\d*)");
    private static final Pattern PATTERN_PHOTO = Pattern.compile("vk\\.com/(\\w)*(-)?(\\d)*(\\?z=)?photo(-?\\d*_\\d*)");
    private static final Pattern PATTERN_VIDEO = Pattern.compile("vk\\.com/video(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_DOC = Pattern.compile("vk\\.com/doc(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_TOPIC = Pattern.compile("vk\\.com/topic-(\\d*)_(\\d*)");
    private static final Pattern PATTERN_FAVE = Pattern.compile("vk\\.com/fave");
    private static final Pattern PATTERN_GROUP_ID = Pattern.compile("vk\\.com/(club|event|public)(\\d+)$");
    private static final Pattern PATTERN_FAVE_WITH_SECTION = Pattern.compile("vk\\.com/fave\\?section=([\\w.]+)");

    //vk.com/wall-2345345_7834545?reply=15345346
    private static final Pattern PATTERN_WALL_POST_COMMENT = Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)");

    public static AbsLink parse(String string) {
        if (!string.contains("vk.com")) {
            return null;
        }

        AbsLink vkLink = parseWallCommentLink(string);
        if (vkLink != null) {
            return vkLink;
        }

        if (string.contains("vk.com/images")) {
            return null;
        }

        if (string.contains("vk.com/search")) {
            return null;
        }

        if (string.contains("vk.com/feed") && !string.contains("?z=photo") && !string.contains("w=wall") && !string.contains("?w=page")) {
            return null;
        }

        if (string.contains("vk.com/friends")) {
            return null;
        }

        if (Pattern.matches(".*vk.com/app\\d.*", string)) {
            return null;
        }

        if (string.endsWith("vk.com/support")) {
            return null;
        }

        if (string.endsWith("vk.com/restore")) {
            return null;
        }

        if (string.contains("vk.com/restore?")) {
            return null;
        }

        if (string.endsWith("vk.com/page")) {
            return null;
        }

        if (string.contains("vk.com/board")) {
            return null;
        }

        if (string.contains("vk.com/login")) {
            return null;
        }

        if (string.contains("vk.com/bugs")) {
            return null;
        }

        if (Pattern.matches(".*vk.com/note\\d.*", string)) {
            return null;
        }

        if (string.contains("vk.com/dev/")) {
            return null;
        }

        if (string.contains("vk.com/wall") && string.contains("?reply=")) {
            return null;
        }

        if (string.endsWith("vk.com/mail")) {
            return new DialogsLink();
        }

        if (string.endsWith("vk.com/im")) {
            return new DialogsLink();
        }

       vkLink = VkLinkParser.parsePage(string);
        if (vkLink != null){
            return vkLink;
        }

        vkLink = VkLinkParser.parsePhoto(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseAlbum(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseProfileById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseGroupById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseTopic(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWallPost(string);
        if (vkLink != null) {
            return vkLink;
        }

    /*    vkLink = VkLinkParser.parseAway(string);
        if (vkLink != null) {
            return vkLink;
        }
    */

        if (string.contains("/im?sel")) {
            vkLink = parseDialog(string);
            if (vkLink != null) {
                return vkLink;
            }
        }

        vkLink = parseAlbums(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseWall(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseVideo(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseAudios(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseDoc(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parsePhotos(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseFave(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = VkLinkParser.parseDomain(string);
        if (vkLink != null) {
            return vkLink;
        }

        return null;
    }

    private static AbsLink parseAlbum(String string) {
        Matcher matcher = PATTERN_ALBUM.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(2);
        String divider = matcher.group(1);
        String albumId = matcher.group(3);

        if (!TextUtils.isEmpty(divider)) {
            ownerId = "-" + ownerId;
        }

        if (albumId.equals("0")) {
            albumId = Long.toString(-6);
        }

        if (albumId.equals("00")) {
            albumId = Long.toString(-7);
        }

        if (albumId.equals("000")) {
            albumId = Long.toString(-15);
        }

        return new PhotoAlbumLink(Integer.parseInt(ownerId), Integer.parseInt(albumId));
    }

    private static AbsLink parseAlbums(String string) {
        Matcher matcher = PATTERN_ALBUMS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(1);
        return new PhotoAlbumsLink(Integer.parseInt(ownerId));
    }

    private static AbsLink parseAway(String string) {
        if (!PATTERN_AWAY.matcher(string).find()) {
            return null;
        }

        return new AwayLink(string);
    }

    private static AbsLink parseDialog(String string) {
        Matcher matcher = PATTERN_DIALOG.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String chat = matcher.group(5);
        int id = Integer.parseInt(matcher.group(6));
        boolean isChat = !TextUtils.isEmpty(chat);

        return new DialogLink(isChat ? Peer.fromChatId(id) : id);
    }

    private static AbsLink parseDomain(String string) {
        Matcher matcher = PATTERN_DOMAIN.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new DomainLink(string, matcher.group(1));
    }

    private static AbsLink parseGroupById(String string) {
        Matcher matcher = PATTERN_GROUP_ID.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        Integer groupid = Integer.parseInt(matcher.group(2));
        return new OwnerLink(-Math.abs(groupid));
    }

    private static AbsLink parsePage(String string) {
        if (string.contains("vk.com/pages")
                || string.contains("vk.com/page")
                || string.contains("vkontakte.ru/pages")
                || string.contains("vkontakte.ru/page")
                || string.contains("vk.com") && string.contains("w=page")) {
            return new PageLink(string);
        }

        return null;
    }


    private static AbsLink parsePhoto(String string) {
        Matcher matcher = PATTERN_PHOTO.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String path = matcher.group(9);
        return new PhotoLink(path);
    }

    private static AbsLink parsePhotos(String string) {
        Matcher matcher = PATTERN_PHOTOS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new PhotoAlbumsLink(Integer.parseInt(matcher.group(1)));
    }

    private static AbsLink parseProfileById(String string) {
        Matcher matcher = PATTERN_PROFILE_ID.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        int ownerId = Integer.valueOf(matcher.group(1));
        return new OwnerLink(ownerId);
    }

    private static AbsLink parseTopic(String string) {
        Matcher matcher = PATTERN_TOPIC.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new TopicLink(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(1)));
    }

    private static AbsLink parseVideo(String string) {
        Matcher matcher = PATTERN_VIDEO.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        try {
            int ownerId = Integer.parseInt(matcher.group(1));
            int videoId = Integer.parseInt(matcher.group(2));
            return new VideoLink(ownerId, videoId);
        } catch (NumberFormatException ignored){
            return null;
        }
    }

    private static AbsLink parseFave(String string) {
        Matcher matcherWithSection = PATTERN_FAVE_WITH_SECTION.matcher(string);
        Matcher matcher = PATTERN_FAVE.matcher(string);

        if (matcherWithSection.find()) {
            return new FaveLink(matcherWithSection.group(1));
        }

        if(matcher.find()){
            return new FaveLink();
        }

        return null;
    }

    private static AbsLink parseAudios(String string) {
        Matcher matcher = PATTERN_AUDIOS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new AudiosLink(Integer.parseInt(matcher.group(1)));
    }

    private static AbsLink parseDoc(String string) {
        Matcher matcher = PATTERN_DOC.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new DocLink(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }

    private static AbsLink parseWall(String string) {
        Matcher matcher = PATTERN_WALL.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallLink(Integer.parseInt(matcher.group(1)));
    }

    private static AbsLink parseWallCommentLink(String string){
        Matcher matcher = PATTERN_WALL_POST_COMMENT.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        WallCommentLink link = new WallCommentLink(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
        return link.isValid() ? link : null;
    }

    private static AbsLink parseWallPost(String string) {
        Matcher matcher = PATTERN_WALL_POST.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallPostLink(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
    }
}
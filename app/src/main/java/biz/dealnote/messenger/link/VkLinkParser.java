package biz.dealnote.messenger.link;

import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.dealnote.messenger.link.types.AbsLink;
import biz.dealnote.messenger.link.types.AudiosLink;
import biz.dealnote.messenger.link.types.AwayLink;
import biz.dealnote.messenger.link.types.BoardLink;
import biz.dealnote.messenger.link.types.DialogLink;
import biz.dealnote.messenger.link.types.DialogsLink;
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
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.util.Optional;

import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static java.lang.Integer.parseInt;

public class VkLinkParser {

    private static final Pattern PATTERN_PHOTOS = Pattern.compile("vk\\.com/photos(-?\\d*)"); //+
    private static final Pattern PATTERN_PROFILE_ID = Pattern.compile("(m\\.)?vk\\.com/id(\\d+)$"); //+
    private static final Pattern PATTERN_DOMAIN = Pattern.compile("vk\\.com/([\\w.]+)");
    private static final Pattern PATTERN_WALL_POST = Pattern.compile("vk.com/(?:[\\w.\\d]+\\?(?:[\\w=&]+)?w=)?wall(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_AWAY = Pattern.compile("vk\\.com/away(\\.php)?\\?(.*)");
    private static final Pattern PATTERN_DIALOG = Pattern.compile("vk\\.com/im\\?sel=(c?)(-?\\d+)");
    private static final Pattern PATTERN_ALBUMS = Pattern.compile("vk\\.com/albums(-?\\d+)");
    private static final Pattern PATTERN_AUDIOS = Pattern.compile("vk\\.com/audios(-?\\d+)");
    private static final Pattern PATTERN_ALBUM = Pattern.compile("vk\\.com/album(-?\\d*)_(\\d*)");
    private static final Pattern PATTERN_WALL = Pattern.compile("vk\\.com/wall(-?\\d*)");
    private static final Pattern PATTERN_PHOTO = Pattern.compile("vk\\.com/(\\w)*(-)?(\\d)*(\\?z=)?photo(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_VIDEO = Pattern.compile("vk\\.com/video(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_DOC = Pattern.compile("vk\\.com/doc(-?\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_TOPIC = Pattern.compile("vk\\.com/topic-(\\d*)_(\\d*)"); //+
    private static final Pattern PATTERN_FAVE = Pattern.compile("vk\\.com/fave");
    private static final Pattern PATTERN_GROUP_ID = Pattern.compile("vk\\.com/(club|event|public)(\\d+)$"); //+
    private static final Pattern PATTERN_FAVE_WITH_SECTION = Pattern.compile("vk\\.com/fave\\?section=([\\w.]+)");

    //vk.com/wall-2345345_7834545?reply=15345346
    private static final Pattern PATTERN_WALL_POST_COMMENT = Pattern.compile("vk\\.com/wall(-?\\d*)_(\\d*)\\?reply=(\\d*)");
    private static final Pattern PATTERN_BOARD = Pattern.compile("vk\\.com/board(\\d+)");
    private static final Pattern PATTERN_FEED_SEARCH = Pattern.compile("vk\\.com/feed\\?q=([^&]*)&section=search");

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

        if (string.contains("vk.com/feed") && !string.contains("?z=photo") && !string.contains("w=wall") && !string.contains("?w=page") && !string.contains("?q=")) {
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

        for(IParser parser : PARSERS){
            try {
                AbsLink link = parser.parse(string).get();
                if(link != null){
                    return link;
                }
            } catch (Exception ignored){
            }
        }

        vkLink = parseBoard(string);
        if(vkLink != null){
            return vkLink;
        }

        vkLink = parsePage(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePhoto(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseAlbum(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseProfileById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseGroupById(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseTopic(string);
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

        vkLink = parseAudios(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseDoc(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parsePhotos(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseFave(string);
        if (vkLink != null) {
            return vkLink;
        }

        vkLink = parseDomain(string);
        if (vkLink != null) {
            return vkLink;
        }

        return null;
    }

    private static final List<IParser> PARSERS = new LinkedList<>();
    static {
        PARSERS.add(string -> {
            Matcher matcher = PATTERN_FEED_SEARCH.matcher(string);
            if(matcher.find()){
                String q = URLDecoder.decode(matcher.group(1), "UTF-8");
                return Optional.wrap(new FeedSearchLink(q));
            }
            return Optional.empty();
        });
    }

    private interface IParser {
        Optional<AbsLink> parse(String string) throws Exception;
    }

    private static AbsLink parseBoard(String string){
        Matcher matcher = PATTERN_BOARD.matcher(string);

        try {
            if(matcher.find()){
                String groupId = matcher.group(1);
                return new BoardLink(Integer.parseInt(groupId));
            }
        } catch (Exception ignored){

        }

        return null;
    }

    private static AbsLink parseAlbum(String string) {
        Matcher matcher = PATTERN_ALBUM.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(1);
        String albumId = matcher.group(2);

        if (albumId.equals("0")) {
            albumId = Long.toString(-6);
        }

        if (albumId.equals("00")) {
            albumId = Long.toString(-7);
        }

        if (albumId.equals("000")) {
            albumId = Long.toString(-15);
        }

        return new PhotoAlbumLink(parseInt(ownerId), parseInt(albumId));
    }

    private static AbsLink parseAlbums(String string) {
        Matcher matcher = PATTERN_ALBUMS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        String ownerId = matcher.group(1);
        return new PhotoAlbumsLink(parseInt(ownerId));
    }

    private static AbsLink parseAway(String string) {
        if (!PATTERN_AWAY.matcher(string).find()) {
            return null;
        }

        return new AwayLink(string);
    }

    private static AbsLink parseDialog(String string) {
        Matcher matcher = PATTERN_DIALOG.matcher(string);

        try {
            if(matcher.find()){
                String chat = matcher.group(1);
                int id = parseInt(matcher.group(2));
                boolean isChat = nonEmpty(chat);
                return new DialogLink(isChat ? Peer.fromChatId(id) : Peer.fromOwnerId(id));
            }
        } catch (Exception ignored){
        }

        return null;
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

        try {
            if (matcher.find()) {
                return new OwnerLink(-Math.abs(parseInt(matcher.group(2))));
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private static AbsLink parsePage(String string) {
        if (string.contains("vk.com/pages")
                || string.contains("vk.com/page")
                || string.contains("vk.com") && string.contains("w=page")) {
            return new PageLink(string.replace("m.vk.com/", "vk.com/"));
        }

        return null;
    }


    private static AbsLink parsePhoto(String string) {
        Matcher matcher = PATTERN_PHOTO.matcher(string);

        try {
            if (matcher.find()) {
                int ownerId = parseInt(matcher.group(5));
                int photoId = parseInt(matcher.group(6));
                return new PhotoLink(photoId, ownerId);
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parsePhotos(String string) {
        Matcher matcher = PATTERN_PHOTOS.matcher(string);
        try {
            if (matcher.find()) {
                return new PhotoAlbumsLink(parseInt(matcher.group(1)));
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static AbsLink parseProfileById(String string) {
        Matcher matcher = PATTERN_PROFILE_ID.matcher(string);
        try {
            if (matcher.find()) {
                return new OwnerLink(parseInt(matcher.group(2)));
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static AbsLink parseTopic(String string) {
        Matcher matcher = PATTERN_TOPIC.matcher(string);
        try {
            if (matcher.find()) {
                return new TopicLink(parseInt(matcher.group(2)), parseInt(matcher.group(1)));
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    private static AbsLink parseVideo(String string) {
        Matcher matcher = PATTERN_VIDEO.matcher(string);

        try {
            if (matcher.find()) {
                return new VideoLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
            }
        } catch (NumberFormatException ignored) {

        }

        return null;
    }

    private static AbsLink parseFave(String string) {
        Matcher matcherWithSection = PATTERN_FAVE_WITH_SECTION.matcher(string);
        Matcher matcher = PATTERN_FAVE.matcher(string);

        if (matcherWithSection.find()) {
            return new FaveLink(matcherWithSection.group(1));
        }

        if (matcher.find()) {
            return new FaveLink();
        }

        return null;
    }

    private static AbsLink parseAudios(String string) {
        Matcher matcher = PATTERN_AUDIOS.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new AudiosLink(parseInt(matcher.group(1)));
    }

    private static AbsLink parseDoc(String string) {
        Matcher matcher = PATTERN_DOC.matcher(string);

        try {
            if (matcher.find()) {
                return new DocLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
            }
        } catch (Exception ignored) {

        }

        return null;
    }

    private static AbsLink parseWall(String string) {
        Matcher matcher = PATTERN_WALL.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallLink(parseInt(matcher.group(1)));
    }

    private static AbsLink parseWallCommentLink(String string) {
        Matcher matcher = PATTERN_WALL_POST_COMMENT.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        WallCommentLink link = new WallCommentLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)), parseInt(matcher.group(3)));
        return link.isValid() ? link : null;
    }

    private static AbsLink parseWallPost(String string) {
        Matcher matcher = PATTERN_WALL_POST.matcher(string);
        if (!matcher.find()) {
            return null;
        }

        return new WallPostLink(parseInt(matcher.group(1)), parseInt(matcher.group(2)));
    }
}
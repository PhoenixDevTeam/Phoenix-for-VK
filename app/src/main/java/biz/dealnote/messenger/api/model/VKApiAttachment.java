package biz.dealnote.messenger.api.model;

/**
 * An abstract class for all attachments
 */
public interface VKApiAttachment {

    /**
     * Attachment is a photo.
     * @see {@link VKApiPhoto}
     */
    String TYPE_PHOTO = "photo";

    /**
     * Attachment is a video.
     * @see {@link VKApiVideo}
     */
    String TYPE_VIDEO = "video";

    /**
     * Attachment is an audio.
     * @see {@link VKApiAudio}
     */
    String TYPE_AUDIO = "audio";

    /**
     * Attachment is a document.
     * @see {@link VkApiDoc}
     */
    String TYPE_DOC = "doc";

    /**
     * Attachment is a wall post.
     * @see {@link VKApiPost}
     */
    String TYPE_POST = "wall";

    /**
     * Attachment is a posted photo.
     * @see {@link VKApiPostedPhoto}
     */
    String TYPE_POSTED_PHOTO = "posted_photo";

    /**
     * Attachment is a link
     * @see {@link VKApiLink}
     */
    String TYPE_LINK = "link";

    /**
     * Attachment is a note
     * @see {@link VKApiNote}
     */
    String TYPE_NOTE = "note";

    /**
     * Attachment is an application content
     * @see {@link VKApiApplicationContent}
     */
    String TYPE_APP = "app";

    /**
     * Attachment is a poll
     * @see {@link VKApiPoll}
     */
    String TYPE_POLL = "poll";

    /**
     * Attachment is a WikiPage
     * @see {@link VKApiWikiPage}
     */
    String TYPE_WIKI_PAGE = "page";

    /**
     * Attachment is a PhotoAlbum
     * @see {@link VKApiPhotoAlbum}
     */
    String TYPE_ALBUM = "album";

    /**
     * Attachment is a Sticker
     * @see {@link VKApiSticker}
     */
    String TYPE_STICKER = "sticker";

    /**
     * @return type of this attachment
     */
    String getType();
}
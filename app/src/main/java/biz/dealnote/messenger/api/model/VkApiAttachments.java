package biz.dealnote.messenger.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hp-dv6 on 27.05.2016.
 * VKMessenger
 */
public class VkApiAttachments {

    /**
     * Attachment is a photo.
     *
     * @see {@link VKApiPhoto}
     */
    public static final String TYPE_PHOTO = "photo";

    /**
     * Attachment is a video.
     *
     * @see {@link VKApiVideo}
     */
    public static final String TYPE_VIDEO = "video";

    /**
     * Attachment is an audio.
     *
     * @see {@link VKApiAudio}
     */
    public static final String TYPE_AUDIO = "audio";

    /**
     * Attachment is a document.
     *
     * @see {@link VkApiDoc}
     */
    public static final String TYPE_DOC = "doc";

    /**
     * Attachment is a wall post.
     *
     * @see {@link VKApiPost}
     */
    public static final String TYPE_POST = "wall";

    /**
     * Attachment is a link
     *
     * @see {@link VKApiLink}
     */
    public static final String TYPE_LINK = "link";

    /**
     * Attachment is a note
     *
     * @see {@link VKApiNote}
     */
    public static final String TYPE_NOTE = "note";

    /**
     * Attachment is an application content
     *
     * @see {@link VKApiApplicationContent}
     */
    public static final String TYPE_APP = "app";

    /**
     * Attachment is a poll
     *
     * @see {@link VKApiPoll}
     */
    public static final String TYPE_POLL = "poll";

    /**
     * Attachment is a WikiPage
     *
     * @see {@link VKApiWikiPage}
     */
    public static final String TYPE_WIKI_PAGE = "page";

    /**
     * Attachment is a PhotoAlbum
     *
     * @see {@link VKApiPhotoAlbum}
     */
    public static final String TYPE_ALBUM = "album";

    /**
     * Attachment is a Sticker
     *
     * @see {@link VKApiSticker}
     */
    public static final String TYPE_STICKER = "sticker";

    public ArrayList<Entry> entries;

    public VkApiAttachments() {
        this.entries = new ArrayList<>(1);
    }

    public VkApiAttachments(int initialSize) {
        this.entries = new ArrayList<>(initialSize);
    }

    public List<Entry> entryList() {
        return Collections.unmodifiableList(entries);
    }

    public void append(VKApiAttachment attachment){
        if(entries == null){
            entries = new ArrayList<>(1);
        }

        entries.add(new Entry(attachment.getType(), attachment));
    }

    public void clear(){
        if(entries != null){
            entries.clear();
        }
    }

    public void append(List<? extends VKApiAttachment> data){
        for(VKApiAttachment attachment : data){
            append(attachment);
        }
    }

    public static class Entry {

        public final String type;
        public final VKApiAttachment attachment;

        public Entry(String type, VKApiAttachment attachment) {
            this.type = type;
            this.attachment = attachment;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean nonEmpty(){
        return size() > 0;
    }

    public int size() {
        return entries == null ? 0 : entries.size();
    }
}
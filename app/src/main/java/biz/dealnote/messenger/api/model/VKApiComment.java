package biz.dealnote.messenger.api.model;

/**
 * Comment object describes a comment.
 */
public class VKApiComment {

    /**
     * Comment ID, positive number
     */
    public int id;

    /**
     * Comment author ID.
     */
    public int from_id;

    /**
     * Date when the comment was added as unixtime.
     */
    public long date;

    /**
     * Text of the comment
     */
    public String text;

    /**
     * ID of the user or community to whom the reply is addressed (if the comment is a reply to another comment).
     */
    public int reply_to_user;

    /**
     * ID of the comment the reply to which is represented by the current comment (if the comment is a reply to another comment).
     */
    public int reply_to_comment;

    /**
     * Number of likes on the comment.
     */
    public int likes;

    /**
     * Information whether the current user liked the comment.
     */
    public boolean user_likes;

    /**
     * Whether the current user can like on the comment.
     */
    public boolean can_like;

    public boolean can_edit;

    /**
     * Information about attachments in the comments (photos, links, etc.;)
     */
    public VkApiAttachments attachments;

    /**
     * Creates empty Comment instance.
     */
    public VKApiComment() {

    }

    public int getAttachmentsCount(){
        return attachments == null ? 0 : attachments.size();
    }

    public boolean hasAttachments(){
        return getAttachmentsCount() > 0;
    }
}
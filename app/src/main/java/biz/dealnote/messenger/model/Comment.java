package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

public class Comment extends AbsModel implements Parcelable, Identificable {

    /**
     * идентификатор комментария.
     */
    private int id;

    /**
     * идентификатор автора комментария.
     */
    private int fromId;

    /**
     * дата создания комментария в формате unixtime.
     */
    private long date;

    /**
     * текст комментария
     */
    private String text;

    /**
     * идентификатор пользователя или сообщества, в ответ которому оставлен текущий комментарий (если применимо).
     */
    private int replyToUser;

    /**
     * идентификатор комментария, в ответ на который оставлен текущий (если применимо).
     */
    private int replyToComment;

    /**
     * Number of likes on the comment.
     */
    private int likesCount;

    /**
     * Information whether the current user liked the comment.
     */
    private boolean userLikes;

    /**
     * Whether the current user can like on the comment.
     */
    private boolean canLike;

    private boolean canEdit;

    /**
     * объект, содержащий информацию о медиавложениях в комментарии
     */
    private Attachments attachments;

    private final Commented commented;

    private Owner author;

    private int dbid;

    private boolean deleted;

    //not parcelable
    private boolean animationNow;

    public Comment(Commented commented) {
        this.commented = commented;
    }

    protected Comment(Parcel in) {
        super(in);
        id = in.readInt();
        fromId = in.readInt();
        date = in.readLong();
        text = in.readString();
        replyToUser = in.readInt();
        replyToComment = in.readInt();
        likesCount = in.readInt();
        userLikes = in.readByte() != 0;
        canLike = in.readByte() != 0;
        canEdit = in.readByte() != 0;
        attachments = in.readParcelable(Attachments.class.getClassLoader());
        commented = in.readParcelable(Commented.class.getClassLoader());
        author = in.readParcelable(fromId > 0 ? User.class.getClassLoader() : Community.class.getClassLoader());
        dbid = in.readInt();
        deleted = in.readByte() != 0;
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };

    @Override
    public int getId() {
        return id;
    }

    public Comment setId(int id) {
        this.id = id;
        return this;
    }

    public int getFromId() {
        return fromId;
    }

    public Comment setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Comment setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public Comment setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyToUser() {
        return replyToUser;
    }

    public Comment setReplyToUser(int replyToUser) {
        this.replyToUser = replyToUser;
        return this;
    }

    public int getReplyToComment() {
        return replyToComment;
    }

    public Comment setReplyToComment(int replyToComment) {
        this.replyToComment = replyToComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public Comment setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public Comment setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public Comment setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public Comment setAttachments(Attachments attachments) {
        this.attachments = attachments;
        return this;
    }

    public Commented getCommented() {
        return commented;
    }

    public Owner getAuthor() {
        return author;
    }

    public Comment setAuthor(Owner author) {
        this.author = author;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Comment setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isAnimationNow() {
        return animationNow;
    }

    public Comment setAnimationNow(boolean animationNow) {
        this.animationNow = animationNow;
        return this;
    }

    public boolean hasAttachments(){
        return attachments != null && !attachments.isEmpty();
    }

    public boolean hasStickerOnly(){
        return attachments != null && !Utils.safeIsEmpty(attachments.getStickers());
    }

    public String getFullAuthorName(){
        return author == null ? null : author.getFullName();
    }

    public String getMaxAuthorAvaUrl(){
        return author == null ? null : author.getMaxSquareAvatar();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Comment setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(fromId);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeInt(replyToUser);
        dest.writeInt(replyToComment);
        dest.writeInt(likesCount);
        dest.writeByte((byte) (userLikes ? 1 : 0));
        dest.writeByte((byte) (canLike ? 1 : 0));
        dest.writeByte((byte) (canEdit ? 1 : 0));
        dest.writeParcelable(attachments, flags);
        dest.writeParcelable(commented, flags);
        dest.writeParcelable(author, flags);
        dest.writeInt(dbid);
        dest.writeByte((byte) (deleted ? 1 : 0));
    }

    public int getAttachmentsCount() {
        return Objects.isNull(attachments) ? 0 : attachments.size();
    }
}

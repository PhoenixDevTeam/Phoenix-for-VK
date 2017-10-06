package biz.dealnote.messenger.db.model.entity;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.util.AssertUtils;

/**
 * Created by Ruslan Kolbasa on 06.09.2017.
 * phoenix
 */
public class CommentEntity {

    private final int sourceId;

    private final int sourceOwnerId;

    private final int sourceType;

    private final String sourceAccessKey;

    private final int id;

    private int fromId;

    private long date;

    private String text;

    private int replyToUserId;

    private int replyToComment;

    private int likesCount;

    private boolean userLikes;

    private boolean canLike;

    private boolean canEdit;

    private boolean deleted;

    private int attachmentsCount;

    private AttachmentsEntity attachments;

    public CommentEntity(int sourceId, int sourceOwnerId, int sourceType, String sourceAccessKey, int id) {
        this.sourceId = sourceId;
        this.sourceOwnerId = sourceOwnerId;
        this.sourceType = sourceType;
        this.id = id;
        this.sourceAccessKey = sourceAccessKey;
        this.attachments = new AttachmentsEntity(Collections.emptyList());
    }

    public int getId() {
        return id;
    }

    public int getFromId() {
        return fromId;
    }

    public CommentEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public CommentEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public CommentEntity setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyToUserId() {
        return replyToUserId;
    }

    public CommentEntity setReplyToUserId(int replyToUserId) {
        this.replyToUserId = replyToUserId;
        return this;
    }

    public int getReplyToComment() {
        return replyToComment;
    }

    public CommentEntity setReplyToComment(int replyToComment) {
        this.replyToComment = replyToComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public CommentEntity setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public CommentEntity setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public CommentEntity setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public CommentEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public CommentEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public CommentEntity setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
        return this;
    }

    public List<Entity> getAttachments() {
        return attachments.getEntities();
    }

    public CommentEntity setAttachments(List<Entity> entities) {
        AssertUtils.requireNonNull(entities, "Entities can't bee null");

        this.attachments = new AttachmentsEntity(entities);
        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getSourceOwnerId() {
        return sourceOwnerId;
    }

    public int getSourceType() {
        return sourceType;
    }

    public String getSourceAccessKey() {
        return sourceAccessKey;
    }
}
package biz.dealnote.messenger.db.model.entity;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.util.AssertUtils;
import io.reactivex.annotations.NonNull;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class PostEntity extends Entity {

    private static final int NO_STORED = -1;

    private int dbid = NO_STORED;

    private final int id;

    private final int ownerId;

    private int fromId;

    private long date;

    private String text;

    private int replyOwnerId;

    private int replyPostId;

    private boolean friendsOnly;

    private int commentsCount;

    private boolean canPostComment;

    private int likesCount;

    private boolean userLikes;

    private boolean canLike;

    private boolean canEdit;

    private boolean canPublish;

    private int repostCount;

    private boolean userReposted;

    private int postType;

    private int attachmentsCount;

    private int signedId;

    private int createdBy;

    private boolean canPin;

    private boolean pinned;

    private boolean deleted;

    private int views;

    private SourceDbo source;

    private AttachmentsEntity attachments;

    private List<PostEntity> copyHierarchy;

    public boolean isCanPublish() {
        return canPublish;
    }

    public PostEntity setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
        return this;
    }

    public PostEntity setDbid(int dbid) {
        this.dbid = dbid;
        return this;
    }

    public int getDbid() {
        return dbid;
    }

    public static final class SourceDbo {

        private final int type;

        private final String platform;

        private final int data;

        private final String url;

        public SourceDbo(int type, String platform, int data, String url) {
            this.type = type;
            this.platform = platform;
            this.data = data;
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public int getType() {
            return type;
        }

        public int getData() {
            return data;
        }

        public String getPlatform() {
            return platform;
        }
    }

    public PostEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        this.attachments = new AttachmentsEntity(Collections.emptyList());
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getFromId() {
        return fromId;
    }

    public PostEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public PostEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public PostEntity setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyOwnerId() {
        return replyOwnerId;
    }

    public PostEntity setReplyOwnerId(int replyOwnerId) {
        this.replyOwnerId = replyOwnerId;
        return this;
    }

    public int getReplyPostId() {
        return replyPostId;
    }

    public PostEntity setReplyPostId(int replyPostId) {
        this.replyPostId = replyPostId;
        return this;
    }

    public boolean isFriendsOnly() {
        return friendsOnly;
    }

    public PostEntity setFriendsOnly(boolean friendsOnly) {
        this.friendsOnly = friendsOnly;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public PostEntity setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public boolean isCanPostComment() {
        return canPostComment;
    }

    public PostEntity setCanPostComment(boolean canPostComment) {
        this.canPostComment = canPostComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public PostEntity setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public PostEntity setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public PostEntity setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public PostEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public PostEntity setRepostCount(int repostCount) {
        this.repostCount = repostCount;
        return this;
    }

    public boolean isUserReposted() {
        return userReposted;
    }

    public PostEntity setUserReposted(boolean userReposted) {
        this.userReposted = userReposted;
        return this;
    }

    public int getPostType() {
        return postType;
    }

    public PostEntity setPostType(int postType) {
        this.postType = postType;
        return this;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public PostEntity setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
        return this;
    }

    public int getSignedId() {
        return signedId;
    }

    public PostEntity setSignedId(int signedId) {
        this.signedId = signedId;
        return this;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public PostEntity setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public boolean isCanPin() {
        return canPin;
    }

    public PostEntity setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public PostEntity setCanPin(boolean canPin) {
        this.canPin = canPin;
        return this;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public PostEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getViews() {
        return views;
    }

    public PostEntity setViews(int views) {
        this.views = views;
        return this;
    }

    public SourceDbo getSource() {
        return source;
    }

    public PostEntity setSource(SourceDbo source) {
        this.source = source;
        return this;
    }

    @NonNull
    public List<Entity> getAttachments() {
        return attachments.getEntities();
    }

    public PostEntity setAttachments(@NonNull List<Entity> entities) {
        AssertUtils.requireNonNull(entities);

        this.attachments = new AttachmentsEntity(entities);
        return this;
    }

    public List<PostEntity> getCopyHierarchy() {
        return copyHierarchy;
    }

    public PostEntity setCopyHierarchy(List<PostEntity> copyHierarchy) {
        this.copyHierarchy = copyHierarchy;
        return this;
    }
}
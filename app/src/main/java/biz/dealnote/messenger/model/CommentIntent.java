package biz.dealnote.messenger.model;

import java.util.List;

/**
 * Created by admin on 07.06.2017.
 * phoenix
 */
public class CommentIntent {

    private String message;

    private Integer replyToComment;

    private Integer draftMessageId;

    private final int authorId;

    private Integer stickerId;

    private List<AbsModel> models;

    public CommentIntent(int authorId) {
        this.authorId = authorId;
    }

    public CommentIntent setModels(List<AbsModel> models) {
        this.models = models;
        return this;
    }

    public List<AbsModel> getModels() {
        return models;
    }

    public Integer getDraftMessageId() {
        return draftMessageId;
    }

    public CommentIntent setMessage(String message) {
        this.message = message;
        return this;
    }

    public CommentIntent setReplyToComment(Integer replyToComment) {
        this.replyToComment = replyToComment;
        return this;
    }

    public CommentIntent setDraftMessageId(Integer draftMessageId) {
        this.draftMessageId = draftMessageId;
        return this;
    }

    public CommentIntent setStickerId(Integer stickerId) {
        this.stickerId = stickerId;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Integer getReplyToComment() {
        return replyToComment;
    }

    public Integer getStickerId() {
        return stickerId;
    }

    public String getMessage() {
        return message;
    }
}
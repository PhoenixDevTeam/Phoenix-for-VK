package biz.dealnote.messenger.api.adapters;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.Copyable;
import biz.dealnote.messenger.api.model.Likeable;
import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.feedback.Copies;
import biz.dealnote.messenger.api.model.feedback.UserArray;
import biz.dealnote.messenger.api.model.feedback.VkApiBaseFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiCommentFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiCopyFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiLikeCommentFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiLikeFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiMentionCommentFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiMentionWallFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiReplyCommentFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiUsersFeedback;
import biz.dealnote.messenger.api.model.feedback.VkApiWallFeedback;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by ruslan.kolbasa on 28.12.2016.
 * phoenix
 */
public class FeedbackDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiBaseFeedback> {

    private static final BaseMentionCommentParser MENTION_COMMENT_PARSER = new BaseMentionCommentParser();
    private static final BaseLikeCommentParser LIKE_COMMENT_PARSER = new BaseLikeCommentParser();
    private static final BaseCopyParser COPY_PARSER = new BaseCopyParser();
    private static final BaseCreateCommentParser CREATE_COMMENT_PARSER = new BaseCreateCommentParser();
    private static final BaseReplyCommentParser REPLY_COMMENT_PARSER = new BaseReplyCommentParser();
    private static final LikeParser LIKE_PARSER = new LikeParser();
    private static final BaseUsersParser USERS_PARSER = new BaseUsersParser();

    @SuppressWarnings("unchecked")
    @Override
    public VkApiBaseFeedback deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        String type = optString(root, "type");
        VkApiBaseFeedback dto = createInstance(type);

        if (Objects.nonNull(dto)) {
            Parser parser = getParser(type);
            parser.parse(root, dto, context);
        }

        return dto;
    }

    private static VkApiBaseFeedback createInstance(String type) {
        switch (type) {
            case "follow":
            case "friend_accepted":
                return new VkApiUsersFeedback();

            case "mention":
                return new VkApiMentionWallFeedback();

            case "mention_comments":
            case "mention_comment_photo":
            case "mention_comment_video":
                return new VkApiMentionCommentFeedback();

            case "wall":
            case "wall_publish":
                return new VkApiWallFeedback();

            case "comment_post":
            case "comment_photo":
            case "comment_video":
                return new VkApiCommentFeedback();

            case "reply_comment":
            case "reply_comment_photo":
            case "reply_comment_video":
            case "reply_topic":
                return new VkApiReplyCommentFeedback();

            case "like_photo":
            case "like_post":
            case "like_video":
                return new VkApiLikeFeedback();

            case "like_comment":
            case "like_comment_photo":
            case "like_comment_video":
            case "like_comment_topic":
                return new VkApiLikeCommentFeedback();

            case "copy_post":
            case "copy_photo":
            case "copy_video":
                return new VkApiCopyFeedback();
        }

        return null;
    }

    private Parser<?> getParser(@NonNull String type) {
        switch (type) {
            case "follow":
            case "friend_accepted":
                return USERS_PARSER;

            case "mention":
                return new Parser<VkApiMentionWallFeedback>() {
                    @Override
                    void parse(JsonObject root, VkApiMentionWallFeedback dto, JsonDeserializationContext context) {
                        super.parse(root, dto, context);
                        dto.post = context.deserialize(root.get("feedback"), VKApiPost.class);
                    }
                };

            case "wall":
            case "wall_publish":
                return new Parser<VkApiWallFeedback>() {
                    @Override
                    void parse(JsonObject root, VkApiWallFeedback dto, JsonDeserializationContext context) {
                        super.parse(root, dto, context);
                        dto.post = context.deserialize(root.get("feedback"), VKApiPost.class);
                    }
                };

            case "comment_photo":
            case "comment_post":
            case "comment_video":
                return CREATE_COMMENT_PARSER;

            case "reply_comment":
            case "reply_comment_photo":
            case "reply_comment_video":
            case "reply_topic":
               return REPLY_COMMENT_PARSER;

            case "like_video":
            case "like_photo":
            case "like_post":
                return LIKE_PARSER;

            case "like_comment_photo":
            case "like_comment_video":
            case "like_comment_topic":
            case "like_comment":
                return LIKE_COMMENT_PARSER;

            case "copy_post":
            case "copy_photo":
                return COPY_PARSER;

            case "mention_comment_photo":
            case "mention_comment_video":
            case "mention_comments":
                return MENTION_COMMENT_PARSER;

            default:
                throw new UnsupportedOperationException();
        }
    }

    private static class Parser<T extends VkApiBaseFeedback> {
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            dto.type = optString(root, "type");
            dto.date = optLong(root, "date");

            if (root.has("reply")) {
                dto.reply = context.deserialize(root.get("reply"), VKApiComment.class);
            }
        }
    }

    private static class BaseCopyParser<T extends VkApiCopyFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);
            dto.copies = context.deserialize(root.get("feedback"), Copies.class);

            Class<? extends Copyable> copyClass;

            switch (dto.type) {
                case "copy_post":
                    copyClass = VKApiPost.class;
                    break;
                case "copy_photo":
                    copyClass = VKApiPhoto.class;
                    break;
                case "copy_video":
                    copyClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.what = context.deserialize(root.get("parent"), copyClass);
        }
    }

    private static class BaseCreateCommentParser<T extends VkApiCommentFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);
            dto.comment = context.deserialize(root.get("feedback"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;
            switch (dto.type){
                case "comment_post":
                    commentableClass = VKApiPost.class;
                    break;
                case "comment_photo":
                    commentableClass = VKApiPhoto.class;
                    break;
                case "comment_video":
                    commentableClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comment_of = context.deserialize(root.get("parent"), commentableClass);
        }
    }

    private static class BaseReplyCommentParser<T extends VkApiReplyCommentFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);
            dto.feedback_comment = context.deserialize(root.get("feedback"), VKApiComment.class);

            if("reply_topic".equals(dto.type)){
                dto.own_comment = null;
                dto.comments_of = context.deserialize(root.get("parent"), VKApiTopic.class);
                return;
            }

            dto.own_comment = context.deserialize(root.get("parent"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;
            String parentCommentableField;

            switch (dto.type){
                case "reply_comment":
                    commentableClass = VKApiPost.class;
                    parentCommentableField = "post";
                    break;
                case "reply_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    parentCommentableField = "photo";
                    break;
                case "reply_comment_video":
                    commentableClass = VKApiVideo.class;
                    parentCommentableField = "video";
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comments_of = context.deserialize(root.getAsJsonObject("parent").get(parentCommentableField), commentableClass);
        }
    }

    private static class BaseUsersParser<T extends VkApiUsersFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);
            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
        }
    }

    private static class LikeParser<T extends VkApiLikeFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);

            Class<? extends Likeable> likedClass;
            switch (dto.type){
                case "like_photo":
                    likedClass = VKApiPhoto.class;
                    break;
                case "like_post":
                    likedClass = VKApiPost.class;
                    break;
                case "like_video":
                    likedClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.liked = context.deserialize(root.get("parent"), likedClass);
            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
        }
    }

    private static class BaseLikeCommentParser<T extends VkApiLikeCommentFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);

            Class<? extends Commentable> commentableClass;
            String parentJsonField;

            switch (dto.type) {
                case "like_comment":
                    commentableClass = VKApiPost.class;
                    parentJsonField = "post";
                    break;
                case "like_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    parentJsonField = "photo";
                    break;
                case "like_comment_video":
                    commentableClass = VKApiVideo.class;
                    parentJsonField = "video";
                    break;
                case "like_comment_topic":
                    commentableClass = VKApiTopic.class;
                    parentJsonField = "topic";
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.users = context.deserialize(root.get("feedback"), UserArray.class);
            dto.comment = context.deserialize(root.get("parent"), VKApiComment.class);
            dto.commented = context.deserialize(root.getAsJsonObject("parent").get(parentJsonField), commentableClass);
        }
    }

    private static class BaseMentionCommentParser<T extends VkApiMentionCommentFeedback> extends Parser<T> {
        @Override
        void parse(JsonObject root, T dto, JsonDeserializationContext context) {
            super.parse(root, dto, context);
            dto.where = context.deserialize(root.get("feedback"), VKApiComment.class);

            Class<? extends Commentable> commentableClass;

            switch (dto.type) {
                case "mention_comments":
                    commentableClass = VKApiPost.class;
                    break;
                case "mention_comment_photo":
                    commentableClass = VKApiPhoto.class;
                    break;
                case "mention_comment_video":
                    commentableClass = VKApiVideo.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported feedback type: " + dto.type);
            }

            dto.comment_of = context.deserialize(root.get("parent"), commentableClass);
        }
    }
}
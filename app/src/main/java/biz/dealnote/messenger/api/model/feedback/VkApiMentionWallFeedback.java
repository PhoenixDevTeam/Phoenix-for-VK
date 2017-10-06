package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.VKApiPost;

/**
 * Base class for types [mention_comments, mention, mention_comment_photo, mention_comment_video]
 */
public class VkApiMentionWallFeedback extends VkApiBaseFeedback {

    public VKApiPost post;

}

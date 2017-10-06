package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.VKApiComment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class VkApiReplyCommentFeedback extends VkApiBaseFeedback {

    public Commentable comments_of;

    public VKApiComment own_comment;

    public VKApiComment feedback_comment;
}

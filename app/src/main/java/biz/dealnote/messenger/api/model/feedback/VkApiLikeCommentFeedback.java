package biz.dealnote.messenger.api.model.feedback;

import biz.dealnote.messenger.api.model.Commentable;
import biz.dealnote.messenger.api.model.VKApiComment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class VkApiLikeCommentFeedback extends VkApiBaseFeedback {

    public UserArray users;

    public VKApiComment comment;

    public Commentable commented;
}

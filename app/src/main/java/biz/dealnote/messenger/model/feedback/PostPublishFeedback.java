package biz.dealnote.messenger.model.feedback;

import biz.dealnote.messenger.model.Post;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public class PostPublishFeedback extends Feedback {

    private Post post;

    public PostPublishFeedback(@FeedbackType int type) {
        super(type);
    }

    public PostPublishFeedback setPost(Post post) {
        this.post = post;
        return this;
    }

    public Post getPost() {
        return post;
    }
}
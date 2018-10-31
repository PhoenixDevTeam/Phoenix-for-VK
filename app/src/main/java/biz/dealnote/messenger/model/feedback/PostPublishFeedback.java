package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;

import biz.dealnote.messenger.model.Post;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public final class PostPublishFeedback extends Feedback implements Parcelable {

    private Post post;

    public PostPublishFeedback(@FeedbackType int type) {
        super(type);
    }

    private PostPublishFeedback(Parcel in) {
        super(in);
        post = in.readParcelable(Post.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(post, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PostPublishFeedback> CREATOR = new Creator<PostPublishFeedback>() {
        @Override
        public PostPublishFeedback createFromParcel(Parcel in) {
            return new PostPublishFeedback(in);
        }

        @Override
        public PostPublishFeedback[] newArray(int size) {
            return new PostPublishFeedback[size];
        }
    };

    public PostPublishFeedback setPost(Post post) {
        this.post = post;
        return this;
    }

    public Post getPost() {
        return post;
    }
}
package biz.dealnote.messenger.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by admin on 07.05.2017.
 * phoenix
 */
public class NewsfeedComment {

    private final Object model;

    private Comment comment;

    public NewsfeedComment(Object model) {
        this.model = model;
    }

    public NewsfeedComment setComment(Comment comment) {
        this.comment = comment;
        return this;
    }

    /**
     * @return Photo, Video, Topic or Post
     */
    @NonNull
    public Object getModel() {
        return model;
    }

    @Nullable
    public Comment getComment() {
        return comment;
    }
}

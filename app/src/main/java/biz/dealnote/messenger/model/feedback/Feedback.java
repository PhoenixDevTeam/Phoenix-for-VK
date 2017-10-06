package biz.dealnote.messenger.model.feedback;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Comment;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 */
public abstract class Feedback extends AbsModel implements Parcelable {

    @FeedbackType
    private final int type;

    private long date;

    private Comment reply;

    public Feedback(@FeedbackType int type) {
        this.type = type;
    }

    public final int getType() {
        return type;
    }

    public final long getDate() {
        return date;
    }

    public final Feedback setDate(long date) {
        this.date = date;
        return this;
    }

    public final Comment getReply() {
        return reply;
    }

    public final Feedback setReply(Comment reply) {
        this.reply = reply;
        return this;
    }

    protected Feedback(Parcel in) {
        super(in);
        //noinspection ResourceType
        type = in.readInt();
        date = in.readLong();
        reply = in.readParcelable(Comment.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @CallSuper
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(type);
        dest.writeLong(date);
        dest.writeParcelable(reply, flags);
    }
}
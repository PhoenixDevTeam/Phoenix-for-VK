package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 19.04.2017.
 * phoenix
 */
public final class ParcelableModelWrapper implements Parcelable {

    private static final List<Class> TYPES = new ArrayList<>();
    static {
        TYPES.add(Photo.class);
        TYPES.add(Post.class);
        TYPES.add(Video.class);
        TYPES.add(FwdMessages.class);
        TYPES.add(VoiceMessage.class);
        TYPES.add(Document.class);
        TYPES.add(Audio.class);
        TYPES.add(Chat.class);
        TYPES.add(Poll.class);
        TYPES.add(Link.class);
        TYPES.add(Comment.class);
    }

    private final AbsModel model;

    public static ParcelableModelWrapper wrap(AbsModel model){
        return new ParcelableModelWrapper(model);
    }

    public static AbsModel readModel(Parcel in){
        return in.<ParcelableModelWrapper>readParcelable(ParcelableModelWrapper.class.getClassLoader()).get();
    }

    public static void writeModel(Parcel dest, int flags, AbsModel owner){
        dest.writeParcelable(new ParcelableModelWrapper(owner), flags);
    }

    private ParcelableModelWrapper(AbsModel model) {
        this.model = model;
    }

    private ParcelableModelWrapper(Parcel in) {
        int index = in.readInt();
        ClassLoader classLoader = TYPES.get(index).getClassLoader();

        model = in.readParcelable(classLoader);
    }

    public static final Creator<ParcelableModelWrapper> CREATOR = new Creator<ParcelableModelWrapper>() {
        @Override
        public ParcelableModelWrapper createFromParcel(Parcel in) {
            return new ParcelableModelWrapper(in);
        }

        @Override
        public ParcelableModelWrapper[] newArray(int size) {
            return new ParcelableModelWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int index = TYPES.indexOf(model.getClass());
        if(index == -1){
            throw new UnsupportedOperationException("Unsupported class: " + model.getClass());
        }

        dest.writeInt(index);
        dest.writeParcelable(model, flags);
    }

    public AbsModel get() {
        return model;
    }
}

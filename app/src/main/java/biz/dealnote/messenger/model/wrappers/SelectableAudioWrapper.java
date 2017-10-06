package biz.dealnote.messenger.model.wrappers;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Selectable;

public class SelectableAudioWrapper implements Parcelable, Selectable {

    private final Audio audio;
    private boolean selected;

    public SelectableAudioWrapper(@NonNull Audio audio){
        this.audio = audio;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @NonNull
    public Audio getAudio() {
        return audio;
    }

    protected SelectableAudioWrapper(Parcel in) {
        audio = in.readParcelable(Audio.class.getClassLoader());
        selected = in.readByte() != 0;
    }

    public static final Creator<SelectableAudioWrapper> CREATOR = new Creator<SelectableAudioWrapper>() {
        @Override
        public SelectableAudioWrapper createFromParcel(Parcel in) {
            return new SelectableAudioWrapper(in);
        }

        @Override
        public SelectableAudioWrapper[] newArray(int size) {
            return new SelectableAudioWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(audio, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
    }
}

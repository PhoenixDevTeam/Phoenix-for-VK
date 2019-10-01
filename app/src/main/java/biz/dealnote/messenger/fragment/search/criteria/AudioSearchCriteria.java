package biz.dealnote.messenger.fragment.search.criteria;

import android.os.Parcel;

public class AudioSearchCriteria extends BaseSearchCriteria {
    public static final Creator<AudioSearchCriteria> CREATOR = new Creator<AudioSearchCriteria>() {
        @Override
        public AudioSearchCriteria createFromParcel(Parcel in) {
            return new AudioSearchCriteria(in);
        }

        @Override
        public AudioSearchCriteria[] newArray(int size) {
            return new AudioSearchCriteria[size];
        }
    };
    private final boolean own;

    public AudioSearchCriteria(String query, boolean own) {
        super(query);
        this.own = own;
    }

    public AudioSearchCriteria(String query, int optionsCount, boolean own) {
        super(query, optionsCount);
        this.own = own;
    }

    protected AudioSearchCriteria(Parcel in) {
        super(in);
        own = in.readInt() == 1;
    }

    public boolean isOwn() {
        return own;
    }

    @Override
    public AudioSearchCriteria clone() throws CloneNotSupportedException {
        return (AudioSearchCriteria) super.clone();
    }
}

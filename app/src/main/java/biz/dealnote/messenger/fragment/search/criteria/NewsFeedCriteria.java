package biz.dealnote.messenger.fragment.search.criteria;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hp-dv6 on 08.06.2016 with Core i7 2670QM.
 * VKMessenger
 */
public final class NewsFeedCriteria extends BaseSearchCriteria implements Parcelable {

    public NewsFeedCriteria(String query) {
        super(query);
    }

    private NewsFeedCriteria(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<NewsFeedCriteria> CREATOR = new Creator<NewsFeedCriteria>() {
        @Override
        public NewsFeedCriteria createFromParcel(Parcel in) {
            return new NewsFeedCriteria(in);
        }

        @Override
        public NewsFeedCriteria[] newArray(int size) {
            return new NewsFeedCriteria[size];
        }
    };
}

package biz.dealnote.messenger.fragment.search.criteria;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.search.options.DatabaseOption;
import biz.dealnote.messenger.fragment.search.options.SimpleBooleanOption;
import biz.dealnote.messenger.fragment.search.options.SpinnerOption;

public class GroupSearchCriteria extends BaseSearchCriteria implements Parcelable, Cloneable {

    public static final int KEY_TYPE = 1;
    public static final int KEY_COUNTRY = 2;
    public static final int KEY_CITY = 3;
    public static final int KEY_SORT = 4;
    public static final int KEY_FUTURE_ONLY = 5;

    public static final int TYPE_PAGE = 1;
    public static final int TYPE_GROUP = 2;
    public static final int TYPE_EVENT = 3;

    public GroupSearchCriteria(String query) {
        super(query, 5);

        SpinnerOption type = new SpinnerOption(KEY_TYPE, R.string.type, true);
        type.available = new ArrayList<>(3);
        type.available.add(new SpinnerOption.Entry(TYPE_PAGE, R.string.page));
        type.available.add(new SpinnerOption.Entry(TYPE_GROUP, R.string.group));
        type.available.add(new SpinnerOption.Entry(TYPE_EVENT, R.string.event));
        appendOption(type);

        DatabaseOption country = new DatabaseOption(KEY_COUNTRY, R.string.country, true, DatabaseOption.TYPE_COUNTRY);
        country.setChildDependencies(KEY_CITY);
        appendOption(country);

        DatabaseOption city = new DatabaseOption(KEY_CITY, R.string.city, true, DatabaseOption.TYPE_CITY);
        city.setDependencyOf(KEY_COUNTRY);
        appendOption(city);

        SpinnerOption sort = new SpinnerOption(KEY_SORT, R.string.sorting, true);
        sort.available = new ArrayList<>(6);
        sort.available.add(new SpinnerOption.Entry(0, R.string.default_sorting));
        sort.available.add(new SpinnerOption.Entry(1, R.string.sorting_by_growth_speed));
        sort.available.add(new SpinnerOption.Entry(2, R.string.sorting_by_day_attendance_members_number_ratio));
        sort.available.add(new SpinnerOption.Entry(3, R.string.sorting_by_likes_number_members_number_ratio));
        sort.available.add(new SpinnerOption.Entry(4, R.string.sorting_by_comments_number_members_number_ratio));
        sort.available.add(new SpinnerOption.Entry(5, R.string.sorting_by_boards_entries_number_members_number_ratio));
        appendOption(sort);

        SimpleBooleanOption futureOnly = new SimpleBooleanOption(KEY_FUTURE_ONLY, R.string.future_events_only, true);
        appendOption(futureOnly);
    }

    private GroupSearchCriteria(Parcel in) {
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

    public static final Creator<GroupSearchCriteria> CREATOR = new Creator<GroupSearchCriteria>() {
        @Override
        public GroupSearchCriteria createFromParcel(Parcel in) {
            return new GroupSearchCriteria(in);
        }

        @Override
        public GroupSearchCriteria[] newArray(int size) {
            return new GroupSearchCriteria[size];
        }
    };

    @Override
    public GroupSearchCriteria clone() throws CloneNotSupportedException {
        return (GroupSearchCriteria) super.clone();
    }
}

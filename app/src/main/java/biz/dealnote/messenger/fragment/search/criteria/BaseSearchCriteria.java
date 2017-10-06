package biz.dealnote.messenger.fragment.search.criteria;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import biz.dealnote.messenger.fragment.search.options.BaseOption;
import biz.dealnote.messenger.fragment.search.options.DatabaseOption;
import biz.dealnote.messenger.fragment.search.options.SimpleBooleanOption;
import biz.dealnote.messenger.fragment.search.options.SimpleNumberOption;
import biz.dealnote.messenger.fragment.search.options.SimpleTextOption;
import biz.dealnote.messenger.fragment.search.options.SpinnerOption;

public class BaseSearchCriteria implements Parcelable, Cloneable {

    private String query;
    private ArrayList<BaseOption> options;

    public BaseSearchCriteria(String query) {
        this(query, 0);
    }

    public BaseSearchCriteria(String query, int optionsCount) {
        this.query = query;
        this.options = new ArrayList<>(optionsCount);
    }

    public static final Creator<BaseSearchCriteria> CREATOR = new Creator<BaseSearchCriteria>() {
        @Override
        public BaseSearchCriteria createFromParcel(Parcel in) {
            return new BaseSearchCriteria(in);
        }

        @Override
        public BaseSearchCriteria[] newArray(int size) {
            return new BaseSearchCriteria[size];
        }
    };

    void appendOption(BaseOption option) {
        this.options.add(option);
    }

    public String getQuery() {
        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseSearchCriteria that = (BaseSearchCriteria) o;

        return (query != null ? query.equals(that.query) : that.query == null)
                && (options != null ? options.equals(that.options) : that.options == null);
    }

    @Override
    public int hashCode() {
        int result = query != null ? query.hashCode() : 0;
        result = 31 * result + (options != null ? options.hashCode() : 0);
        return result;
    }

    @Override
    protected BaseSearchCriteria clone() throws CloneNotSupportedException {
        BaseSearchCriteria clone = (BaseSearchCriteria) super.clone();
        clone.options = new ArrayList<>(this.options.size());

        for (BaseOption option : this.options) {
            clone.options.add(option.clone());
        }

        return clone;
    }

    public BaseSearchCriteria safellyClone() {
        try {
            return clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException("Unable to clone criteria");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);

        dest.writeInt(options.size());

        for (BaseOption option : options) {
            dest.writeInt(option.optionType);
            dest.writeParcelable(option, flags);
        }
    }

    protected BaseSearchCriteria(Parcel in) {
        this.query = in.readString();

        int optionsSize = in.readInt();
        this.options = new ArrayList<>(optionsSize);

        for (int i = 0; i < optionsSize; i++) {
            int optionType = in.readInt();

            ClassLoader classLoader;
            switch (optionType) {
                case BaseOption.DATABASE:
                    classLoader = DatabaseOption.class.getClassLoader();
                    break;
                case BaseOption.SIMPLE_BOOLEAN:
                    classLoader = SimpleBooleanOption.class.getClassLoader();
                    break;
                case BaseOption.SIMPLE_TEXT:
                    classLoader = SimpleTextOption.class.getClassLoader();
                    break;
                case BaseOption.SIMPLE_NUMBER:
                    classLoader = SimpleNumberOption.class.getClassLoader();
                    break;
                case BaseOption.SPINNER:
                    classLoader = SpinnerOption.class.getClassLoader();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown option type !!!");
            }

            this.options.add(in.readParcelable(classLoader));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseOption> T findOptionByKey(int key) {
        for (BaseOption option : options) {
            if (option.key == key) {
                return (T) option;
            }
        }

        return null;
    }

    public Boolean extractBoleanValueFromOption(int key) {
        SimpleBooleanOption simpleBooleanOption = findOptionByKey(key);
        return simpleBooleanOption != null && simpleBooleanOption.checked;
    }

    public Integer extractNumberValueFromOption(int key) {
        SimpleNumberOption simpleNumberOption = findOptionByKey(key);
        return simpleNumberOption == null ? null : simpleNumberOption.value;
    }

    public String extractTextValueFromOption(int key) {
        SimpleTextOption option = findOptionByKey(key);
        return option == null ? null : option.value;
    }

    public Integer extractDatabaseEntryValueId(int key) {
        DatabaseOption databaseOption = findOptionByKey(key);
        if (databaseOption == null || databaseOption.value == null) {
            return null;
        } else {
            return databaseOption.value.id;
        }
    }

    public void setQuery(String q) {
        this.query = q;
    }

    public ArrayList<BaseOption> getOptions() {
        return options;
    }
}

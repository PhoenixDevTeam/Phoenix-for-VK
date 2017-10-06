package biz.dealnote.messenger.db;

import android.content.ContentProviderResult;
import android.content.UriMatcher;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class DatabaseIdRange implements Parcelable {

    public static final DatabaseIdRange INVALID = new DatabaseIdRange(-1, -1);

    private final int first;
    private final int last;

    private DatabaseIdRange(int first, int last){
        this.first = first;
        this.last = last;
    }

    protected DatabaseIdRange(Parcel in) {
        first = in.readInt();
        last = in.readInt();
    }

    public int getFirst() {
        return first;
    }

    public int getLast() {
        return last;
    }

    public static final Creator<DatabaseIdRange> CREATOR = new Creator<DatabaseIdRange>() {
        @Override
        public DatabaseIdRange createFromParcel(Parcel in) {
            return new DatabaseIdRange(in);
        }

        @Override
        public DatabaseIdRange[] newArray(int size) {
            return new DatabaseIdRange[size];
        }
    };

    public static DatabaseIdRange create(int first, int last){
        return new DatabaseIdRange(first, last);
    }

    @Nullable
    public static DatabaseIdRange createFromContentProviderResults(ContentProviderResult[] results){
        Integer f = null;
        Integer l = null;

        for(ContentProviderResult result : results){
            if(result.uri != null && !result.uri.toString().isEmpty()){
                int dbid = Integer.parseInt(result.uri.getPathSegments().get(1));
                if(f == null || dbid < f){
                    f = dbid;
                }

                if(l == null || dbid > l){
                    l = dbid;
                }
            }
        }

        return nonNull(f) && nonNull(l) ? new DatabaseIdRange(f, l) : null;
    }

    private static final int MATCH_CODE = 10;

    @Nullable
    public static DatabaseIdRange createFromContentProviderResults(ContentProviderResult[] results, String path){
        UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(MessengerContentProvider.AUTHORITY, path, MATCH_CODE);

        Integer f = null;
        Integer l = null;
        for(ContentProviderResult result : results){
            if(result.uri != null && !result.uri.toString().isEmpty()){
                if(sUriMatcher.match(result.uri) != MATCH_CODE){
                    continue;
                }

                int dbid = Integer.parseInt(result.uri.getPathSegments().get(1));
                if(f == null || dbid < f){
                    f = dbid;
                }

                if(l == null || dbid > l){
                    l = dbid;
                }
            }
        }

        return nonNull(f) && nonNull(l) ? new DatabaseIdRange(f, l) : null;
    }


    @Override
    public String toString() {
        return "DatabaseIdRange{" +
                "first=" + first +
                ", last=" + last +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(first);
        dest.writeInt(last);
    }
}

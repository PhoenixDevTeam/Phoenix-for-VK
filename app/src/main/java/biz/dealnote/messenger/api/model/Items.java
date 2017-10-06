package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by admin on 21.12.2016.
 * phoenix
 */
public class Items<I> {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<I> items;

    public List<I> getItems() {
        return items;
    }

    public int getCount() {
        return count;
    }
}

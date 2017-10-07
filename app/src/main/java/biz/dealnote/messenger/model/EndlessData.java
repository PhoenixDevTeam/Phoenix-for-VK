package biz.dealnote.messenger.model;

import java.util.List;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class EndlessData<T> {

    private final List<T> data;

    private final boolean hasNext;

    public EndlessData(List<T> data, boolean hasNext) {
        this.data = data;
        this.hasNext = hasNext;
    }

    public static <T> EndlessData<T> create(List<T> data, boolean hasNext){
        return new EndlessData<>(data, hasNext);
    }

    public List<T> get() {
        return data;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
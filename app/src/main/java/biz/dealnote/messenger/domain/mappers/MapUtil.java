package biz.dealnote.messenger.domain.mappers;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class MapUtil {

    public static <O, R> List<R> mapAll(@Nullable List<O> orig, @NonNull MapF<O, R> function) {
        if (orig != null) {
            List<R> list = new ArrayList<>(orig.size());
            for (O o : orig) {
                list.add(function.map(o));
            }
            return list;
        } else {
            return new ArrayList<>(0);
        }
    }
}
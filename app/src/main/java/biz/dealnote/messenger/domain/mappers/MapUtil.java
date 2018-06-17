package biz.dealnote.messenger.domain.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
class MapUtil {

    static <O, R> void mapAndAdd(@Nullable Collection<O> orig, @NonNull MapF<O, R> function, @NonNull Collection<R> target) {
        if (orig != null) {
            for (O o : orig) {
                target.add(function.map(o));
            }
        }
    }

    static <O, R> List<R> mapAll(@Nullable Collection<O> orig, @NonNull MapF<O, R> function, boolean mutable) {
        if (orig != null && orig.size() > 0) {
            if (mutable || orig.size() > 1) {
                List<R> list = new ArrayList<>(orig.size());
                for (O o : orig) {
                    list.add(function.map(o));
                }
                return list;
            }

            return Collections.singletonList(function.map(orig.iterator().next()));
        } else {
            return mutable ? new ArrayList<>(0) : Collections.emptyList();
        }
    }

    static <O, R> List<R> mapAll(@Nullable Collection<O> orig, @NonNull MapF<O, R> function) {
        return mapAll(orig, function, true);
    }
}
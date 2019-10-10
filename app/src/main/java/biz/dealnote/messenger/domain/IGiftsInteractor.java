package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Gift;
import io.reactivex.Single;

public interface IGiftsInteractor {
    Single<List<Gift>> get(int userId, IntNextFrom start, int count);
}

package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.db.model.entity.NewsEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.model.FeedSourceCriteria;
import biz.dealnote.messenger.model.criteria.FeedCriteria;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

public interface IFeedRepository extends IRepository {

    Single<List<NewsEntity>> findByCriteria(@NonNull FeedCriteria criteria);

    Single<int[]> store(int accountId, @NonNull List<NewsEntity> data, @Nullable OwnerEntities owners, boolean clearBeforeStore);

    Completable storeLists(int accountid, @NonNull List<VkApiFeedList> data);

    Single<List<Pair<Integer, String>>> getAllLists(@NonNull FeedSourceCriteria criteria);
}

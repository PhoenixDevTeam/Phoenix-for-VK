package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.feedback.FeedbackEntity;
import biz.dealnote.messenger.model.criteria.NotificationsCriteria;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 13-Jun-16.
 * phoenix
 */
public interface IFeedbackStore extends IStore {
    Single<int[]> insert(int accountId, List<FeedbackEntity> dbos, OwnerEntities owners, boolean clearBefore);
    Single<List<FeedbackEntity>> findByCriteria(@NonNull NotificationsCriteria criteria);
}
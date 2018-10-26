package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.feedback.FeedbackEntity;
import biz.dealnote.messenger.model.criteria.NotificationsCriteria;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 13-Jun-16.
 * phoenix
 */
public interface IFeedbackStorage extends IStorage {
    Single<int[]> insert(int accountId, List<FeedbackEntity> dbos, OwnerEntities owners, boolean clearBefore);
    Single<List<FeedbackEntity>> findByCriteria(@NonNull NotificationsCriteria criteria);
}
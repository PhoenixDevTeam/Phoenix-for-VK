package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.feedback.Feedback;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 11.09.2017.
 * phoenix
 */
public interface IFeedbackInteractor {
    Single<List<Feedback>> getCachedFeedbacks(int accountId);
    Single<Pair<List<Feedback>, String>> getActualFeedbacks(int accountId, int count, String startFrom);

    Completable maskAaViewed(int accountId);
}
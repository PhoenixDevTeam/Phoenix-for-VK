package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import java.util.Collection;

import biz.dealnote.messenger.api.model.VKApiPoll;
import io.reactivex.Single;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IPollsApi {

    @CheckResult
    Single<VKApiPoll> create(String question, Boolean isAnonymous, Integer ownerId, Collection<String> addAnswers);

    @CheckResult
    Single<Boolean> deleteVote(Integer ownerId, int pollId, int answerId, Boolean isBoard);

    @CheckResult
    Single<Boolean> addVote(Integer ownerId, int pollId, int answerId, Boolean isBoard);

    @CheckResult
    Single<VKApiPoll> getById(Integer ownerId, Boolean isBoard, Integer pollId);

}

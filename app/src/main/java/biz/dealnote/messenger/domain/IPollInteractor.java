package biz.dealnote.messenger.domain;

import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.model.Poll;
import io.reactivex.Single;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IPollInteractor {
    Single<Poll> createPoll(int accountId, String question, boolean anon, boolean multiple, int ownerId, List<String> options);
    Single<Poll> addVote(int accountId, Poll poll, Set<Integer> answerIds);
    Single<Poll> removeVote(int accountId, Poll poll, int answerId);
    Single<Poll> getPollById(int accountId, int ownerId, int pollId, boolean isBoard);
}
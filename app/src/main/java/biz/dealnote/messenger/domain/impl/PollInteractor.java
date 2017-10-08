package biz.dealnote.messenger.domain.impl;

import java.util.List;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.domain.IPollInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Poll;
import io.reactivex.Single;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class PollInteractor implements IPollInteractor {

    private final INetworker networker;

    public PollInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<Poll> createPoll(int accountId, String question, boolean anon, int ownerId, List<String> options) {
        return networker.vkDefault(accountId)
                .polls()
                .create(question, anon, ownerId, options)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<Poll> addVote(int accountId, Poll poll, int answerId) {
        return networker.vkDefault(accountId)
                .polls()
                .addVote(poll.getOwnerId(), poll.getId(), answerId, poll.isBoard())
                .map(ignore -> withAddVoteChanges(answerId, poll));
    }

    private static Poll withAddVoteChanges(int answerId, Poll poll) {
        poll.setMyAnswerId(answerId);
        poll.setVoteCount(poll.getVoteCount() + 1);

        for (Poll.Answer answer : poll.getAnswers()) {
            if (answer.getId() == answerId) {
                answer.setVoteCount(answer.getVoteCount() + 1);
            }

            if (poll.getVoteCount() == 0) {
                answer.setRate(0);
                continue;
            }

            answer.setRate((double) answer.getVoteCount() / (double) poll.getVoteCount() * 100);
        }

        return poll;
    }

    @Override
    public Single<Poll> removeVote(int accountId, Poll poll, int answerId) {
        return networker.vkDefault(accountId)
                .polls()
                .deleteVote(poll.getOwnerId(), poll.getId(), answerId, poll.isBoard())
                .map(ignore -> withRemoveVoteChanges(answerId, poll));
    }

    private static Poll withRemoveVoteChanges(int answerId, Poll poll) {
        poll.setMyAnswerId(0);
        poll.setVoteCount(poll.getVoteCount() - 1);

        for (Poll.Answer answer : poll.getAnswers()) {
            if (answer.getId() == answerId) {
                answer.setVoteCount(answer.getVoteCount() - 1);
            }

            if (poll.getVoteCount() == 0) {
                answer.setRate(0);
                continue;
            }

            answer.setRate((double) answer.getVoteCount() / (double) poll.getVoteCount() * 100);
        }

        return poll;
    }

    @Override
    public Single<Poll> getPollById(int accountId, int ownerId, int pollId, boolean isBoard) {
        return networker.vkDefault(accountId)
                .polls()
                .getById(ownerId, isBoard, pollId)
                .map(dto -> Dto2Model.transform(dto).setBoard(isBoard));
    }
}
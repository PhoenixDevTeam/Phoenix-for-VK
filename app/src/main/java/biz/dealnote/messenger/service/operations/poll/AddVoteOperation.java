package biz.dealnote.messenger.service.operations.poll;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class AddVoteOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        Poll poll = (Poll) request.getParcelable(Extra.POLL);

        int answerId = request.getInt(EXTRA_ANSWER_ID);
        //boolean storeToDb = request.getBoolean(Extra.STORE_TO_DB);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .polls()
                .addVote(poll.getOwnerId(), poll.getId(), answerId, poll.isBoard())
                .blockingGet();

        if (success) {
            commitChanges(answerId, poll);
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(Extra.SUCCESS, success);
        bundle.putParcelable(Extra.POLL, poll);
        return bundle;
    }

    private void commitChanges(int answerId, Poll poll) {
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

        //if(storeToPollsTable){
        //    context.getContentResolver().insert(MessengerContentProvider.getPollContentUriFor(aid), PollColumns.getCV(poll));
        //}
    }
}
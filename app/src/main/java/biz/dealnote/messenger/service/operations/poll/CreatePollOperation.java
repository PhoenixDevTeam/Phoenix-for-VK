package biz.dealnote.messenger.service.operations.poll;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiPoll;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.service.StringArray;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class CreatePollOperation extends AbsApiOperation {

    public static final String EXTRA_QUESTION = "question";
    public static final String EXTRA_IS_ANOMYMOUS = "is_anonymous";
    public static final String EXTRA_ADD_ANSWERS = "add_answers";

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        String question = request.getString(EXTRA_QUESTION);
        boolean anonymous = request.getBoolean(EXTRA_IS_ANOMYMOUS);
        Integer ownerId = request.optInt(Extra.OWNER_ID);
        StringArray options = (StringArray) request.getParcelable(EXTRA_ADD_ANSWERS);

        VKApiPoll poll = Apis.get()
                .vkDefault(accountId)
                .polls()
                .create(question, anonymous, ownerId, options.asList())
                .blockingGet();

        //context.getContentResolver().insert(MessengerContentProvider.getPollContentUriFor(accountId), PollColumns.getCV(poll));

        Bundle result = new Bundle();
        result.putParcelable(Extra.POLL, Dto2Model.transform(poll));
        return result;
    }
}

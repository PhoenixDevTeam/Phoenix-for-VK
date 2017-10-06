package biz.dealnote.messenger.service.operations.message;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.db.DialogsHelper;
import biz.dealnote.messenger.service.operations.AbsApiOperation;

public class EditChatOperation extends AbsApiOperation {

    @Override
    public Bundle execute(Context context, Request request, int accountId) throws Exception {
        int chatId = request.getInt(Extra.CHAT_ID);
        String title = request.getString(Extra.TITLE);

        boolean success = Apis.get()
                .vkDefault(accountId)
                .messages()
                .editChat(chatId, title)
                .blockingGet();

        if (success) {
            DialogsHelper.changeChatTitle(context, accountId, chatId, title);
        }

        return buildSimpleSuccessResult(success);
    }
}

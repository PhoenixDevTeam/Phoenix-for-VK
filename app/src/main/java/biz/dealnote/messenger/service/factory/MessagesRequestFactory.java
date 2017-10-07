package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;

public class MessagesRequestFactory {

    public static final int REQUEST_DELETE_DIALOG = 7002;
    public static final int REQUEST_EDIT_CHAT = 7004;

    public static Request getEditChatRequest(int chatId, String title){
        Request request = new Request(REQUEST_EDIT_CHAT);
        request.put(Extra.CHAT_ID, chatId);
        request.put(Extra.TITLE, title);
        return request;
    }

    public static Request getDeleteDialogRequest(int accountId, int peerId, int offset, int count){
        Request request = new Request(REQUEST_DELETE_DIALOG);
        request.put(Extra.ACCOUNT_ID, accountId);
        request.put(Extra.PEER_ID, peerId);
        request.put(Extra.OFFSET, offset);
        request.put(Extra.COUNT, count);
        return request;
    }
}
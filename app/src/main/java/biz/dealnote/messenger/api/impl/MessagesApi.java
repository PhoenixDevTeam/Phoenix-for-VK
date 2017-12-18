package biz.dealnote.messenger.api.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IMessagesApi;
import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VkApiDialog;
import biz.dealnote.messenger.api.model.VkApiLongpollServer;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.api.model.response.DialogsResponse;
import biz.dealnote.messenger.api.model.response.LongpollHistoryResponse;
import biz.dealnote.messenger.api.model.response.MessageHistoryResponse;
import biz.dealnote.messenger.api.model.response.SearchDialogsResponse;
import biz.dealnote.messenger.api.services.IMessageService;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;

/**
 * Created by ruslan.kolbasa on 29.12.2016.
 * phoenix
 */
class MessagesApi extends AbsApi implements IMessagesApi {

    MessagesApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    private Single<IMessageService> serviceRx(int... tokenTypes) {
        return super.provideService(IMessageService.class, tokenTypes);
    }

    @Override
    public Completable edit(int peerId, int messageId, String message, List<IAttachmentToken> attachments, boolean keepFwd) {
        String atts = join(attachments, ",", AbsApi::formatAttachmentToken);

        String fwd = keepFwd ? "-" + messageId : null; // todo not working!!!
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMapCompletable(service -> service
                        .editMessage(peerId, messageId, message, atts, fwd)
                        .toCompletable());
    }

    @Override
    public Single<Boolean> removeChatUser(int chatId, int userId) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .removeChatUser(chatId, userId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> addChatUser(int chatId, int userId) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .addChatUser(chatId, userId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));

    }

    @Override
    public Single<Map<Integer, List<ChatUserDto>>> getChatUsers(Collection<Integer> chatIds, String fields, String nameCase) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .getChatUsers(join(chatIds, ","), fields, nameCase)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiChat>> getChat(Integer chatId, Collection<Integer> chatIds, String fields, String nameCase) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .getChat(chatId, join(chatIds, ","), fields, nameCase)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> listEmptyIfNull(response.chats)));
    }

    @Override
    public Single<Boolean> editChat(int chatId, String title) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .editChat(chatId, title)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> createChat(Collection<Integer> userIds, String title) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .createChat(join(userIds, ","), title)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> deleteDialog(int peerId, Integer offset, Integer count) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .deleteDialog(peerId, offset, count)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> restore(int messageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .restore(messageId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Map<String, Integer>> delete(Collection<Integer> messageIds, Boolean deleteForAll, Boolean spam) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .delete(join(messageIds, ","), integerFromBoolean(deleteForAll), integerFromBoolean(spam)) //{"response":{"1173002":1,"1173001":1}}
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> markAsRead(Collection<Integer> messageIds, Integer peerId, Integer startMessageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .markAsRead(join(messageIds, ","), peerId, startMessageId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> setActivity(int peerId, boolean typing) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .setActivity(peerId, typing ? "typing" : null)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Items<VKApiMessage>> search(String query, Integer peerId, Long date, Integer previewLength, Integer offset, Integer count) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .search(query, peerId, date, previewLength, offset, count)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            fixMessageList(response.getItems());
                            return response;
                        }));
    }

    @Override
    public Single<LongpollHistoryResponse> getLongPollHistory(Long ts, Long pts, Integer previewLength, Boolean onlines, String fields, Integer eventsLimit, Integer msgsLimit, Integer max_msg_id) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .getLongPollHistory(ts, pts, previewLength, integerFromBoolean(onlines), fields,
                                eventsLimit, msgsLimit, max_msg_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<AttachmentsHistoryResponse> getHistoryAttachments(int peerId, String mediaType, String startFrom, Integer count, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getHistoryAttachments(peerId, mediaType, startFrom, count, 1, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> send(Integer randomId, Integer peerId, String domain, String message,
                                Double latitude, Double longitude, Collection<IAttachmentToken> attachments,
                                Collection<Integer> forwardMessages, Integer stickerId) {

        String atts = join(attachments, ",", AbsApi::formatAttachmentToken);
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .send(randomId, peerId, domain, message, latitude, longitude, atts,
                                join(forwardMessages, ","), stickerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DialogsResponse> getDialogs(Integer offset, Integer count, Integer startMessageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getDialogs(offset, count, startMessageId, null, null, null)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            if (nonNull(response.dialogs)) {
                                for (VkApiDialog dialog : response.dialogs) {
                                    fixMessage(dialog.message);
                                }
                            }
                            return response;
                        }));
    }

    @Override
    public Single<List<VKApiMessage>> getById(Collection<Integer> identifiers) {
        String ids = join(identifiers, ",");

        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getById(ids, null)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            List<VKApiMessage> messages = response.getItems();
                            fixMessageList(messages);
                            return messages;
                        }));
    }

    private void fixMessage(VKApiMessage message) {
        if (message.from_id == 0) {
            if (message.out) {
                message.from_id = getAccountId();
            }
        }
    }

    private void fixMessageList(Collection<VKApiMessage> dtos) {
        for (VKApiMessage message : dtos) {
            fixMessage(message);
        }
    }

    @Override
    public Single<MessageHistoryResponse> getHistory(Integer offset, Integer count, int peerId, Integer startMessageId, Boolean rev) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getHistory(offset, count, peerId, startMessageId, rev != null && rev ? 1 : 0)
                        .map(extractResponseWithErrorHandling())
                        .map(history -> {
                            fixMessageList(history.messages);
                            return history;
                        }));
    }

    @Override
    public Single<VkApiLongpollServer> getLongpollServer(boolean needPts, boolean useSsl) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getLongpollServer(needPts ? 1 : 0, useSsl ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<SearchDialogsResponse.AbsChattable>> searchDialogs(String query, Integer limit, String fileds) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .searchDialogs(query, limit, fileds)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> isNull(response.getData()) ? Collections.emptyList() : response.getData()));
    }
}
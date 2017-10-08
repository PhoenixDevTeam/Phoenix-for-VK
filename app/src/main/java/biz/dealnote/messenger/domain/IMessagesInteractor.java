package biz.dealnote.messenger.domain;

import android.support.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.model.AppChatUser;
import biz.dealnote.messenger.model.Dialog;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.model.SentMsg;
import biz.dealnote.messenger.model.User;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 03.09.2017.
 * phoenix
 */
public interface IMessagesInteractor {
    /**
     * Получить все закэшированные сообщения в локальной БД
     * @param accountId идентификатор аккаунта
     * @param peerId идентификатор диалога
     * @return полученные сообщения
     */
    Single<List<Message>> getCachedPeerMessages(int accountId, int peerId);

    /**
     * Получить все закэшированные диалоги в локальной БД
     * @param accountId идентификатор аккаунта
     * @return диалоги
     */
    Single<List<Dialog>> getCachedDialogs(int accountId);

    /**
     * Сохранить в локальную БД сообщения
     * @param accountId идентификатор аккаунта
     * @param messages сообщения
     * @return Completable
     */
    Completable insertMessages(int accountId, List<VKApiMessage> messages);

    /**
     * Получить актуальный список сообщений для конкретного диалога
     * @param accountId идентификатор аккаунта
     * @param peerId идентификатор диалога
     * @param count количество сообщений
     * @param offset сдвиг (может быть как положительным, так и отрицательным)
     * @param startMessageId идентификатор сообщения, после которого необходимо получить (если null - от последнего)
     * @param cacheData если true - сохранить полученные данные в кэш
     * @return полученные сообщения
     */
    Single<List<Message>> getPeerMessages(int accountId, int peerId, int count, Integer offset, Integer startMessageId, boolean cacheData);

    Single<List<Dialog>> getDialogs(int accountId, int count, Integer startMessageId);

    Single<List<Message>> findCachedMessages(int accountId, List<Integer> ids);

    @CheckResult
    Completable fixDialogs(int accountId, int peerId);

    @CheckResult
    Completable fixDialogs(int accountId, int peerId, int unreadCount);

    Single<Message> put(SaveMessageBuilder builder);

    Single<Integer> send(int accountId, int dbid);

    Single<SentMsg> sendUnsentMessage(Collection<Integer> accountIds);

    /**
     * Поиск диалогов
     * @param accountId идентификатор аккаунта
     * @param count количество результатов
     * @param q строка поиска
     * @return список найденных диалогов
     */
    Single<List<Object>> searchDialogs(int accountId, int count, String q);

    Single<List<Message>> searchMessages(int accountId, Integer peerId, int count, int offset, String q);

    Single<List<AppChatUser>> getChatUsers(int accountId, int chatId);

    Completable removeChatUser(int accountId, int chatId, int userId);

    Single<List<AppChatUser>> addChatUsers(int accountId, int chatId, List<User> users);

    Completable deleteDialog(int accountId, int peedId, int count, int offset);

    Completable deleteMessages(int accountId, Collection<Integer> ids);

    Completable restoreMessage(int accountId, int messageId);

    Completable changeChatTitle(int accountId, int chatId, String title);

    Single<Integer> createGroupChat(int accountId, Collection<Integer> users, String title);

    Completable markAsRead(int accountId, int peerId);
}
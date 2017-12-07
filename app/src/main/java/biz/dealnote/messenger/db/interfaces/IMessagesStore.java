package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.db.model.MessagePatch;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.model.DraftMessage;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.model.MessageUpdate;
import biz.dealnote.messenger.model.criteria.MessagesCriteria;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 01.06.2016.
 * VKMessenger
 */
public interface IMessagesStore extends IStore {

    Completable insertPeerDbos(int accountId, int peerId, @NonNull List<MessageEntity> dbos, boolean clearHistory);

    Single<int[]> insertDbos(int accountId, @NonNull List<MessageEntity> dbos);

    Single<Integer> calculateUnreadCount(int accountId, int peerId);

    Single<List<MessageEntity>> getByCriteria(@NonNull MessagesCriteria criteria, boolean withAtatchments, boolean withForwardMessages);

    Single<Integer> insert(int accountId, int peerId, @NonNull MessagePatch patch);

    Single<Integer> applyPatch(int accountId, int messageId, @NonNull MessagePatch patch);

    @CheckResult
    Maybe<DraftMessage> findDraftMessage(int accountId, int peerId);

    @CheckResult
    Single<Integer> saveDraftMessageBody(int acocuntId, int peerId, String body);

    //@CheckResult
    //Maybe<Integer> getDraftMessageId(int accoutnId, int peerId);

    Single<Integer> getMessageStatus(int accountId, int dbid);

    @CheckResult
    Completable changeMessageStatus(int accountId, int messageId, @MessageStatus int status, @Nullable Integer vkid);

    //@CheckResult
    //Completable updateMessageFlag(int accountId, int messageId, Collection<Pair<Integer, Boolean>> values);

    @CheckResult
    Single<Boolean> deleteMessage(int accountId, int messageId);

    Single<Optional<Integer>> findLastSentMessageIdForPeer(int accounId, int peerId);

    Single<List<MessageEntity>> findMessagesByIds(int accountId, List<Integer> ids, boolean withAtatchments, boolean withForwardMessages);

    Single<Optional<Pair<Integer, MessageEntity>>> findFirstUnsentMessage(Collection<Integer> accountIds, boolean withAtatchments, boolean withForwardMessages);

    Completable notifyMessageHasAttachments(int accountId, int messageId);

    ///**
    // * Получить список сообщений, которые "приаттаччены" к сообщению с идентификатором attachTo
    // *
    // * @param accountId          идентификатор аккаунта
    // * @param attachTo           идентификатор сообщения
    // * @param includeFwd         если true - рекурсивно загрузить всю иерархию сообщений (вложенные во вложенных и т.д.)
    // * @param includeAttachments - если true - включить вложения к пересланным сообщениям
    // * @param forceAttachments   если true - то алгоритм проигнорирует значение в HAS_ATTACHMENTS
    // *                           и в любом случае будет делать выборку из таблицы вложений
    // * @return список сообщений
    // */
    //@CheckResult
    //Single<List<Message>> getForwardMessages(int accountId, int attachTo, boolean includeFwd, boolean includeAttachments, boolean forceAttachments);

    @CheckResult
    Single<List<Integer>> getForwardMessageIds(int accountId, int attachTo);

    Observable<MessageUpdate> observeMessageUpdates();

    Single<List<Integer>> getMissingMessages(int accountId, Collection<Integer> ids);

    Completable markAsRead(int accountId, int peerId);
}
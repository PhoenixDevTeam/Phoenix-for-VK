package biz.dealnote.messenger.db.interfaces;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.db.model.entity.DialogEntity;
import biz.dealnote.messenger.model.Chat;
import biz.dealnote.messenger.model.criteria.DialogsCriteria;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by hp-dv6 on 04.06.2016.
 * VKMessenger
 */
public interface IDialogsStore extends IStore {

    int getUnreadDialogsCount(int accountId);

    Observable<Pair<Integer, Integer>> observeUnreadDialogsCount();

    void setUnreadDialogsCount(int accountId, int unreadCount);

    Single<List<DialogEntity>> getDialogs(@NonNull DialogsCriteria criteria);

    Completable removePeerWithId(int accountId, int peerId);

    Completable updatePeerWithId(int accountId, int peerId, int lastMessageId, int unreadCount);

    Completable insertDialogs(int accountId, List<DialogEntity> dbos, boolean clearBefore);

    Completable changeTitle(int accountId, int peedId, String title);

    /**
     * Получение списка идентификаторов диалогов, информация о которых отсутствует в базе данных
     *
     * @param ids список входящих идентификаторов
     * @return отсутствующие
     */
    Single<Collection<Integer>> getMissingGroupChats(int accountId, Collection<Integer> ids);

    Observable<IDialogUpdate> observeDialogUpdates();

    Completable insertChats(int accountId, List<VKApiChat> chats);

    Observable<IDeletedDialog> observeDialogsDeleting();

    interface IDialogUpdate {
        int getAccountId();
        int getPeerId();
        int getLastMessageId();
        int getUnreadCount();
    }

    interface IDeletedDialog {
        int getAccountId();
        int getPeerId();
    }

    Single<Optional<Chat>> findChatById(int accountId, int peerId);
}
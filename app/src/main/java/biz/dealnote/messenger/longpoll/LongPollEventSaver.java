package biz.dealnote.messenger.longpoll;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import biz.dealnote.messenger.api.model.longpoll.BadgeCountChangeUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsResetUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsSetUpdate;
import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import biz.dealnote.messenger.db.LongPollOperation;
import biz.dealnote.messenger.db.MessengerContentProvider;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.MessageFlag;
import biz.dealnote.messenger.util.Exestime;
import biz.dealnote.messenger.util.Logger;
import io.reactivex.Completable;

import static biz.dealnote.messenger.util.Utils.hasSomeFlag;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class LongPollEventSaver {

    private final IMessagesInteractor messagesInteractor;

    public LongPollEventSaver() {
        this.messagesInteractor = InteractorFactory.createMessagesInteractor();
    }

    public Completable save(@NonNull Context context, int accountId, @NonNull VkApiLongpollUpdates updates) {
        return Completable.create(e -> {
            long start = System.currentTimeMillis();
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (nonEmpty(updates.message_flags_reset_updates)) {
                operations.addAll(LongPollOperation.forMessagesFlagReset(accountId, updates.message_flags_reset_updates));
            }

            if (nonEmpty(updates.message_flags_set_updates)) {
                operations.addAll(LongPollOperation.forMessagesFlagSet(accountId, updates.message_flags_set_updates));
            }

            if (nonEmpty(updates.user_is_offline_updates) || nonEmpty(updates.user_is_online_updates)) {
                operations.addAll(LongPollOperation.forUserActivityUpdates(accountId, updates.user_is_offline_updates, updates.user_is_online_updates));
            }

            if (nonEmpty(updates.input_messages_set_read_updates) || nonEmpty(updates.output_messages_set_read_updates)) {
                operations.addAll(LongPollOperation.forMessagesReadUpdate(accountId, updates.input_messages_set_read_updates, updates.output_messages_set_read_updates));
            }

            try {
                if (nonEmpty(operations)) {
                    context.getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
                }
            } catch (RemoteException | OperationApplicationException error) {
                Logger.d(LongPollNotificationHelper.TAG, "Error commit long polling actions to DB: " + e.toString());
            }

            Collection<Integer> changedEntries = getChangedPeerIds(updates);

            if (nonEmpty(changedEntries)) {
                for (Integer peerId : changedEntries) {
                    messagesInteractor.fixDialogs(accountId, peerId).blockingAwait();
                }
            }

            if(nonEmpty(updates.getBadgeCountChangeUpdates())){
                for(BadgeCountChangeUpdate u : updates.getBadgeCountChangeUpdates()){
                    Stores.getInstance()
                            .dialogs()
                            .setUnreadDialogsCount(accountId, u.getCount());
                }
            }

            Exestime.log("LongPollEventSaver, doInBackground", start, "count: " + updates.getUpdatesCount());
            e.onComplete();
        });
    }

    /**
     * Возвращает список чатов, в которых произошли изменения,
     * повлиявшие на счетчики непрочитанных
     *
     * @return список чатов
     */
    private static Collection<Integer> getChangedPeerIds(VkApiLongpollUpdates updates) {
        Set<Integer> result = new HashSet<>(1);

        //if (nonEmpty(updates.add_message_updates)) {
        //    for (AddMessageUpdate u : updates.add_message_updates) {
        //        result.add(u.peer_id);
        //    }
        //}

        if (nonEmpty(updates.message_flags_reset_updates)) {
            for (MessageFlagsResetUpdate u : updates.message_flags_reset_updates) {
                if(hasSomeFlag(u.getMask(), MessageFlag.DELETED, MessageFlag.UNREAD)){
                    result.add(u.getPeerId());
                }
            }
        }

        if (nonEmpty(updates.message_flags_set_updates)) {
            for (MessageFlagsSetUpdate u : updates.message_flags_set_updates) {
                if(hasSomeFlag(u.getMask(), MessageFlag.DELETED, MessageFlag.UNREAD)){
                    result.add(u.getPeerId());
                }
            }
        }

        return result;
    }
}
package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.longpoll.AbsLongpollEvent;
import biz.dealnote.messenger.api.model.longpoll.AddMessageUpdate;
import biz.dealnote.messenger.api.model.longpoll.BadgeCountChangeUpdate;
import biz.dealnote.messenger.api.model.longpoll.InputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsResetUpdate;
import biz.dealnote.messenger.api.model.longpoll.MessageFlagsSetUpdate;
import biz.dealnote.messenger.api.model.longpoll.OutputMessagesSetReadUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOfflineUpdate;
import biz.dealnote.messenger.api.model.longpoll.UserIsOnlineUpdate;
import biz.dealnote.messenger.api.model.longpoll.WriteTextInDialogUpdate;
import biz.dealnote.messenger.api.util.VKStringUtils;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.hasFlag;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class LongpollUpdateAdapter extends AbsAdapter implements JsonDeserializer<AbsLongpollEvent> {

    private static final String TAG = LongpollUpdateAdapter.class.getSimpleName();

    @Override
    public AbsLongpollEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        int action = array.get(0).getAsInt();
        return deserialize(action, array);
    }

    private AbsLongpollEvent deserialize(int action, JsonArray array){
        switch (action){
            case AbsLongpollEvent.ACTION_MESSAGE_ADDED:
                AddMessageUpdate addMessageUpdate = deserializeAddMessageUpdate(array);
                if(addMessageUpdate.message_id == 0){
                    Logger.e(TAG, "AddMessageUpdate.message_id=0, array: " + array);
                    return null;
                }

                return addMessageUpdate;

            case AbsLongpollEvent.ACTION_USER_WRITE_TEXT_IN_DIALOG:
                WriteTextInDialogUpdate w = new WriteTextInDialogUpdate();
                w.user_id = optInt(array, 1);
                w.flags = optInt(array, 2);
                return w;

            case AbsLongpollEvent.ACTION_USER_IS_ONLINE:
                UserIsOnlineUpdate u = new UserIsOnlineUpdate();
                u.user_id = -optInt(array, 1);
                u.extra = optInt(array, 2);
                return u;

            case AbsLongpollEvent.ACTION_USER_IS_OFFLINE:
                UserIsOfflineUpdate u1 = new UserIsOfflineUpdate();
                u1.user_id = -optInt(array, 1);
                u1.flags = optInt(array, 2);
                return u1;

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_RESET:
                MessageFlagsResetUpdate r = new MessageFlagsResetUpdate();
                r.message_id = optInt(array, 1);
                r.mask = optInt(array, 2);
                r.peer_id = optInt(array, 3);

                if(r.message_id == 0){
                    Logger.e(TAG, "MessageFlagsResetUpdate.message_id=0, array: " + array);
                    return null;
                }

                return r;

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_SET:
                MessageFlagsSetUpdate messageFlagsSetUpdate = new MessageFlagsSetUpdate();
                messageFlagsSetUpdate.message_id = optInt(array, 1);
                messageFlagsSetUpdate.mask = optInt(array, 2);
                messageFlagsSetUpdate.peer_id = optInt(array, 3);

                if(messageFlagsSetUpdate.getMessageId() == 0){
                    Logger.e(TAG, "MessageFlagsSetUpdate.message_id=0, array: " + array);
                    return null;
                }

                return messageFlagsSetUpdate;

            case AbsLongpollEvent.ACTION_COUNTER_UNREAD_WAS_CHANGED:
                BadgeCountChangeUpdate c = new BadgeCountChangeUpdate();
                c.count = optInt(array, 1);
                return c;

            case AbsLongpollEvent.ACTION_SET_INPUT_MESSAGES_AS_READ:
                InputMessagesSetReadUpdate x = new InputMessagesSetReadUpdate();
                x.peer_id = optInt(array, 1);
                x.local_id = optInt(array, 2);

                if(x.peer_id == 0){
                    Logger.e(TAG, "InputMessagesSetReadUpdate.peer_id=0, array: " + array);
                    return null;
                }

                return x;

            case AbsLongpollEvent.ACTION_SET_OUTPUT_MESSAGES_AS_READ:
                OutputMessagesSetReadUpdate x1 = new OutputMessagesSetReadUpdate();
                x1.peer_id = optInt(array, 1);
                x1.local_id = optInt(array, 2);

                if(x1.peer_id == 0){
                    Logger.e(TAG, "OutputMessagesSetReadUpdate.peer_id=0, array: " + array);
                    return null;
                }

                return x1;
        }

        return null;
    }

    private AddMessageUpdate deserializeAddMessageUpdate(JsonArray array){
        AddMessageUpdate update = new AddMessageUpdate();

        int flags = optInt(array, 2);

        update.message_id = optInt(array, 1);
        update.peer_id = optInt(array, 3);
        update.timestamp = optLong(array, 4);
        update.text = VKStringUtils.unescape(optString(array, 5));
        update.outbox = hasFlag(flags, VKApiMessage.FLAG_OUTBOX);
        update.unread = hasFlag(flags, VKApiMessage.FLAG_UNREAD);
        update.important = hasFlag(flags, VKApiMessage.FLAG_IMPORTANT);
        update.deleted = hasFlag(flags, VKApiMessage.FLAG_DELETED);

        JsonObject extra = (JsonObject) opt(array, 6);
        if(nonNull(extra)){
            update.from = optInt(extra, "from");
            update.subject = optString(extra, "title");
            update.sourceText = optString(extra, "source_text");
            update.sourceAct = optString(extra, "source_act");
            update.sourceMid = optInt(extra, "source_mid");
        }

        JsonObject attachments = (JsonObject) opt(array, 7);
        if(nonNull(attachments)){
            update.hasMedia = attachments.has("attach1_type");
            String fwd = optString(attachments, "fwd");
            if(nonEmpty(fwd)){
                update.fwds = parseLineWithSeparators(fwd, ",");
            }
        }

        update.random_id = optString(array, 8); // ok

        if(update.from == 0 && !Peer.isGroupChat(update.peer_id) && !update.outbox){
            update.from = update.peer_id;
        }

        return update;
    }

    private static ArrayList<String> parseLineWithSeparators(String line, String separator) {
        if (isNull(line) || line.isEmpty()) {
            return null;
        }

        String[] tokens = line.split(separator);
        ArrayList<String> ids = new ArrayList<>();
        Collections.addAll(ids, tokens);
        return ids;
    }
}

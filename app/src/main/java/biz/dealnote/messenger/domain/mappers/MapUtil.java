package biz.dealnote.messenger.domain.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.model.VkApiConversation;
import biz.dealnote.messenger.model.Conversation;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

import static biz.dealnote.messenger.util.Utils.addFlagIf;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class MapUtil {

    static int calculateConversationAcl(VkApiConversation conversation){
        int result = 0;

        if(conversation.settings != null && conversation.settings.acl != null){
            VkApiConversation.Acl acl = conversation.settings.acl;
            result = addFlagIf(result, Conversation.AclFlags.CAN_CHANGE_INFO, acl.can_change_info);
            result = addFlagIf(result, Conversation.AclFlags.CAN_CHANGE_INVITE_LINK, acl.can_change_invite_link);
            result = addFlagIf(result, Conversation.AclFlags.CAN_CHANGE_PIN, acl.can_change_pin);
            result = addFlagIf(result, Conversation.AclFlags.CAN_INVITE, acl.can_invite);
            result = addFlagIf(result, Conversation.AclFlags.CAN_PROMOTE_USERS, acl.can_promote_users);
            result = addFlagIf(result, Conversation.AclFlags.CAN_SEE_INVITE_LINK, acl.can_see_invite_link);
        }

        return result;
    }

    static <O, R> void mapAndAdd(@Nullable Collection<O> orig, @NonNull MapF<O, R> function, @NonNull Collection<R> target) {
        if (orig != null) {
            for (O o : orig) {
                target.add(function.map(o));
            }
        }
    }

    public static <O, R> List<R> mapAll(@Nullable Collection<O> orig, @NonNull MapF<O, R> function, boolean mutable) {
        if (orig != null && orig.size() > 0) {
            if (mutable || orig.size() > 1) {
                List<R> list = new ArrayList<>(orig.size());
                for (O o : orig) {
                    list.add(function.map(o));
                }
                return list;
            }

            return Collections.singletonList(function.map(orig.iterator().next()));
        } else {
            return mutable ? new ArrayList<>(0) : Collections.emptyList();
        }
    }

    public static <O, R> List<R> mapAll(@Nullable Collection<O> orig, @NonNull MapF<O, R> function) {
        return mapAll(orig, function, true);
    }
}
package biz.dealnote.messenger.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.api.model.CommentsDto;
import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.api.model.VKApiComment;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VKApiTopic;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.VkApiDialog;
import biz.dealnote.messenger.api.model.feedback.Copies;
import biz.dealnote.messenger.api.model.feedback.UserArray;
import biz.dealnote.messenger.api.model.feedback.VkApiUsersFeedback;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Peer;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class VKOwnIds {

    private Set<Integer> uids;
    private Set<Integer> gids;

    public VKOwnIds() {
        this.uids = new HashSet<>();
        this.gids = new HashSet<>();
    }

    public VKOwnIds append(UserArray userArray){
        for (int id : userArray.ids) {
            append(id);
        }

        return this;
    }

    public VKOwnIds append(@NonNull VkApiUsersFeedback dto) {
        append(dto.users);
        return this;
    }

    public VKOwnIds append(@NonNull VKApiTopic topic){
        append(topic.created_by);
        append(topic.updated_by);
        return this;
    }

    public VKOwnIds append(@NonNull Copies copies){
        for (Copies.IdPair pair : copies.pairs) {
            append(pair.owner_id);
        }
        return this;
    }

    public VKOwnIds append(CommentsDto commentsDto){
        if(nonNull(commentsDto) && Utils.nonEmpty(commentsDto.list)){
            for(VKApiComment comment : commentsDto.list){
                append(comment);
            }
        }

        return this;
    }

    public VKOwnIds append(@NonNull VKApiComment comment) {
        append(comment.from_id);
        if (comment.attachments != null) {
            append(comment.attachments);
        }

        return this;
    }

    public VKOwnIds appendAttachmentDto(@NonNull VKApiAttachment attachment) {
        if (attachment instanceof VKApiPost) {
            append((VKApiPost) attachment);
        }

        return this;
    }

    public VKOwnIds append(@NonNull VkApiAttachments attachments) {
        List<VkApiAttachments.Entry> entries = attachments.entryList();
        for (VkApiAttachments.Entry entry : entries) {
            appendAttachmentDto(entry.attachment);
        }

        return this;
    }

    public static VKOwnIds fromPosts(@NonNull Collection<VKApiPost> posts) {
        VKOwnIds ids = new VKOwnIds();
        for (VKApiPost post : posts) {
            ids.append(post);
        }

        return ids;
    }

    @NonNull
    public Collection<Integer> getAll() {
        Collection<Integer> result = new HashSet<>(uids.size() + gids.size());
        result.addAll(uids);

        for (Integer gid : gids) {
            result.add(-Math.abs(gid));
        }

        return result;
    }

    public Set<Integer> getUids() {
        return uids;
    }

    public Set<Integer> getGids() {
        return gids;
    }

    public VKOwnIds appendAll(@NonNull Collection<Integer> ids) {
        for (Integer id : ids) {
            append(id);
        }

        return this;
    }

    public VKOwnIds append(Collection<VKApiMessage> messages) {
        if (messages != null) {
            for (VKApiMessage message : messages) {
                append(message);
            }
        }

        return this;
    }

    public VKOwnIds append(VkApiDialog dialog) {
        if (dialog.message != null) {
            append(dialog.message);
        }

        return this;
    }

    public VKOwnIds append(VKApiMessage message) {
        append(message.from_id);
        append(message.action_mid); // тут 100% пользователь, нюанс в том, что он может быть < 0, если email
        //append(message.admin_id);

        if (!message.isGroupChat()) {
            append(message.peer_id);
        }

        if (message.fwd_messages != null) {
            for (VKApiMessage fwd : message.fwd_messages) {
                append(fwd);
            }
        }

        if (nonNull(message.attachments)) {
            List<VkApiAttachments.Entry> entries = message.attachments.entryList();
            for (VkApiAttachments.Entry entry : entries) {
                if (entry.attachment instanceof VKApiPost) {
                    append((VKApiPost) entry.attachment);
                }
            }
        }

        return this;
    }

    public VKOwnIds append(@NonNull ArrayList<Message> messages) {
        for (Message message : messages) {
            append(message);
        }
        return this;
    }

    public VKOwnIds append(@NonNull Message message) {
        append(message.getSenderId());
        append(message.getActionMid()); // тут 100% пользователь, нюанс в том, что он может быть < 0, если email
        append(message.getAdminId());

        if (!Peer.isGroupChat(message.getPeerId())) {
            append(message.getPeerId());
        }

        if (nonNull(message.getFwd())) {
            List<Message> forwardMessages = message.getFwd();
            for (Message fwd : forwardMessages) {
                append(fwd);
            }
        }

        return this;
    }

    public VKOwnIds appendNews(@NonNull VKApiNews news) {
        append(news.source_id);
        append(news.copy_owner_id);

        if(news.hasCopyHistory()){
            for(VKApiPost post : news.copy_history){
                append(post);
            }
        }

        if(news.hasAttachments()){
            append(news.attachments);
        }

        return this;
    }


    public VKOwnIds append(VKApiPost post) {
        //append(post.owner_id);
        append(post.from_id);
        append(post.signer_id);
        append(post.created_by);

        if (post.copy_history != null) {
            for (VKApiPost copy : post.copy_history) {
                append(copy);
            }
        }

        return this;
    }

    public void append(int ownerId) {
        if (ownerId == 0) return;

        if (ownerId > 0) {
            appendUid(ownerId);
        } else {
            appendGid(ownerId);
        }
    }

    public void appendAll(int[] ownerIds){
        if(ownerIds != null){
            for(int id : ownerIds){
                append(id);
            }
        }
    }

    public void appendUid(int uid) {
        uids.add(uid);
    }

    public void appendGid(int gid) {
        gids.add(Math.abs(gid));
    }

    public boolean constainsUids() {
        return uids != null && !uids.isEmpty();
    }

    public boolean constainsGids() {
        return gids != null && !gids.isEmpty();
    }

    public boolean isEmpty() {
        return !constainsUids() && !constainsGids();
    }

    public boolean nonEmpty(){
        return constainsGids() || constainsUids();
    }

    @Override
    public String toString() {
        return "uids: " + uids + ", gids: " + gids;
    }

    public static boolean isUser(int ownerId) {
        return ownerId > 0;
    }

    public static boolean isGroup(int ownerId) {
        return ownerId < 0;
    }
}

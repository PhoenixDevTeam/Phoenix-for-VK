package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.link.internal.LinkActionAdapter;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.model.CryptStatus;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.BubbleLinearLayout;
import biz.dealnote.messenger.view.OnlineView;
import biz.dealnote.messenger.view.emoji.EmojiconTextView;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class MessagesAdapter extends RecyclerBindableAdapter<Message, RecyclerView.ViewHolder> {

    private static final int TYPE_MY_MESSAGE = 1;
    private static final int TYPE_FRIEND_MESSAGE = 2;
    private static final int TYPE_SERVICE = 3;
    private static final int TYPE_DELETED = 4;
    private static final int TYPE_STICKER_MY = 5;
    private static final int TYPE_STICKER_FRIEND = 6;

    private static final int ENCRYPTED_MESSAGE_BUBBLE_ALPHA = 150;

    private Context context;
    private AttachmentsViewBinder attachmentsViewBinder;
    private Transformation avatarTransformation;
    private ShapeDrawable selectedDrawable;
    private int unreadColor;

    private EmojiconTextView.OnHashTagClickListener onHashTagClickListener;
    private OnMessageActionListener onMessageActionListener;
    private AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback;

    public MessagesAdapter(Context context, List<Message> items, AttachmentsViewBinder.OnAttachmentsActionCallback callback) {
        super(items);
        this.context = context;
        this.attachmentsActionCallback = callback;
        this.attachmentsViewBinder = new AttachmentsViewBinder(context, callback);
        this.avatarTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.selectedDrawable = new ShapeDrawable(new OvalShape());
        this.selectedDrawable.getPaint().setColor(CurrentTheme.getIconColorActive(context));
        this.unreadColor = CurrentTheme.getMessageUnreadColor(context);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        Message message = getItem(position);
        switch (type) {
            case TYPE_DELETED:
                bindDeletedHolder((DeletedMessageHolder) viewHolder, message);
                break;
            case TYPE_SERVICE:
                bindServiceHolder((ServiceMessageHolder) viewHolder, message);
                break;
            case TYPE_MY_MESSAGE:
            case TYPE_FRIEND_MESSAGE:
                bindNormalMessage((MessageHolder) viewHolder, message);
                break;
            case TYPE_STICKER_FRIEND:
            case TYPE_STICKER_MY:
                bindStickerHolder((StickerMessageHolder) viewHolder, message);
                break;
        }
    }

    private void bindStickerHolder(StickerMessageHolder holder, final Message message) {
        bindBaseMessageHolder(holder, message);
        int stickerId = message.getAttachments().getStickers().get(0).getId();

        String url = VKApiStickerSet.buildImgUrl256(stickerId);
        PicassoInstance.with()
                .load(url)
                .into(holder.sticker);
    }

    private void bindStatusText(TextView textView, int status, long time) {
        switch (status) {
            case MessageStatus.SENDING:
                textView.setText(context.getString(R.string.sending));
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            case MessageStatus.QUEUE:
                textView.setText(context.getString(R.string.in_order));
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            case MessageStatus.ERROR:
                textView.setText(context.getString(R.string.error));
                textView.setTextColor(Color.RED);
                break;
            case MessageStatus.WAITING_FOR_UPLOAD:
                textView.setText(R.string.waiting_for_upload);
                textView.setTextColor(ContextCompat.getColor(context, R.color.default_message_status));
                break;
            default:
                textView.setText(AppTextUtils.getDateFromUnixTime(time));
                textView.setTextColor(CurrentTheme.getSecondaryTextColorCode(context));
                break;
        }
    }

    private OwnerLinkSpanFactory.ActionListener ownerLinkAdapter = new LinkActionAdapter(){
        @Override
        public void onOwnerClick(int ownerId) {
            if(nonNull(attachmentsActionCallback)){
                attachmentsActionCallback.onOpenOwner(ownerId);
            }
        }
    };

    private void bindReadState(View root, boolean read) {
        root.setBackgroundColor(read ? Color.TRANSPARENT : unreadColor);
        root.getBackground().setAlpha(60);
    }

    private void bindBaseMessageHolder(BaseMessageHolder holder, Message message) {
        holder.important.setVisibility(message.isImportant() ? View.VISIBLE : View.GONE);

        bindStatusText(holder.status, message.getStatus(), message.getDate());
        bindReadState(holder.itemView, message.isRead());

        if (message.isSelected()) {
            holder.avatar.setBackground(selectedDrawable);
            holder.avatar.setImageResource(R.drawable.ic_message_check_vector);
        } else {
            String avaurl = message.getSender() != null ? message.getSender().getMaxSquareAvatar() : null;
            ViewUtils.displayAvatar(holder.avatar, avatarTransformation, avaurl, Constants.PICASSO_TAG);

            holder.avatar.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.avatar.setOnClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onAvatarClick(message, message.getSenderId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (nonNull(onMessageActionListener)) {
                onMessageActionListener.onMessageClicked(message);
            }
        });

        holder.itemView.setOnLongClickListener(v -> nonNull(onMessageActionListener)
                && onMessageActionListener.onMessageLongClick(message));
    }

    private void bindNormalMessage(final MessageHolder holder, final Message message) {
        bindBaseMessageHolder(holder, message);

        holder.body.setVisibility(TextUtils.isEmpty(message.getBody()) ? View.GONE : View.VISIBLE);

        String displayedBody = null;

        switch (message.getCryptStatus()){
            case CryptStatus.NO_ENCRYPTION:
            case CryptStatus.ENCRYPTED:
            case CryptStatus.DECRYPT_FAILED:
                displayedBody = message.getBody();
                break;
            case CryptStatus.DECRYPTED:
                displayedBody = message.getDecryptedBody();
                break;
        }

        switch (message.getCryptStatus()){
            case CryptStatus.ENCRYPTED:
            case CryptStatus.DECRYPT_FAILED:
                holder.bubble.setBubbleAlpha(ENCRYPTED_MESSAGE_BUBBLE_ALPHA);
                break;
            case CryptStatus.NO_ENCRYPTION:
            case CryptStatus.DECRYPTED:
                holder.bubble.setBubbleAlpha(255);
                break;
        }

        holder.body.setText(OwnerLinkSpanFactory.withSpans(displayedBody, true, false, ownerLinkAdapter));
        holder.encryptedView.setVisibility(message.getCryptStatus() == CryptStatus.NO_ENCRYPTION ? View.GONE : View.VISIBLE);

        boolean hasAttachments = Utils.nonEmpty(message.getFwd()) || (nonNull(message.getAttachments()) && message.getAttachments().size() > 0);
        holder.attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);

        if (hasAttachments) {
            attachmentsViewBinder.displayAttachments(message.getAttachments(), holder.attachmentsHolder, true);
            attachmentsViewBinder.displayForwards(message.getFwd(), holder.forwardMessagesRoot, context, true);
        }
    }

    private void bindServiceHolder(ServiceMessageHolder holder, Message message) {
        holder.tvAction.setText(message.getServiceText(context));
        attachmentsViewBinder.displayAttachments(message.getAttachments(), holder.mAttachmentsHolder, true);
    }

    private void bindDeletedHolder(final DeletedMessageHolder holder, final Message message) {
        holder.buttonRestore.setOnClickListener(v -> {
            if (onMessageActionListener != null) {
                onMessageActionListener.onRestoreClick(message, holder.getAdapterPosition());
            }
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case TYPE_DELETED:
                return new DeletedMessageHolder(view);
            case TYPE_MY_MESSAGE:
            case TYPE_FRIEND_MESSAGE:
                return new MessageHolder(view);
            case TYPE_SERVICE:
                return new ServiceMessageHolder(view);
            case TYPE_STICKER_FRIEND:
            case TYPE_STICKER_MY:
                return new StickerMessageHolder(view);
        }

        return null;
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_MY_MESSAGE:
                return R.layout.item_message_my;
            case TYPE_FRIEND_MESSAGE:
                return R.layout.item_message_friend;
            case TYPE_SERVICE:
                return R.layout.item_service_message;
            case TYPE_DELETED:
                return R.layout.item_message_deleted;
            case TYPE_STICKER_FRIEND:
                return R.layout.item_message_friend_sticker;
            case TYPE_STICKER_MY:
                return R.layout.item_message_my_sticker;
        }

        throw new IllegalArgumentException();
    }

    @Override
    protected int getItemType(int position) {
        Message m = getItem(position - getHeadersCount());
        if (m.isDeleted()) {
            return TYPE_DELETED;
        }

        if (m.isServiseMessage()) {
            return TYPE_SERVICE;
        }

        if (m.isSticker()) {
            return m.isOut() ? TYPE_STICKER_MY : TYPE_STICKER_FRIEND;
        }

        return m.isOut() ? TYPE_MY_MESSAGE : TYPE_FRIEND_MESSAGE;
    }

    public void setVoiceActionListener(AttachmentsViewBinder.VoiceActionListener voiceActionListener) {
        this.attachmentsViewBinder.setVoiceActionListener(voiceActionListener);
    }

    public void configNowVoiceMessagePlaying(int voiceId, float progress, boolean paused, boolean amin) {
        attachmentsViewBinder.configNowVoiceMessagePlaying(voiceId, progress, paused, amin);
    }

    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        attachmentsViewBinder.bindVoiceHolderById(holderId, play, paused, progress, amin);
    }

    public void disableVoiceMessagePlaying() {
        attachmentsViewBinder.disableVoiceMessagePlaying();
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.onHashTagClickListener = onHashTagClickListener;
    }

    public void setOnMessageActionListener(OnMessageActionListener onMessageActionListener) {
        this.onMessageActionListener = onMessageActionListener;
    }

    public interface OnMessageActionListener {
        void onAvatarClick(@NonNull Message message, int userId);

        void onRestoreClick(@NonNull Message message, int position);

        boolean onMessageLongClick(@NonNull Message message);

        void onMessageClicked(@NonNull Message message);
    }

    private class StickerMessageHolder extends BaseMessageHolder {

        ImageView sticker;

        StickerMessageHolder(View itemView) {
            super(itemView);
            this.sticker = itemView.findViewById(R.id.sticker);
        }
    }

    private abstract class BaseMessageHolder extends RecyclerView.ViewHolder {

        TextView status;
        ImageView avatar;
        OnlineView important;

        BaseMessageHolder(View itemView) {
            super(itemView);
            this.status = itemView.findViewById(R.id.item_message_status_text);
            this.important = itemView.findViewById(R.id.item_message_important);
            this.avatar = itemView.findViewById(R.id.item_message_avatar);
        }
    }

    private class ServiceMessageHolder extends RecyclerView.ViewHolder {

        TextView tvAction;
        AttachmentsHolder mAttachmentsHolder;

        ServiceMessageHolder(View itemView) {
            super(itemView);
            tvAction = itemView.findViewById(R.id.item_service_message_text);

            mAttachmentsHolder = new AttachmentsHolder();
            mAttachmentsHolder.setVgAudios(itemView.findViewById(R.id.audio_attachments)).
                    setVgDocs(itemView.findViewById(R.id.docs_attachments)).
                    setVgPhotos(itemView.findViewById(R.id.photo_attachments)).
                    setVgPosts(itemView.findViewById(R.id.posts_attachments)).
                    setVgStickers(itemView.findViewById(R.id.stickers_attachments));
        }
    }

    private class DeletedMessageHolder extends RecyclerView.ViewHolder {

        Button buttonRestore;

        DeletedMessageHolder(View itemView) {
            super(itemView);
            buttonRestore = itemView.findViewById(R.id.item_messages_deleted_restore);
        }
    }

    private class MessageHolder extends BaseMessageHolder {

        View root;
        EmojiconTextView body;
        ViewGroup forwardMessagesRoot;
        BubbleLinearLayout bubble;
        View attachmentsRoot;
        AttachmentsHolder attachmentsHolder;
        View encryptedView;

        MessageHolder(View itemView) {
            super(itemView);
            encryptedView = itemView.findViewById(R.id.item_message_encrypted);

            body = itemView.findViewById(R.id.item_message_text);
            body.setMovementMethod(LinkMovementMethod.getInstance());
            body.setOnHashTagClickListener(onHashTagClickListener);
            body.setOnLongClickListener(v -> this.itemView.performLongClick());
            body.setOnClickListener(v -> this.itemView.performClick());

            root = itemView.findViewById(R.id.message_container);
            forwardMessagesRoot = itemView.findViewById(R.id.forward_messages);
            bubble = itemView.findViewById(R.id.item_message_bubble);

            attachmentsRoot = itemView.findViewById(R.id.item_message_attachment_container);
            attachmentsHolder = new AttachmentsHolder();
            attachmentsHolder.setVgAudios(attachmentsRoot.findViewById(R.id.audio_attachments))
                    .setVgDocs(attachmentsRoot.findViewById(R.id.docs_attachments))
                    .setVgPhotos(attachmentsRoot.findViewById(R.id.photo_attachments))
                    .setVgPosts(attachmentsRoot.findViewById(R.id.posts_attachments))
                    .setVoiceMessageRoot(attachmentsRoot.findViewById(R.id.voice_message_attachments));
        }
    }
}

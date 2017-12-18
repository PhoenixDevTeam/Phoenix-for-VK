package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.holder.IdentificableHolder;
import biz.dealnote.messenger.adapter.holder.SharedHolders;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.link.internal.LinkActionAdapter;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.model.Attachments;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.WaveFormView;
import biz.dealnote.messenger.view.emoji.EmojiconTextView;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.dpToPx;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;
import static biz.dealnote.messenger.util.Utils.safeLenghtOf;

public class AttachmentsViewBinder {

    private static final int PREFFERED_STICKER_SIZE = 120;
    private static int sHolderIdCounter;

    private PhotosViewHelper photosViewHelper;
    private Transformation mAvatarTransformation;

    private int mActiveWaveFormColor;
    private int mNoactiveWaveFormColor;
    private SharedHolders<VoiceHolder> mVoiceSharedHolders;

    private VoiceActionListener mVoiceActionListener;
    private OnAttachmentsActionCallback mAttachmentsActionCallback;
    private EmojiconTextView.OnHashTagClickListener mOnHashTagClickListener;
    private Context mContext;

    public AttachmentsViewBinder(Context context, @NonNull OnAttachmentsActionCallback attachmentsActionCallback) {
        this.mContext = context;
        this.mVoiceSharedHolders = new SharedHolders<>(true);
        this.mAvatarTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.photosViewHelper = new PhotosViewHelper(context, attachmentsActionCallback);
        this.mAttachmentsActionCallback = attachmentsActionCallback;
        this.mActiveWaveFormColor = CurrentTheme.getIconColorActive(context);
        this.mNoactiveWaveFormColor = Utils.adjustAlpha(mActiveWaveFormColor, 0.5f);
    }

    private static void safeSetVisibitity(@Nullable View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    private static int generateHolderId(){
        sHolderIdCounter++;
        return sHolderIdCounter;
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.mOnHashTagClickListener = onHashTagClickListener;
    }

    public void displayAttachments(Attachments attachments, AttachmentsHolder containers, boolean postsAsLinks) {
        if (attachments == null) {
            safeSetVisibitity(containers.getVgAudios(), View.GONE);
            safeSetVisibitity(containers.getVgDocs(), View.GONE);
            safeSetVisibitity(containers.getVgPhotos(), View.GONE);
            safeSetVisibitity(containers.getVgPosts(), View.GONE);
            safeSetVisibitity(containers.getVgStickers(), View.GONE);
            safeSetVisibitity(containers.getVoiceMessageRoot(), View.GONE);
            safeSetVisibitity(containers.getVgFriends(), View.GONE);
        } else {
            displayAudios(attachments.getAudios(), containers.getVgAudios());
            displayVoiceMessages(attachments.getVoiceMessages(), containers.getVoiceMessageRoot());
            displayDocs(attachments.getDocLinks(postsAsLinks), containers.getVgDocs());
            //displayNewFriends(friends, containers.getVgFriends());

            if(containers.getVgStickers() != null ){
                displayStickers(attachments.getStickers(), containers.getVgStickers());
            }

            photosViewHelper.displayPhotos(attachments.getPostImages(), containers.getVgPhotos());
        }
    }

    /*private void displayNewFriends(ArrayList<String> friends, ViewGroup container){
        if (Objects.isNull(friends) || Objects.isNull(container)) return;
        boolean empty = safeIsEmpty(friends);
        Logger.d("DISPLAYING FRIENDS", friends.size() + "");

        container.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        int i = friends.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_feed_new_friend, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);

            if (g < friends.size()) {
                ImageView avatar = (ImageView) root.findViewById(R.id.item_feed_new_friend_avatar);
                TextView name = (TextView) root.findViewById(R.id.item_feed_new_friend_name);

                VKApiOwner user = OwnersHelper.loadOwnerNameAndPhoto(mContext, Accounts.getCurrentUid(mContext), Integer.parseInt(friends.get(g)));

                PicassoInstance.with()
                        .load(user.get100photoOrSmaller())
                        .transform(new RoundTransformation())
                        .into(avatar);
                name.setText(user.getFullName());
                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }

    }*/

    private void displayVoiceMessages(final ArrayList<VoiceMessage> voices, ViewGroup container) {
        if(Objects.isNull(container)) return;

        boolean empty = safeIsEmpty(voices);
        container.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            return;
        }

        int i = voices.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_voice_message, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);

            if (g < voices.size()) {
                VoiceHolder holder = (VoiceHolder) root.getTag();
                if(holder == null){
                    holder = new VoiceHolder(root);
                    root.setTag(holder);
                }

                final VoiceMessage voice = voices.get(g);
                bindVoiceHolder(holder, voice);

                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
    }

    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin){
        VoiceHolder holder = mVoiceSharedHolders.findHolderByHolderId(holderId);
        if(nonNull(holder)){
            bindVoiceHolderPlayState(holder, play, paused, progress, amin);
        }
    }

    public void disableVoiceMessagePlaying(){
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);
            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, false, false, 0f, false);
                }
            }
        }
    }

    private void bindVoiceHolderPlayState(VoiceHolder holder, boolean play, boolean paused, float progress, boolean anim) {
        @DrawableRes
        int icon = play && !paused ? R.drawable.pause : R.drawable.play;

        holder.mButtonPlay.setImageResource(icon);
        holder.mWaveFormView.setCurrentActiveProgress(play ? progress : 1.0f, anim);
    }

    public void configNowVoiceMessagePlaying(int voiceMessageId, float progress, boolean paused, boolean amin) {
        SparseArray<Set<WeakReference<VoiceHolder>>> holders = mVoiceSharedHolders.getCache();
        for (int i = 0; i < holders.size(); i++) {
            int key = holders.keyAt(i);

            boolean play = key == voiceMessageId;

            Set<WeakReference<VoiceHolder>> set = holders.get(key);
            for (WeakReference<VoiceHolder> reference : set) {
                VoiceHolder holder = reference.get();
                if (nonNull(holder)) {
                    bindVoiceHolderPlayState(holder, play, paused, progress, amin);
                }
            }
        }
    }

    public void setVoiceActionListener(VoiceActionListener voiceActionListener) {
        this.mVoiceActionListener = voiceActionListener;
    }

    private static final byte[] DEFAUL_WAVEFORM = {0,0,0,0,0,0,0,0,0,0,0,0,0};

    private void bindVoiceHolder(@NonNull VoiceHolder holder, @NonNull VoiceMessage voice) {
        int voiceMessageId = voice.getId();
        mVoiceSharedHolders.put(voiceMessageId, holder);

        holder.mDurationText.setText(AppTextUtils.getDurationString(voice.getDuration()));

        // can bee NULL/empty
        if(nonNull(voice.getWaveform()) && voice.getWaveform().length > 0){
            holder.mWaveFormView.setWaveForm(voice.getWaveform());
        } else {
            holder.mWaveFormView.setWaveForm(DEFAUL_WAVEFORM);
        }

        holder.mButtonPlay.setOnClickListener(v -> {
            if(nonNull(mVoiceActionListener)){
                mVoiceActionListener.onVoicePlayButtonClick(holder.getHolderId(), voiceMessageId, voice);
            }
        });

        if (nonNull(mVoiceActionListener)) {
            mVoiceActionListener.onVoiceHolderBinded(voiceMessageId, holder.getHolderId());
        }
    }

    private static final class CopyHolder {

        final ViewGroup itemView;
        final ImageView ivAvatar;
        final TextView ownerName;
        final EmojiconTextView bodyView;
        final View tvShowMore;
        final View buttonDots;
        final AttachmentsHolder attachmentsHolder;

        CopyHolder(ViewGroup itemView, OnAttachmentsActionCallback callback){
            this.itemView = itemView;
            this.bodyView = itemView.findViewById(R.id.item_post_copy_text);
            this.ivAvatar = itemView.findViewById(R.id.item_copy_history_post_avatar);
            this.tvShowMore = itemView.findViewById(R.id.item_post_copy_show_more);
            this.ownerName = itemView.findViewById(R.id.item_post_copy_owner_name);
            this.buttonDots = itemView.findViewById(R.id.item_copy_history_post_dots);
            this.attachmentsHolder = AttachmentsHolder.forCopyPost(itemView);
            this.callback = callback;

            this.buttonDots.setOnClickListener(v -> showDotsMenu());
        }

        final OnAttachmentsActionCallback callback;

        void showDotsMenu(){
            PopupMenu menu = new PopupMenu(itemView.getContext(), buttonDots);
            menu.getMenu().add(R.string.open_post).setOnMenuItemClickListener(item -> {
                Post copy = (Post) buttonDots.getTag();
                callback.onPostOpen(copy);
                return true;
            });

            menu.show();
        }
    }

    public void displayCopyHistory(List<Post> posts, ViewGroup container, boolean reduce, int layout) {
        if (container != null) {
            container.setVisibility(safeIsEmpty(posts) ? View.GONE : View.VISIBLE);
        }

        if (safeIsEmpty(posts) || container == null) {
            return;
        }

        int i = posts.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            View itemView = LayoutInflater.from(container.getContext()).inflate(layout, container, false);
            CopyHolder holder = new CopyHolder((ViewGroup) itemView, mAttachmentsActionCallback);
            itemView.setTag(holder);

            if (!reduce) {
                holder.bodyView.setAutoLinkMask(Linkify.WEB_URLS);
                holder.bodyView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            container.addView(itemView);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup postViewGroup = (ViewGroup) container.getChildAt(g);

            if (g < posts.size()) {
                final CopyHolder holder = (CopyHolder) postViewGroup.getTag();
                final Post copy = posts.get(g);

                if (isNull(copy)) {
                    postViewGroup.setVisibility(View.GONE);
                    return;
                }

                postViewGroup.setVisibility(View.VISIBLE);

                String text = reduce ? AppTextUtils.reduceStringForPost(copy.getText()) : copy.getText();

                holder.bodyView.setVisibility(isEmpty(copy.getText()) ? View.GONE : View.VISIBLE);
                holder.bodyView.setOnHashTagClickListener(mOnHashTagClickListener);
                holder.bodyView.setText(OwnerLinkSpanFactory.withSpans(text, true, false, new LinkActionAdapter() {
                    @Override
                    public void onOwnerClick(int ownerId) {
                        mAttachmentsActionCallback.onOpenOwner(ownerId);
                    }
                }));

                holder.ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(copy.getAuthorId()));
                ViewUtils.displayAvatar(holder.ivAvatar, mAvatarTransformation, copy.getAuthorPhoto(), Constants.PICASSO_TAG);

                holder.tvShowMore.setVisibility(reduce && safeLenghtOf(copy.getText()) > 400 ? View.VISIBLE : View.GONE);
                holder.ownerName.setText(copy.getAuthorName());
                holder.buttonDots.setTag(copy);

                displayAttachments(copy.getAttachments(), holder.attachmentsHolder, false);
            } else {
                postViewGroup.setVisibility(View.GONE);
            }
        }
    }

    public void displayForwards(List<Message> fwds, final ViewGroup fwdContainer, final Context context, boolean postsAsLinks) {
        fwdContainer.setVisibility(safeIsEmpty(fwds) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(fwds)) {
            return;
        }

        final int i = fwds.size() - fwdContainer.getChildCount();
        for (int j = 0; j < i; j++) {
            View localView = LayoutInflater.from(context).inflate(R.layout.item_forward_message, fwdContainer, false);
            fwdContainer.addView(localView);
        }

        for (int g = 0; g < fwdContainer.getChildCount(); g++) {
            final ViewGroup itemView = (ViewGroup) fwdContainer.getChildAt(g);
            if (g < fwds.size()) {
                final Message message = fwds.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(message);

                TextView tvBody = itemView.findViewById(R.id.item_fwd_message_text);
                tvBody.setText(message.getBody());
                tvBody.setVisibility(message.getBody() == null || message.getBody().length() == 0 ? View.GONE : View.VISIBLE);

                ((TextView) itemView.findViewById(R.id.item_fwd_message_username)).setText(message.getSender().getFullName());
                ((TextView) itemView.findViewById(R.id.item_fwd_message_time)).setText(AppTextUtils.getDateFromUnixTime(message.getDate()));
                TextView tvFwds = itemView.findViewById(R.id.item_forward_message_fwds);
                tvFwds.setVisibility(message.getForwardMessagesCount() > 0 ? View.VISIBLE : View.GONE);

                tvFwds.setOnClickListener(v -> mAttachmentsActionCallback.onForwardMessagesOpen(message.getFwd()));

                final ImageView ivAvatar = itemView.findViewById(R.id.item_fwd_message_avatar);

                String senderPhotoUrl = message.getSender() == null ? null : message.getSender().getMaxSquareAvatar();
                ViewUtils.displayAvatar(ivAvatar, mAvatarTransformation, senderPhotoUrl, Constants.PICASSO_TAG);

                ivAvatar.setOnClickListener(v -> mAttachmentsActionCallback.onOpenOwner(message.getSenderId()));

                AttachmentsHolder attachmentContainers = new AttachmentsHolder();
                attachmentContainers.setVgAudios(itemView.findViewById(R.id.audio_attachments)).
                        setVgDocs(itemView.findViewById(R.id.docs_attachments)).
                        setVgPhotos(itemView.findViewById(R.id.photo_attachments)).
                        setVgPosts(itemView.findViewById(R.id.posts_attachments)).
                        setVgStickers(itemView.findViewById(R.id.stickers_attachments)).
                        setVoiceMessageRoot(itemView.findViewById(R.id.voice_message_attachments));

                displayAttachments(message.getAttachments(), attachmentContainers, postsAsLinks);
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void displayStickers(List<Sticker> stickers, ViewGroup stickersContainer) {
        stickersContainer.setVisibility(safeIsEmpty(stickers) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(stickers)) {
            return;
        }

        if (stickersContainer.getChildCount() == 0) {
            ImageView localView = new ImageView(mContext);
            stickersContainer.addView(localView);
        }

        ImageView imageView = (ImageView) stickersContainer.getChildAt(0);
        Sticker sticker = stickers.get(0);

        boolean horisontal = sticker.getHeight() < sticker.getWidth();
        double proporsion = (double) sticker.getWidth() / (double) sticker.getHeight();

        float finalWidth;
        float finalHeihgt;

        if (horisontal) {
            finalWidth = dpToPx(PREFFERED_STICKER_SIZE, mContext);
            finalHeihgt = (float) (finalWidth / proporsion);
        } else {
            finalHeihgt = dpToPx(PREFFERED_STICKER_SIZE, mContext);
            finalWidth = (float) (finalHeihgt * proporsion);
        }

        imageView.getLayoutParams().height = (int) finalHeihgt;
        imageView.getLayoutParams().width = (int) finalWidth;

        PicassoInstance.with()
                .load(sticker.getPhoto256())
                .into(imageView);

    }

    private void displayDocs(List<DocLink> docs, ViewGroup root) {
        root.setVisibility(safeIsEmpty(docs) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(docs)) {
            return;
        }

        int i = docs.size() - root.getChildCount();
        for (int j = 0; j < i; j++) {
            root.addView(LayoutInflater.from(mContext).inflate(R.layout.item_document, root, false));
        }

        for (int g = 0; g < root.getChildCount(); g++) {
            ViewGroup itemView = (ViewGroup) root.getChildAt(g);
            if (g < docs.size()) {
                final DocLink doc = docs.get(g);
                itemView.setVisibility(View.VISIBLE);
                itemView.setTag(doc);

                TextView tvTitle = itemView.findViewById(R.id.item_document_title);
                TextView tvDetails = itemView.findViewById(R.id.item_document_ext_size);
                ImageView ivPhoto = itemView.findViewById(R.id.item_document_image);
                ImageView ivType = itemView.findViewById(R.id.item_document_type);

                String title = doc.getTitle(mContext);
                String details = doc.getSecondaryText(mContext);

                String imageUrl = doc.getImageUrl();
                String ext = doc.getExt() == null ? "" : doc.getExt() + ", ";

                String subtitle = ext + details;

                tvTitle.setText(title);
                tvDetails.setText(subtitle);

                itemView.setOnClickListener(v -> openDocLink(doc));

                switch (doc.getType()){
                    case Types.DOC:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivPhoto.setVisibility(View.VISIBLE);
                            PicassoInstance.with()
                                    .load(imageUrl)
                                    .into(ivPhoto);
                        } else {
                            ivType.setVisibility(View.VISIBLE);
                            ivPhoto.setVisibility(View.GONE);
                            ivType.getBackground().setColorFilter(CurrentTheme.getIconColorActive(mContext), PorterDuff.Mode.MULTIPLY);
                            ivType.setImageResource(R.drawable.file);
                        }
                        break;
                    case Types.POST:
                        if (imageUrl != null) {
                            ivType.setVisibility(View.GONE);
                            ivPhoto.setVisibility(View.VISIBLE);
                            PicassoInstance.with()
                                    .load(imageUrl)
                                    .into(ivPhoto);
                        } else {
                            ivPhoto.setVisibility(View.GONE);
                            ivType.setVisibility(View.GONE);
                        }
                        break;
                    case Types.LINK:
                    case Types.WIKI_PAGE:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhoto.setVisibility(View.GONE);
                        ivType.getBackground().setColorFilter(CurrentTheme.getIconColorActive(mContext), PorterDuff.Mode.MULTIPLY);
                        ivType.setImageResource(R.drawable.share);
                        break;
                    case Types.POLL:
                        ivType.setVisibility(View.VISIBLE);
                        ivPhoto.setVisibility(View.GONE);
                        ivType.getBackground().setColorFilter(CurrentTheme.getIconColorActive(mContext), PorterDuff.Mode.MULTIPLY);
                        ivType.setImageResource(R.drawable.chart_bar);
                        break;
                    default:
                        ivType.setVisibility(View.GONE);
                        ivPhoto.setVisibility(View.GONE);
                        break;
                }

            } else {
                itemView.setVisibility(View.GONE);
                itemView.setTag(null);
            }
        }
    }

    private void openDocLink(DocLink link) {
        switch (link.getType()) {
            case Types.DOC:
                mAttachmentsActionCallback.onDocPreviewOpen((Document) link.attachment);
                break;
            case Types.POST:
                mAttachmentsActionCallback.onPostOpen((Post) link.attachment);
                break;
            case Types.LINK:
                mAttachmentsActionCallback.onLinkOpen((Link) link.attachment);
                break;
            case Types.POLL:
                mAttachmentsActionCallback.onPollOpen((Poll) link.attachment);
                break;
            case Types.WIKI_PAGE:
                mAttachmentsActionCallback.onWikiPageOpen((WikiPage) link.attachment);
                break;
        }
    }

    /**
     * Отображение аудиозаписей
     *
     * @param audios    аудиозаписи
     * @param container контейнер для аудиозаписей
     */
    private void displayAudios(final ArrayList<Audio> audios, ViewGroup container) {
        container.setVisibility(safeIsEmpty(audios) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(audios)) {
            return;
        }

        int i = audios.size() - container.getChildCount();
        for (int j = 0; j < i; j++) {
            container.addView(LayoutInflater.from(mContext).inflate(R.layout.item_small_audio, container, false));
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            ViewGroup root = (ViewGroup) container.getChildAt(g);
            if (g < audios.size()) {
                final Audio audio = audios.get(g);

                AudioHolder holder = new AudioHolder(mContext, root);

                holder.tvTitle.setText(audio.getArtist());
                holder.tvSubtitle.setText(audio.getTitle());

                int finalG = g;
                holder.ibPlay.setOnClickListener(v -> mAttachmentsActionCallback.onAudioPlay(finalG, audios));

                root.setVisibility(View.VISIBLE);
                root.setTag(audio);
            } else {
                root.setVisibility(View.GONE);
                root.setTag(null);
            }
        }
    }

    public interface VoiceActionListener extends EventListener {
        void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId);
        void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage);
    }

    public interface OnAttachmentsActionCallback {
        void onPollOpen(@NonNull Poll poll);

        void onVideoPlay(@NonNull Video video);

        void onAudioPlay(int position, @NonNull ArrayList<Audio> audios);

        void onForwardMessagesOpen(@NonNull ArrayList<Message> messages);

        void onOpenOwner(int userId);

        void onDocPreviewOpen(@NonNull Document document);

        void onPostOpen(@NonNull Post post);

        void onLinkOpen(@NonNull Link link);

        void onWikiPageOpen(@NonNull WikiPage page);

        void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index);
    }

    private static class AudioHolder {

        TextView tvTitle;
        TextView tvSubtitle;
        ImageView ibPlay;

        AudioHolder(Context context, View root) {
            tvTitle = root.findViewById(R.id.dialog_message);
            tvSubtitle = root.findViewById(R.id.item_audio_subtitle);
            ibPlay = root.findViewById(R.id.item_audio_play);
            ibPlay.getBackground().setColorFilter(CurrentTheme.getIconColorActive(context), PorterDuff.Mode.MULTIPLY);
        }
    }

    private class VoiceHolder implements IdentificableHolder {

        WaveFormView mWaveFormView;
        ImageView mButtonPlay;
        TextView mDurationText;

        VoiceHolder(View itemView) {
            mWaveFormView = itemView.findViewById(R.id.item_voice_wave_form_view);
            mWaveFormView.setActiveColor(mActiveWaveFormColor);
            mWaveFormView.setNoactiveColor(mNoactiveWaveFormColor);
            mWaveFormView.setSectionCount(Utils.isLandscape(itemView.getContext()) ? 128 : 64);
            mWaveFormView.setTag(generateHolderId());

            mButtonPlay = itemView.findViewById(R.id.item_voice_button_play);
            mButtonPlay.getBackground().setColorFilter(mActiveWaveFormColor, PorterDuff.Mode.MULTIPLY);

            mDurationText = itemView.findViewById(R.id.item_voice_duration);
        }

        @Override
        public int getHolderId(){
            return (int) mWaveFormView.getTag();
        }
    }
}

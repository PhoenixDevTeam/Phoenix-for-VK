package biz.dealnote.messenger.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.ContextMenu;
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
import biz.dealnote.messenger.link.internal.LinkActionAdapter;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.link.internal.TopicLink;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.WeakViewAnimatorAdapter;
import biz.dealnote.messenger.view.emoji.EmojiconTextView;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

public class CommentsAdapter extends RecyclerBindableAdapter<Comment, RecyclerView.ViewHolder> {

    private Context context;
    private AttachmentsViewBinder attachmentsViewBinder;
    private Transformation transformation;
    private int colorTextSecondary;
    private int iconColorActive;
    private EmojiconTextView.OnHashTagClickListener onHashTagClickListener;

    public CommentsAdapter(Context context, List<Comment> items, AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        super(items);
        this.context = context;
        this.attachmentsViewBinder = new AttachmentsViewBinder(context, attachmentsActionCallback);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
        this.colorTextSecondary = CurrentTheme.getSecondaryTextColorCode(context);
        this.iconColorActive = CurrentTheme.getIconColorActive(context);
    }

    public void setOnHashTagClickListener(EmojiconTextView.OnHashTagClickListener onHashTagClickListener) {
        this.onHashTagClickListener = onHashTagClickListener;
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        switch (type){
            case TYPE_NORMAL:
                bindNormalHolder((NormalCommentHoler) viewHolder, getItem(position));
                break;
            case TYPE_DELETED:
                bindDeletedComment((DeletedHolder) viewHolder, getItem(position));
                break;
        }
    }

    private void bindDeletedComment(DeletedHolder holder, final Comment comment){
        holder.buttonRestore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestoreComment(comment.getId());
            }
        });
    }

    private void bindNormalHolder(final NormalCommentHoler holder, final Comment comment){
        holder.cancelSelectionAnimation();

        if (comment.isAnimationNow()) {
            holder.startSelectionAnimation();
            comment.setAnimationNow(false);
        }

        if (!comment.hasAttachments()) {
            holder.vAttachmentsRoot.setVisibility(View.GONE);
        } else {
            holder.vAttachmentsRoot.setVisibility(View.VISIBLE);
            attachmentsViewBinder.displayAttachments(comment.getAttachments(), holder.attachmentContainers, true);
        }

        holder.tvOwnerName.setText(comment.getFullAuthorName());

        Spannable text = OwnerLinkSpanFactory.withSpans(comment.getText(), true, true, new LinkActionAdapter() {
            @Override
            public void onTopicLinkClicked(TopicLink link) {
                onReplyClick(link.replyToOwner, link.replyToCommentId);
            }

            @Override
            public void onOwnerClick(int ownerId) {
                if(listener != null){
                    listener.onAvatarClick(ownerId);
                }
            }
        });

        holder.tvText.setText(text, TextView.BufferType.SPANNABLE);
        holder.tvText.setVisibility(TextUtils.isEmpty(comment.getText()) ? View.GONE : View.VISIBLE);
        holder.tvText.setMovementMethod(LinkMovementMethod.getInstance());

        holder.ivLike.setVisibility(comment.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.ivLike.setColorFilter(comment.isUserLikes() ? iconColorActive : colorTextSecondary, PorterDuff.Mode.MULTIPLY);
        holder.tvLikeCounter.setText(String.valueOf(comment.getLikesCount()));
        holder.tvLikeCounter.setVisibility(comment.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        holder.tvLikeCounter.setTextColor(comment.isUserLikes() ? iconColorActive : colorTextSecondary);

        holder.tvTime.setMovementMethod(LinkMovementMethod.getInstance());

        ViewUtils.displayAvatar(holder.ivOwnerAvatar, transformation, comment.getMaxAuthorAvaUrl(), Constants.PICASSO_TAG);

        holder.tvTime.setText(genTimeAndReplyText(comment), TextView.BufferType.SPANNABLE);
        holder.tvTime.setTextColor(colorTextSecondary);

        holder.ivLike.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCommentLikeClick(comment, !comment.isUserLikes());
            }
        });

        holder.ivOwnerAvatar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAvatarClick(comment.getFromId());
            }
        });
    }

    private Spannable genTimeAndReplyText(final Comment comment) {
        String time = AppTextUtils.getDateFromUnixTime(comment.getDate());
        if (comment.getReplyToUser() == 0) {
            return Spannable.Factory.getInstance().newSpannable(time);
        }

        String commentText = context.getString(R.string.comment).toLowerCase();
        String target = context.getString(R.string.in_response_to, time, commentText);

        int start = target.indexOf(commentText);

        Spannable spannable = Spannable.Factory.getInstance().newSpannable(target);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                onReplyClick(comment.getReplyToUser(), comment.getReplyToComment());
            }
        };

        spannable.setSpan(span, start, target.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private void onReplyClick(int ownerId, int commentId) {
        if (listener != null) {
            listener.onReplyToOwnerClick(ownerId, commentId);
        }
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type){
            case TYPE_NORMAL:
                return new NormalCommentHoler(view);
            case TYPE_DELETED:
                return new DeletedHolder(view);
            default:
                return null;
        }
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_DELETED:
                return R.layout.item_comment_deleted;
            case TYPE_NORMAL:
                return R.layout.item_comment;
        }

        throw new IllegalArgumentException();
    }

    private static final int TYPE_DELETED = 0;
    private static final int TYPE_NORMAL = 1;

    private class DeletedHolder extends RecyclerView.ViewHolder {

        Button buttonRestore;

        DeletedHolder(View itemView) {
            super(itemView);
            buttonRestore = itemView.findViewById(R.id.item_comment_deleted_restore);
        }
    }

    private class NormalCommentHoler extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        TextView tvOwnerName;
        ImageView ivOwnerAvatar;
        EmojiconTextView tvText;
        TextView tvTime;
        ImageView ivLike;
        TextView tvLikeCounter;
        View selectionView;
        View vAttachmentsRoot;

        AttachmentsHolder attachmentContainers;

        NormalCommentHoler(View root) {
            super(root);
            ivOwnerAvatar = root.findViewById(R.id.item_comment_owner_avatar);
            tvOwnerName = root.findViewById(R.id.item_comment_owner_name);
            tvText = root.findViewById(R.id.item_comment_text);

            tvText.setOnHashTagClickListener(hashTag -> {
                if(nonNull(onHashTagClickListener)){
                    onHashTagClickListener.onHashTagClicked(hashTag);
                }
            });

            tvTime = root.findViewById(R.id.item_comment_time);
            ivLike = root.findViewById(R.id.item_comment_like);
            tvLikeCounter = root.findViewById(R.id.item_comment_like_counter);
            selectionView = root.findViewById(R.id.item_comment_selection);
            selectionView.setBackgroundColor(CurrentTheme.getIconColorActive(context));
            ivLike.setColorFilter(CurrentTheme.getSecondaryTextColorCode(context), PorterDuff.Mode.MULTIPLY);
            vAttachmentsRoot = root.findViewById(R.id.item_comment_attachments_root);

            itemView.setOnCreateContextMenuListener(this);

            attachmentContainers = AttachmentsHolder.forComment((ViewGroup) vAttachmentsRoot);
            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.INVISIBLE);
                }
            };
        }

        Animator.AnimatorListener animationAdapter;

        ObjectAnimator animator;

        void startSelectionAnimation(){
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(1500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void cancelSelectionAnimation(){
            if(animator != null){
                animator.cancel();
                animator = null;
            }

            selectionView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if(isNull(recyclerView)) return;

            int position = recyclerView.getChildAdapterPosition(v) - getHeadersCount();
            if(listener != null){
                listener.populateCommentContextMenu(menu, getItem(position));
            }
        }
    }

    private OnCommentActionListener listener;

    public void setListener(OnCommentActionListener listener) {
        this.listener = listener;
    }

    public interface OnCommentActionListener {
        void onReplyToOwnerClick(int ownerId, int commentId);
        void onRestoreComment(int commentId);
        void onAvatarClick(int ownerId);
        void onCommentLikeClick(Comment comment, boolean add);
        void populateCommentContextMenu(ContextMenu menu, Comment comment);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    private RecyclerView recyclerView;

    @Override
    protected int getItemType(int position) {
        return getItem(position - getHeadersCount()).isDeleted() ? TYPE_DELETED : TYPE_NORMAL;
    }
}
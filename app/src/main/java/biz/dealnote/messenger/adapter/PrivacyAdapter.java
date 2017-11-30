package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;
import java.util.EventListener;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.api.model.VkApiPrivacy;
import biz.dealnote.messenger.model.FriendList;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Utils;

public class PrivacyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ENTRY = 0;
    private static final int TYPE_TITLE = 1;

    private Context mContext;
    private Privacy mPrivacy;

    public PrivacyAdapter(Context context, Privacy privacy) {
        this.mContext = context;
        this.mPrivacy = privacy;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case TYPE_ENTRY:
                return new EntryViewHolder(inflater.inflate(R.layout.item_privacy_entry, parent, false));
            case TYPE_TITLE:
                return new TitleViewHolder(inflater.inflate(R.layout.item_privacy_title, parent, false));
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            bindTitle((TitleViewHolder) holder);
            return;
        }

        if (position <= mPrivacy.getAllowedUsers().size()) {
            int allowedUserIndex = position - 1;
            bindUserEntry((EntryViewHolder) holder, mPrivacy.getAllowedUsers().get(allowedUserIndex), true);
            return;
        }

        if (position <= count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists())) {
            int allowedListIndex = position - count(mPrivacy.getAllowedUsers()) - 1;
            bindListEntry((EntryViewHolder) holder, mPrivacy.getAllowedLists().get(allowedListIndex), true);
            return;
        }

        if (position <= count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists(), mPrivacy.getDisallowedUsers()) + 1) {
            int excludedUserIndex = position - count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists()) - 2;
            bindUserEntry((EntryViewHolder) holder, mPrivacy.getDisallowedUsers().get(excludedUserIndex), false);
            return;
        }

        int excludedListIndex = position - count(
                mPrivacy.getAllowedUsers(),
                mPrivacy.getAllowedLists(),
                mPrivacy.getDisallowedUsers()) - 2;

        bindListEntry((EntryViewHolder) holder, mPrivacy.getDisallowedLists().get(excludedListIndex), false);
    }

    private int count(Collection<?>... collection) {
        return Utils.safeCountOfMultiple(collection);
    }

    private void bindTitle(TitleViewHolder holder) {
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            holder.itemView.setLayoutParams(layoutParams);
        }

        final int position = holder.getAdapterPosition();
        if (position == 0) {
            String title = mContext.getString(getTypeTitle());
            String fullText = mContext.getString(R.string.who_can_have_access) + " " + title;
            Spannable spannable = SpannableStringBuilder.valueOf(fullText);
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if(mActionListener != null){
                        mActionListener.onTypeClick();
                    }
                }
            };

            spannable.setSpan(span, fullText.length() - title.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.title.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            holder.title.setText(R.string.who_cannot_have_access);
        }

        holder.buttonAdd.setOnClickListener(v -> {
            if(mActionListener != null){
                if(position == 0){
                    mActionListener.onAddToAllowedClick();
                } else {
                    mActionListener.onAddToDisallowedClick();
                }
            }
        });
    }

    private int getTypeTitle() {
        switch (mPrivacy.getType()) {
            default:
                return R.string.privacy_to_all_users;
            case VkApiPrivacy.Type.FRIENDS:
                return R.string.privacy_to_friends_only;
            case VkApiPrivacy.Type.FRIENDS_OF_FRIENDS:
                return R.string.privacy_to_friends_and_friends_of_friends;
            case VkApiPrivacy.Type.ONLY_ME:
                return R.string.privacy_to_only_me;
        }
    }

    private void bindListEntry(EntryViewHolder holder, final FriendList friendList, final boolean allow) {
        holder.avatar.setColorFilter(CurrentTheme.getColorAccent(mContext));

        PicassoInstance.with()
                .load(R.drawable.ic_privacy_friends_list)
                .into(holder.avatar);

        holder.title.setText(friendList.getName());
        holder.buttonRemove.setOnClickListener(v -> {
            if(mActionListener != null){
                if(allow){
                    mActionListener.onAllowedFriendsListRemove(friendList);
                } else {
                    mActionListener.onDisallowedFriendsListRemove(friendList);
                }
            }
        });
    }

    private void bindUserEntry(EntryViewHolder holder, final User user, final boolean allow) {
        holder.avatar.setColorFilter(null);

        PicassoInstance.with()
                .load(user.getMaxSquareAvatar())
                .into(holder.avatar);
        holder.title.setText(user.getFullName());
        holder.buttonRemove.setOnClickListener(v -> {
            if(mActionListener != null){
                if (allow){
                    mActionListener.onAllowedUserRemove(user);
                } else {
                    mActionListener.onDisallowedUserRemove(user);
                }
            }
        });
    }

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.mLayoutManager = recyclerView.getLayoutManager();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.mLayoutManager = null;
    }

    @Override
    public int getItemCount() {
        // 2 titles
        return 2 + count(mPrivacy.getAllowedUsers(),
                mPrivacy.getAllowedLists(),
                mPrivacy.getDisallowedUsers(),
                mPrivacy.getDisallowedLists());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TITLE;
        }

        if (position == count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists()) + 1) {
            return TYPE_TITLE;
        }

        return TYPE_ENTRY;
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        View buttonAdd;

        TitleViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.title.setMovementMethod(LinkMovementMethod.getInstance());
            this.buttonAdd = itemView.findViewById(R.id.button_add);
        }
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;
        View buttonRemove;
        TextView title;

        EntryViewHolder(View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.avatar);
            this.buttonRemove = itemView.findViewById(R.id.button_remove);
            this.title = itemView.findViewById(R.id.name);
        }
    }

    private ActionListener mActionListener;

    public void setActionListener(ActionListener actionListener){
        this.mActionListener = actionListener;
    }

    public interface ActionListener extends EventListener {
        void onTypeClick();
        void onAllowedUserRemove(User user);
        void onAllowedFriendsListRemove(FriendList friendList);
        void onDisallowedUserRemove(User user);
        void onDisallowedFriendsListRemove(FriendList friendList);
        void onAddToAllowedClick();
        void onAddToDisallowedClick();
    }
}
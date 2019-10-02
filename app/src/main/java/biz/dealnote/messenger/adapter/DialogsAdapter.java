package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Transformation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.ChatAction;
import biz.dealnote.messenger.model.Dialog;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserPlatform;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.OnlineView;

/**
 * Created by hp-dv6 on 05.06.2016.
 * VKMessenger
 */
public class DialogsAdapter extends RecyclerView.Adapter<DialogsAdapter.DialogViewHolder> {

    private static final SimpleDateFormat DF_TODAY = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DF_OLD = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());

    private Context mContext;
    private List<Dialog> mDialogs;
    private static final Date DATE = new Date();
    private Transformation mTransformation;
    private ForegroundColorSpan mForegroundColorSpan;
    private long mStartOfToday;
    private RecyclerView.AdapterDataObserver mDataObserver;

    public DialogsAdapter(Context context, @NonNull List<Dialog> dialogs) {
        this.mContext = context;
        this.mDialogs = dialogs;
        this.mTransformation = CurrentTheme.createTransformationForAvatar(context);
        this.mForegroundColorSpan = new ForegroundColorSpan(CurrentTheme.getPrimaryTextColorCode(context));
        this.mDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                initStartOfTodayDate();
            }
        };

        registerAdapterDataObserver(mDataObserver);
        initStartOfTodayDate();
    }

    private void initStartOfTodayDate() {
        // А - Аптемезация
        this.mStartOfToday = Utils.startOfTodayMillis();
    }

    public void cleanup() {
        unregisterAdapterDataObserver(mDataObserver);
    }

    @Override
    public DialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DialogViewHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.item_dialog, parent, false));
    }

    @Override
    public void onBindViewHolder(DialogViewHolder holder, int position) {
        final Dialog dialog = mDialogs.get(position);
        Dialog previous = position == 0 ? null : mDialogs.get(position - 1);

        holder.mDialogTitle.setText(dialog.getDisplayTitle(mContext));

        String lastMessage = dialog.getLastMessageBody();
        if (dialog.hasAttachments()) {
            lastMessage = TextUtils.isEmpty(lastMessage) ?
                    mContext.getString(R.string.attachments) : lastMessage + " " + mContext.getString(R.string.attachments);
        }

        if (dialog.hasForwardMessages()) {
            lastMessage = TextUtils.isEmpty(lastMessage) ?
                    mContext.getString(R.string.forward_messages) : lastMessage + " " + mContext.getString(R.string.forward_messages);
        }

        Integer lastMessageAction = dialog.getLastMessageAction();
        if (Objects.nonNull(lastMessageAction) && dialog.getLastMessageAction() != ChatAction.NO_ACTION) {
            lastMessage = mContext.getString(R.string.service_message);
        }

        if (dialog.isChat()) {
            String aurhorText = dialog.isLastMessageOut() ? mContext.getString(R.string.dialog_me) : dialog.getSenderShortName(mContext);
            Spannable spannable = SpannableStringBuilder.valueOf(aurhorText + " - " + lastMessage);
            spannable.setSpan(mForegroundColorSpan, 0, aurhorText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.mDialogMessage.setText(spannable);
        } else {
            holder.mDialogMessage.setText(lastMessage);
        }

        boolean lastMessageRead = dialog.isLastMessageRead();
        int titleTextStyle = getTextStyle(dialog.isLastMessageOut(), lastMessageRead);
        holder.mDialogTitle.setTypeface(null, titleTextStyle);

        boolean online = false;
        boolean onlineMobile = false;

        @UserPlatform
        int platform = UserPlatform.UNKNOWN;
        int app = 0;

        if (dialog.getInterlocutor() instanceof User) {
            User interlocuter = (User) dialog.getInterlocutor();
            online = interlocuter.isOnline();
            onlineMobile = interlocuter.isOnlineMobile();
            platform = interlocuter.getPlatform();
            app = interlocuter.getOnlineApp();
        }

        Integer iconRes = ViewUtils.getOnlineIcon(online, onlineMobile, platform, app);
        if (iconRes != null) {
            holder.ivOnline.setIcon(iconRes);
        } else {
            holder.ivOnline.setIcon(0);
        }

        holder.ivDialogType.setImageResource(dialog.isGroupChannel() ? R.drawable.channel : R.drawable.person_multiple);
        holder.ivDialogType.setVisibility(dialog.isChat() ? View.VISIBLE : View.GONE);
        holder.ivUnreadTicks.setVisibility(dialog.isLastMessageOut() ? View.VISIBLE : View.GONE);
        holder.ivUnreadTicks.setImageResource(lastMessageRead ? R.drawable.check_all : R.drawable.check);

        holder.ivOnline.setVisibility(online && !dialog.isChat() ? View.VISIBLE : View.GONE);

        boolean counterVisible = dialog.getUnreadCount() > 0;
        holder.tvUnreadCount.setText(AppTextUtils.getCounterWithK(dialog.getUnreadCount()));
        holder.tvUnreadCount.setVisibility(counterVisible ? View.VISIBLE : View.INVISIBLE);

        long lastMessageJavaTime = dialog.getLastMessageDate() * 1000;
        int headerStatus = getDivided(lastMessageJavaTime, previous == null ? null : previous.getLastMessageDate() * 1000);

        switch (headerStatus) {
            case DIV_DISABLE:
                holder.mHeaderRoot.setVisibility(View.GONE);
                break;
            case DIV_OLD:
                holder.mHeaderRoot.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_older);
                break;
            case DIV_TODAY:
                holder.mHeaderRoot.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_today);
                break;
            case DIV_YESTERDAY:
                holder.mHeaderRoot.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_yesterday);
                break;
            case DIV_THIS_WEEK:
                holder.mHeaderRoot.setVisibility(View.VISIBLE);
                holder.mHeaderTitle.setText(R.string.dialog_day_ten_days);
                break;
        }

        DATE.setTime(lastMessageJavaTime);
        holder.tvDate.setText(lastMessageJavaTime >= mStartOfToday ? DF_TODAY.format(DATE) : DF_OLD.format(DATE));

        ViewUtils.displayAvatar(holder.ivAvatar, mTransformation, dialog.getImageUrl(), PICASSO_TAG);

        holder.mContentRoot.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onDialogClick(dialog);
            }
        });

        holder.mContentRoot.setOnLongClickListener(v -> mClickListener != null && mClickListener.onDialogLongClick(dialog));

        holder.ivAvatar.setOnClickListener(view -> {
            if (Objects.nonNull(mClickListener)) {
                mClickListener.onAvatarClick(dialog);
            }
        });
    }

    public static final String PICASSO_TAG = "dialogs.adapter.tag";

    private int getTextStyle(boolean out, boolean read) {
        return read || out ? Typeface.NORMAL : Typeface.BOLD;
    }

    private static final int DIV_DISABLE = 0;
    private static final int DIV_TODAY = 1;
    private static final int DIV_YESTERDAY = 2;
    private static final int DIV_THIS_WEEK = 3;
    private static final int DIV_OLD = 4;

    private int getDivided(long messageDateJavaTime, Long previousMessageDateJavaTime) {
        int stCurrent = getStatus(messageDateJavaTime);
        if (previousMessageDateJavaTime == null) {
            return stCurrent;
        } else {
            int stPrevious = getStatus(previousMessageDateJavaTime);
            if (stCurrent == stPrevious) {
                return DIV_DISABLE;
            } else {
                return stCurrent;
            }
        }
    }

    private int getStatus(long time) {
        if (time >= mStartOfToday) {
            return DIV_TODAY;
        }

        if (time >= mStartOfToday - 86400000) {
            return DIV_YESTERDAY;
        }

        if (time >= mStartOfToday - 864000000) {
            return DIV_THIS_WEEK;
        }

        return DIV_OLD;
    }

    private ClickListener mClickListener;

    public DialogsAdapter setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
        return this;
    }

    public void setData(List<Dialog> data) {
        this.mDialogs = data;
        notifyDataSetChanged();
    }

    public interface ClickListener extends EventListener {
        void onDialogClick(Dialog dialog);

        boolean onDialogLongClick(Dialog dialog);

        void onAvatarClick(Dialog dialog);
    }

    @Override
    public int getItemCount() {
        return mDialogs.size();
    }

    class DialogViewHolder extends RecyclerView.ViewHolder {

        View mContentRoot;
        TextView mDialogTitle;
        TextView mDialogMessage;
        ImageView ivDialogType;
        ImageView ivAvatar;
        TextView tvUnreadCount;
        ImageView ivUnreadTicks;
        OnlineView ivOnline;
        TextView tvDate;
        View mHeaderRoot;
        TextView mHeaderTitle;

        DialogViewHolder(View view) {
            super(view);
            mContentRoot = view.findViewById(R.id.content_root);
            mDialogTitle = view.findViewById(R.id.dialog_title);
            mDialogMessage = view.findViewById(R.id.dialog_message);
            ivDialogType = view.findViewById(R.id.dialog_type);
            ivUnreadTicks = view.findViewById(R.id.unread_ticks);
            ivAvatar = view.findViewById(R.id.item_chat_avatar);
            tvUnreadCount = view.findViewById(R.id.item_chat_unread_count);
            ivOnline = view.findViewById(R.id.item_chat_online);
            tvDate = view.findViewById(R.id.item_chat_date);
            mHeaderRoot = view.findViewById(R.id.header_root);
            mHeaderTitle = view.findViewById(R.id.header_title);
        }
    }
}
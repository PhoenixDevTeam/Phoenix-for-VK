package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.drawer.AbsDrawerItem;
import biz.dealnote.messenger.model.drawer.IconDrawerItem;
import biz.dealnote.messenger.model.drawer.NoIconDrawerItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Utils;

public class MenuListAdapter extends RecyclerBindableAdapter<AbsDrawerItem, RecyclerView.ViewHolder> {

    private int unselectedTextColor;
    private int activeColor;
    private int unselectedIconColor;
    private Transformation transformation;
    private final ActionListener actionListener;

    public MenuListAdapter(@NonNull Context context, @NonNull List<AbsDrawerItem> pageItems, @NonNull ActionListener actionListener) {
        super(pageItems);
        this.unselectedTextColor = CurrentTheme.getColorFromAttrs(R.attr.textColorPrimary, context, "#000000");
        this.activeColor = CurrentTheme.getIconColorActive(context);
        this.unselectedIconColor = CurrentTheme.getIconColorStatic(context);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onDrawerItemClick(AbsDrawerItem item);
        void onDrawerItemLongClick(AbsDrawerItem item);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position, int type) {
        AbsDrawerItem item = getItem(position);
        holder.itemView.setSelected(item.isSelected());

        switch (type) {
            case AbsDrawerItem.TYPE_WITH_ICON:
                bindIconHolder((NormalHolder) holder, (IconDrawerItem) item);
                break;
            case AbsDrawerItem.TYPE_RECENT_CHAT:
                bindRecentChat((RecentChatHolder) holder, (RecentChat) item);
                break;
            case AbsDrawerItem.TYPE_WITHOUT_ICON:
                bindWithoutIcon((NoIconHolder) holder, (NoIconDrawerItem) item);
                break;
        }
    }

    private void bindWithoutIcon(NoIconHolder holder, NoIconDrawerItem item) {
        holder.txTitle.setText(item.getTitle());
        holder.txTitle.setTextColor(item.isSelected() ? activeColor : unselectedTextColor);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindRecentChat(RecentChatHolder holder, RecentChat item) {
        holder.tvChatTitle.setText(item.getTitle());
        holder.tvChatTitle.setTextColor(item.isSelected() ? activeColor : unselectedTextColor);

        if (Utils.isEmpty(item.getIconUrl())) {
            PicassoInstance.with()
                    .load(R.drawable.ic_group_chat)
                    .transform(transformation)
                    .into(holder.ivChatImage);
        } else {
            PicassoInstance.with()
                    .load(item.getIconUrl())
                    .transform(transformation)
                    .into(holder.ivChatImage);
        }

        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindIconHolder(NormalHolder holder, IconDrawerItem item) {
        holder.txtTitle.setText(item.getTitle());
        holder.txtTitle.setTextColor(item.isSelected() ? activeColor : unselectedTextColor);

        holder.tvCount.setVisibility(item.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        holder.tvCount.setText(String.valueOf(item.getCount()));

        holder.imgIcon.setImageResource(item.getIcon());
        holder.imgIcon.setColorFilter(item.isSelected() ? activeColor : unselectedIconColor);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case AbsDrawerItem.TYPE_DIVIDER:
                return new DividerHolder(view);
            case AbsDrawerItem.TYPE_RECENT_CHAT:
                return new RecentChatHolder(view);
            case AbsDrawerItem.TYPE_WITH_ICON:
                return new NormalHolder(view);
            case AbsDrawerItem.TYPE_WITHOUT_ICON:
                return new NoIconHolder(view);
        }
        throw new IllegalStateException();
    }

    @Override
    protected int getItemType(int position) {
        return getItem(position - getHeadersCount()).getType();
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case AbsDrawerItem.TYPE_DIVIDER:
                return R.layout.drawer_list_item_divider;
            case AbsDrawerItem.TYPE_RECENT_CHAT:
                return R.layout.item_navi_recents;
            case AbsDrawerItem.TYPE_WITH_ICON:
                return R.layout.drawer_list_item;
            case AbsDrawerItem.TYPE_WITHOUT_ICON:
                return R.layout.drawer_list_item_without_icon;
        }

        throw new IllegalStateException();
    }

    private class NormalHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtTitle;
        TextView tvCount;
        View contentRoot;

        NormalHolder(View view) {
            super(view);
            contentRoot = view.findViewById(R.id.content_root);
            imgIcon = view.findViewById(R.id.icon);
            txtTitle = view.findViewById(R.id.title);
            tvCount = view.findViewById(R.id.counter);
        }
    }

    private class DividerHolder extends RecyclerView.ViewHolder {

        DividerHolder(View itemView) {
            super(itemView);
        }
    }

    private class NoIconHolder extends RecyclerView.ViewHolder {
        TextView txTitle;
        View contentRoot;

        NoIconHolder(View view) {
            super(view);
            contentRoot = view.findViewById(R.id.content_root);
            txTitle = view.findViewById(R.id.title);
        }
    }

    private class RecentChatHolder extends RecyclerView.ViewHolder {

        TextView tvChatTitle;
        ImageView ivChatImage;
        View contentRoot;

        RecentChatHolder(View itemView) {
            super(itemView);
            contentRoot = itemView.findViewById(R.id.content_root);
            tvChatTitle = itemView.findViewById(R.id.title);
            ivChatImage = itemView.findViewById(R.id.item_friend_avatar);
        }
    }
}
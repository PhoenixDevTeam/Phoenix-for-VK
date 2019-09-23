package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Transformation;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.drawer.AbsMenuItem;
import biz.dealnote.messenger.model.drawer.IconMenuItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Utils;

public class MenuListAdapter extends RecyclerBindableAdapter<AbsMenuItem, RecyclerView.ViewHolder> {

    private int colorPrimary;
    private int colorSurface;
    private int colorOnPrimary;
    private int colorOnSurface;
    private Transformation transformation;
    private final ActionListener actionListener;

    public MenuListAdapter(@NonNull Context context, @NonNull List<AbsMenuItem> pageItems, @NonNull ActionListener actionListener) {
        super(pageItems);
        this.colorPrimary = CurrentTheme.getColorPrimary(context);
        this.colorSurface = CurrentTheme.getColorSurface(context);
        this.colorOnPrimary = CurrentTheme.getColorOnPrimary(context);
        this.colorOnSurface = CurrentTheme.getColorOnSurface(context);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
        this.actionListener = actionListener;
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position, int type) {
        AbsMenuItem item = getItem(position);
        holder.itemView.setSelected(item.isSelected());

        switch (type) {
            case AbsMenuItem.TYPE_ICON:
                bindIconHolder((NormalHolder) holder, (IconMenuItem) item);
                break;
            case AbsMenuItem.TYPE_RECENT_CHAT:
                bindRecentChat((RecentChatHolder) holder, (RecentChat) item);
                break;
        }
    }

    private void bindIconHolder(NormalHolder holder, IconMenuItem item) {
        holder.txtTitle.setText(item.getTitle());
        holder.txtTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.tvCount.setVisibility(item.getCount() > 0 ? View.VISIBLE : View.GONE);
        holder.tvCount.setText(String.valueOf(item.getCount()));

        holder.imgIcon.setImageResource(item.getIcon());
        holder.imgIcon.setColorFilter(item.isSelected() ? colorOnPrimary : colorOnSurface);

        holder.contentRoot.getBackground().setTint(item.isSelected() ? colorPrimary : colorSurface);
        holder.contentRoot.setOnClickListener(v -> actionListener.onDrawerItemClick(item));
        holder.contentRoot.setOnLongClickListener(view -> {
            actionListener.onDrawerItemLongClick(item);
            return true;
        });
    }

    private void bindRecentChat(RecentChatHolder holder, RecentChat item) {
        holder.tvChatTitle.setText(item.getTitle());
        holder.tvChatTitle.setTextColor(item.isSelected() ? colorOnPrimary : colorOnSurface);

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

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case AbsMenuItem.TYPE_RECENT_CHAT:
                return new RecentChatHolder(view);
            case AbsMenuItem.TYPE_ICON:
                return new NormalHolder(view);
        }
        throw new IllegalStateException();
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case AbsMenuItem.TYPE_RECENT_CHAT:
                return R.layout.item_navi_recents;
            case AbsMenuItem.TYPE_ICON:
                return R.layout.drawer_list_item;
        }

        throw new IllegalStateException();
    }

    @Override
    protected int getItemType(int position) {
        return getItem(position - getHeadersCount()).getType();
    }

    public interface ActionListener {
        void onDrawerItemClick(AbsMenuItem item);

        void onDrawerItemLongClick(AbsMenuItem item);
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
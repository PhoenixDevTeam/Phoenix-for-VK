package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.base.RecyclerBindableAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.Icon;
import biz.dealnote.messenger.model.menu.AdvancedItem;
import biz.dealnote.messenger.model.menu.Section;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.view.ColorFilterImageView;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 3/19/2018.
 * Phoenix-for-VK
 */
public class RecyclerMenuAdapter extends RecyclerBindableAdapter<AdvancedItem, RecyclerMenuAdapter.MenuItemHolder> {

    public RecyclerMenuAdapter(List<AdvancedItem> items) {
        super(items);
    }

    @Override
    protected void onBindItemViewHolder(MenuItemHolder viewHolder, int position, int type) {
        onBindMenuItemHolder(viewHolder, position);
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onClick(AdvancedItem item);

        void onLongClick(AdvancedItem item);
    }

    @Override
    protected MenuItemHolder viewHolder(View view, int type) {
        return new MenuItemHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_advanced_menu;
    }

    private void onBindMenuItemHolder(MenuItemHolder holder, int position) {
        final Context context = holder.itemView.getContext();

        AdvancedItem item = getItem(position);

        AssertUtils.requireNonNull(item);

        Section section = item.getSection();

        boolean headerVisible;
        if (isNull(section)) {
            headerVisible = false;
        } else if (position == 0) {
            headerVisible = true;
        } else {
            AdvancedItem previous = getItem(position - 1);
            AssertUtils.requireNonNull(previous);

            headerVisible = section != previous.getSection();
        }

        holder.headerRoot.setOnClickListener(v -> {/*dummy*/});

        if (headerVisible) {
            holder.headerRoot.setVisibility(View.VISIBLE);
            holder.headerText.setText(section.getTitle().getText(context));

            if (nonNull(section.getIcon())) {
                holder.headerIcon.setVisibility(View.VISIBLE);
                holder.headerIcon.setImageResource(section.getIcon());
            } else {
                holder.headerIcon.setVisibility(View.GONE);
            }
        } else {
            holder.headerRoot.setVisibility(View.GONE);
        }

        holder.itemOffsetView.setVisibility(nonNull(section) ? View.VISIBLE : View.GONE);

        bindIcon(holder.itemIcon, item.getIcon());

        holder.itemTitle.setText(item.getTitle().getText(context));
        holder.itemSubtitle.setVisibility(isNull(item.getSubtitle()) ? View.GONE : View.VISIBLE);
        holder.itemSubtitle.setText(item.getSubtitle() == null ? null : item.getSubtitle().getText(context));

        boolean last = position == getItemCount() - 1;

        boolean dividerVisible;

        if (last) {
            dividerVisible = false;
        } else {
            AdvancedItem next = getItem(position + 1);
            AssertUtils.requireNonNull(next);

            dividerVisible = next.getSection() != section;
        }

        holder.divider.setVisibility(dividerVisible ? View.VISIBLE : View.GONE);

        holder.itemRoot.setOnClickListener(v -> {
            if (nonNull(actionListener)) {
                actionListener.onClick(item);
            }
        });

        holder.itemRoot.setOnLongClickListener(v -> {
            if (actionListener != null) {
                actionListener.onLongClick(item);
            }
            return true;
        });
    }

    private void bindIcon(ColorFilterImageView imageView, Icon icon) {
        if (nonNull(icon)) {
            imageView.setVisibility(View.VISIBLE);

            if (icon.isRemote()) {
                imageView.setColorFilterEnabled(false);
                PicassoInstance.with()
                        .load(icon.getUrl())
                        .transform(CurrentTheme.createTransformationForAvatar(imageView.getContext()))
                        .into(imageView);
            } else {
                imageView.setColorFilterEnabled(true);
                PicassoInstance.with().cancelRequest(imageView);
                imageView.setImageResource(icon.getRes());
            }
        } else {
            PicassoInstance.with().cancelRequest(imageView);
            imageView.setVisibility(View.GONE);
        }
    }

    static class MenuItemHolder extends RecyclerView.ViewHolder {

        final View headerRoot;
        final ImageView headerIcon;
        final TextView headerText;

        final View itemOffsetView;
        final ColorFilterImageView itemIcon;
        final TextView itemTitle;
        final TextView itemSubtitle;
        final View itemRoot;
        final View divider;

        MenuItemHolder(View itemView) {
            super(itemView);
            this.headerRoot = itemView.findViewById(R.id.header_root);
            this.headerIcon = itemView.findViewById(R.id.header_icon);
            this.headerText = itemView.findViewById(R.id.header_text);
            this.itemRoot = itemView.findViewById(R.id.item_root);
            this.itemOffsetView = itemView.findViewById(R.id.item_offset);
            this.itemIcon = itemView.findViewById(R.id.item_icon);
            this.itemTitle = itemView.findViewById(R.id.item_title);
            this.itemSubtitle = itemView.findViewById(R.id.item_subtitle);

            this.divider = itemView.findViewById(R.id.divider);
        }
    }
}
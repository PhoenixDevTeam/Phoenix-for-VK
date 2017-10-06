package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.menu.Item;
import biz.dealnote.messenger.model.menu.Section;
import biz.dealnote.messenger.util.AssertUtils;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public class MenuAdapter extends ArrayAdapter<Item>{

    public MenuAdapter(@NonNull Context context, @NonNull List<Item> items) {
        super(context, R.layout.item_custom_menu, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;

        if(nonNull(convertView)){
            view = convertView;
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_menu, parent, false);
            view.setTag(new Holder(view));
        }

        Holder holder = (Holder) view.getTag();
        Item item = getItem(position);

        AssertUtils.requireNonNull(item);

        Section section = item.getSection();

        boolean headerVisible;
        if(isNull(section)){
            headerVisible = false;
        } else if(position == 0){
            headerVisible = true;
        } else {
            Item previous = getItem(position - 1);
            AssertUtils.requireNonNull(previous);

            headerVisible = section != previous.getSection();
        }

        holder.headerRoot.setOnClickListener(v -> {/*dummy*/});

        if(headerVisible){
            holder.headerRoot.setVisibility(View.VISIBLE);
            holder.headerText.setText(section.getTitle().getText(getContext()));

            if(nonNull(section.getIcon())){
                holder.headerIcon.setVisibility(View.VISIBLE);
                holder.headerIcon.setImageResource(section.getIcon());
            } else {
                holder.headerIcon.setVisibility(View.GONE);
            }
        } else {
            holder.headerRoot.setVisibility(View.GONE);
        }

        holder.itemOffsetView.setVisibility(nonNull(section) ? View.VISIBLE : View.GONE);

        if(nonNull(item.getIcon())){
            holder.itemIcon.setVisibility(View.VISIBLE);
            holder.itemIcon.setImageResource(item.getIcon());
        } else {
            holder.itemIcon.setVisibility(View.GONE);
        }

        holder.itemText.setText(item.getTitle().getText(getContext()));

        boolean last = position == getCount() - 1;

        boolean dividerVisible;

        if(last){
            dividerVisible = false;
        } else {
            Item next = getItem(position + 1);
            AssertUtils.requireNonNull(next);

            dividerVisible = next.getSection() != section;
        }

        holder.divider.setVisibility(dividerVisible ? View.VISIBLE : View.GONE);
        return view;
    }

    static class Holder {

        final View headerRoot;
        final ImageView headerIcon;
        final TextView headerText;

        final View itemOffsetView;
        final ImageView itemIcon;
        final TextView itemText;

        final View divider;

        Holder(View itemView) {
            this.headerRoot = itemView.findViewById(R.id.header_root);
            this.headerIcon = (ImageView) itemView.findViewById(R.id.header_icon);
            this.headerText = (TextView) itemView.findViewById(R.id.header_text);

            this.itemOffsetView = itemView.findViewById(R.id.item_offset);
            this.itemIcon = (ImageView) itemView.findViewById(R.id.item_icon);
            this.itemText = (TextView) itemView.findViewById(R.id.item_text);

            this.divider = itemView.findViewById(R.id.divider);
        }
    }
}
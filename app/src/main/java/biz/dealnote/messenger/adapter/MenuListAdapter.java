package biz.dealnote.messenger.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.ArrayList;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.drawer.AbsDrawerItem;
import biz.dealnote.messenger.model.drawer.IconDrawerItem;
import biz.dealnote.messenger.model.drawer.NoIconDrawerItem;
import biz.dealnote.messenger.model.drawer.RecentChat;
import biz.dealnote.messenger.settings.CurrentTheme;

public class MenuListAdapter extends ArrayAdapter<AbsDrawerItem> {

    private Context context;
    private ArrayList<AbsDrawerItem> pageItems;
    private ListView listView;
    private int unselectedTextColor;
    private int activeColor;
    private int unselectedIconColor;
    private Transformation transformation;

    public MenuListAdapter(Context context, ArrayList<AbsDrawerItem> pageItems, ListView listView) {
        super(context, R.layout.drawer_list_item, pageItems);
        this.context = context;
        this.pageItems = pageItems;
        this.listView = listView;
        this.unselectedTextColor = CurrentTheme.getColorFromAttrs(R.attr.textColorPrimary, context, "#000000");
        this.activeColor = CurrentTheme.getIconColorActive(context);
        this.unselectedIconColor = CurrentTheme.getIconColorStatic(context);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public int getCount() {
        return pageItems.size();
    }

    @Override
    public AbsDrawerItem getItem(int position) {
        return pageItems.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != AbsDrawerItem.TYPE_DIVIDER;
    }

    @Override
    public int getItemViewType(int position) {
        return pageItems.get(position).getType();
    }

    private class NormalHolder {
        ImageView imgIcon;
        TextView txtTitle;
        TextView tvCount;

        NormalHolder(View view){
            imgIcon = view.findViewById(R.id.icon);
            txtTitle = view.findViewById(R.id.title);
            tvCount = view.findViewById(R.id.counter);
        }
    }

    private class NoIconHolder {
        TextView txTitle;

        NoIconHolder(View view){
            txTitle = view.findViewById(R.id.title);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final AbsDrawerItem item = pageItems.get(position);
        boolean selected = listView.getCheckedItemPosition() == position + listView.getHeaderViewsCount();

        switch (getItemViewType(position)) {
            case AbsDrawerItem.TYPE_WITH_ICON:
                IconDrawerItem iconDrawerItem = (IconDrawerItem) item;
                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.drawer_list_item, parent, false);
                }

                NormalHolder normalHolder = new NormalHolder(convertView);
                normalHolder.txtTitle.setText(iconDrawerItem.getTitle());
                normalHolder.txtTitle.setTextColor(selected ? activeColor : unselectedTextColor);

                normalHolder.tvCount.setVisibility(iconDrawerItem.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
                normalHolder.tvCount.setText(String.valueOf(iconDrawerItem.getCount()));

                normalHolder.imgIcon.setImageResource(iconDrawerItem.getIcon());
                normalHolder.imgIcon.setColorFilter(selected ? activeColor : unselectedIconColor);
                return convertView;

            case AbsDrawerItem.TYPE_WITHOUT_ICON:
                NoIconDrawerItem noIconDrawerItem = (NoIconDrawerItem) item;
                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.drawer_list_item_without_icon, parent, false);
                }

                NoIconHolder noIconHolder = new NoIconHolder(convertView);
                noIconHolder.txTitle.setText(noIconDrawerItem.getTitle());
                noIconHolder.txTitle.setTextColor(selected ? activeColor : unselectedTextColor);

                return convertView;
            case AbsDrawerItem.TYPE_DIVIDER:
                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.drawer_list_item_divider, parent, false);
                }

                return convertView;
            case AbsDrawerItem.TYPE_RECENT_CHAT:
                RecentChat recentChat = (RecentChat) item;
                if (convertView == null) {
                    LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = mInflater.inflate(R.layout.item_navi_recents, parent, false);
                }

                TextView tvChatTitle = convertView.findViewById(R.id.title);
                tvChatTitle.setText(recentChat.getTitle());
                tvChatTitle.setTextColor(selected ? activeColor : unselectedTextColor);

                ImageView ivChatImage = convertView.findViewById(R.id.item_friend_avatar);

                if (TextUtils.isEmpty(recentChat.getIconUrl())) {
                    PicassoInstance.with()
                            .load(R.drawable.ic_group_chat)
                            .transform(transformation)
                            .into(ivChatImage);
                } else {
                    PicassoInstance.with()
                            .load(recentChat.getIconUrl())
                            .transform(transformation)
                            .into(ivChatImage);
                }

                return convertView;
            default:
                throw new IllegalArgumentException();
        }
    }
}
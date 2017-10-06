package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.SelectionUtils;
import biz.dealnote.messenger.fragment.UserInfoResolveUtil;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UsersPart;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.ViewUtils;

public class FriendsRecycleAdapter extends RecyclerView.Adapter<FriendsRecycleAdapter.Holder> {

    private static final int STATUS_COLOR_OFFLINE = Color.parseColor("#999999");

    private List<UsersPart> data;
    private boolean group;
    private Context context;
    private Transformation transformation;
    private Listener listener;

    public FriendsRecycleAdapter(List<UsersPart> data, Context context) {
        this.data = data;
        this.context = context;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_new_user, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final ItemInfo itemInfo = get(position);
        final User user = itemInfo.user;

        holder.headerCount.getBackground().setColorFilter(CurrentTheme.getColorPrimary(context), PorterDuff.Mode.MULTIPLY);
        boolean headerVisible = group && itemInfo.first;
        holder.header.setVisibility(headerVisible ? View.VISIBLE : View.GONE);

        if(headerVisible){
            holder.headerCount.setText(String.valueOf(itemInfo.fullSectionCount));
            holder.headerTitle.setText(itemInfo.sectionTitleRes);
        }

        holder.name.setText(user.getFullName());

        holder.status.setText(UserInfoResolveUtil.getUserActivityLine(context, user));
        holder.status.setTextColor(user.isOnline() ? CurrentTheme.getIconColorActive(context) : STATUS_COLOR_OFFLINE);

        holder.online.setVisibility(user.isOnline() ? View.VISIBLE : View.GONE);
        Integer onlineIcon = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if(onlineIcon != null){
            holder.online.setImageResource(onlineIcon);
        }

        String avaUrl = user.getMaxSquareAvatar();
        ViewUtils.displayAvatar(holder.avatar, transformation, avaUrl, Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onUserClick(user);
            }
        });

        SelectionUtils.addSelectionProfileSupport(context, holder.avatarRoot, user);
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for(UsersPart pair : data){
            if(!pair.enable){
                continue;
            }

            count = count + pair.users.size();
        }

        return count;
    }

    private ItemInfo get(int position) throws IllegalArgumentException {
        int offset = 0;
        for (UsersPart pair : data) {
            if (!pair.enable) {
                continue;
            }

            int newOffset = offset + pair.users.size();
            if (position < newOffset) {
                int internalPosition = position - offset;
                boolean first = internalPosition == 0;
                int displayCount = pair.displayCount == null ? pair.users.size() : pair.displayCount;
                return new ItemInfo(pair.users.get(internalPosition), first, displayCount, pair.titleResId);
            }

            offset = newOffset;
        }

        throw new IllegalArgumentException("Invalid adapter position");
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public void setData(List<UsersPart> data, boolean grouping) {
        this.data = data;
        this.group = grouping;
        notifyDataSetChanged();
    }

    public interface Listener {
        void onUserClick(User user);
    }

    private static class ItemInfo {

        User user;
        boolean first;
        int fullSectionCount;
        int sectionTitleRes;

        ItemInfo(User user, boolean first, int fullSectionCount, int sectionTitleRes) {
            this.user = user;
            this.first = first;
            this.fullSectionCount = fullSectionCount;
            this.sectionTitleRes = sectionTitleRes;
        }
    }

    public class Holder extends RecyclerView.ViewHolder {

        View header;
        TextView headerTitle;
        TextView headerCount;

        TextView name;
        TextView status;
        ViewGroup avatarRoot;
        ImageView avatar;
        ImageView online;

        public Holder(View itemView) {
            super(itemView);
            this.header = itemView.findViewById(R.id.header);
            this.headerTitle = itemView.findViewById(R.id.title);
            this.headerCount = itemView.findViewById(R.id.count);
            this.name = itemView.findViewById(R.id.item_friend_name);
            this.status = itemView.findViewById(R.id.item_friend_status);
            this.avatar = itemView.findViewById(R.id.item_friend_avatar);
            this.avatarRoot = itemView.findViewById(R.id.item_friend_avatar_container);
            this.online = itemView.findViewById(R.id.item_friend_online);
            this.online.setColorFilter(CurrentTheme.getIconColorActive(context), PorterDuff.Mode.MULTIPLY);
        }
    }
}
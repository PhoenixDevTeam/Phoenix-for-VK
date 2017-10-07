package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Transformation;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.link.internal.LinkActionAdapter;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.FormatUtil;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.OnlineView;

/**
 * Created by admin on 14.06.2017.
 * phoenix
 */
public class CommunityBannedAdapter extends RecyclerView.Adapter<CommunityBannedAdapter.Holder> {

    private List<Banned> data;
    private final Transformation transformation;

    public CommunityBannedAdapter(Context context, List<Banned> data) {
        this.data = data;
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_community_ban_info, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final Context context = holder.itemView.getContext();

        Banned banned = data.get(position);

        User user = banned.getUser();
        User admin = banned.getAdmin();

        Banned.Info info = banned.getInfo();

        holder.name.setText(user.getFullName());

        ViewUtils.displayAvatar(holder.avatar, transformation, user.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        Integer onlineViewRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if(Objects.nonNull(onlineViewRes)){
            holder.onlineView.setIcon(onlineViewRes);
            holder.onlineView.setVisibility(View.VISIBLE);
        } else {
            holder.onlineView.setVisibility(View.GONE);
        }

        String comment = info.getComment();

        if(Utils.nonEmpty(comment)){
            holder.comment.setVisibility(View.VISIBLE);

            String commentText = context.getString(R.string.ban_comment_text, comment);
            holder.comment.setText(commentText);
        } else {
            holder.comment.setVisibility(View.GONE);
        }

        Spannable spannable = FormatUtil.formatCommunityBanInfo(context, admin.getId(),
                admin.getFullName(), info.getEndDate(), ownerLinkActionListener);

        holder.dateAndAdminInfo.setMovementMethod(LinkMovementMethod.getInstance());
        holder.dateAndAdminInfo.setText(spannable, TextView.BufferType.SPANNABLE);

        holder.itemView.setOnClickListener(v -> {
            if(Objects.nonNull(actionListener)){
                actionListener.onBannedClick(banned);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if(Objects.nonNull(actionListener)){
                actionListener.onBannedLongClick(banned);
            }
            return true;
        });
    }

    private OwnerLinkSpanFactory.ActionListener ownerLinkActionListener = new LinkActionAdapter();

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Banned> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onBannedClick(Banned banned);
        void onBannedLongClick(Banned banned);
    }

    static class Holder extends RecyclerView.ViewHolder {

        ImageView avatar;
        OnlineView onlineView;

        TextView name;
        TextView dateAndAdminInfo;
        TextView comment;

        Holder(View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.avatar);
            this.onlineView = itemView.findViewById(R.id.online);
            this.name = itemView.findViewById(R.id.name);
            this.dateAndAdminInfo = itemView.findViewById(R.id.date_and_admin_info);
            this.comment = itemView.findViewById(R.id.comment_text);
        }
    }
}
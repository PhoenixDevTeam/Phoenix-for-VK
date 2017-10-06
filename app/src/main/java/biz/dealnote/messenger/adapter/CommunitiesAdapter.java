package biz.dealnote.messenger.adapter;

import android.content.Context;
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
import biz.dealnote.messenger.adapter.multidata.MultyDataAdapter;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.DataWrapper;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.ViewUtils;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesAdapter extends MultyDataAdapter<Community, CommunitiesAdapter.Holder> {

    private final Transformation transformation;

    public CommunitiesAdapter(Context context, List<DataWrapper<Community>> dataWrappers, int[] titles) {
        super(dataWrappers, titles);
        this.transformation = CurrentTheme.createTransformationForAvatar(context);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int position) {
        return new Holder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_community, viewGroup, false));
    }

    private static final ItemInfo<Community> INFO = new ItemInfo<>();

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        get(position, INFO);

        Community community = INFO.item;
        holder.headerRoot.setVisibility(INFO.internalPosition == 0 ? View.VISIBLE : View.GONE);
        holder.headerTitle.setText(INFO.sectionTitleRes);

        ViewUtils.displayAvatar(holder.ivAvatar, transformation, community.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.tvName.setText(community.getFullName());
        holder.subtitle.setText(R.string.community);

        holder.contentRoot.setOnClickListener(view -> {
            if(Objects.nonNull(actionListener)){
                actionListener.onCommunityClick(community);
            }
        });
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onCommunityClick(Community community);
    }

    static class Holder extends RecyclerView.ViewHolder {

        View headerRoot;
        TextView headerTitle;

        View contentRoot;
        TextView tvName;
        ImageView ivAvatar;
        TextView subtitle;

        Holder(View root) {
            super(root);
            this.headerRoot = root.findViewById(R.id.header_root);
            this.headerTitle = root.findViewById(R.id.header_title);
            this.contentRoot = root.findViewById(R.id.content_root);
            this.tvName = root.findViewById(R.id.name);
            this.ivAvatar = root.findViewById(R.id.avatar);
            this.subtitle = root.findViewById(R.id.subtitle);
        }
    }
}
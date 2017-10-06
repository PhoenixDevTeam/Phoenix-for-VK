package biz.dealnote.messenger.adapter.fave;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.ViewUtils;

public class FaveUsersAdapter extends RecyclerView.Adapter<FaveUsersAdapter.Holder> {

    private List<User> data;
    private Context context;

    public FaveUsersAdapter(List<User> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_user, parent, false));
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final User user = data.get(position);
        holder.name.setText(user.getFullName());
        ViewUtils.displayAvatar(holder.avatar, null, user.getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if(clickListener != null){
                clickListener.onUserClick(holder.getAdapterPosition(), user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<User> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        ImageView avatar;
        TextView name;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            final int position = recyclerView.getChildAdapterPosition(v);
            final User user = data.get(position);
            menu.setHeaderTitle(user.getFullName());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if(clickListener != null){
                    clickListener.onDelete(position, user);
                }
                return true;
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    private RecyclerView recyclerView;

    public interface ClickListener {
        void onUserClick(int index, User user);
        void onDelete(int index, User user);
    }

    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
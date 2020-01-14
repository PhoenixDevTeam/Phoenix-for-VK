package biz.dealnote.messenger.adapter.fave;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.FavePage;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.ViewUtils;

public class FavePagesAdapter extends RecyclerView.Adapter<FavePagesAdapter.Holder> {

    private List<FavePage> data;
    private Context context;

    public FavePagesAdapter(List<FavePage> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        final FavePage favePage = data.get(position);
        switch (favePage.getType()) {
            case "user":
                holder.name.setText(favePage.getUser().getFullName());
                ViewUtils.displayAvatar(holder.avatar, null, favePage.getUser().getMaxSquareAvatar(), Constants.PICASSO_TAG);
                break;
            case "group":
                holder.name.setText(favePage.getGroup().getFullName());
                ViewUtils.displayAvatar(holder.avatar, null, favePage.getGroup().getMaxSquareAvatar(), Constants.PICASSO_TAG);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if(clickListener != null){
//                clickListener.onUserClick(holder.getAdapterPosition(), user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<FavePage> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
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
            final FavePage favePage = data.get(position);
//            menu.setHeaderTitle(favePage.getUser().getFullName());
//
//            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
//                if(clickListener != null){
//                    clickListener.onDelete(position, user);
//                }
//                return true;
//            });
        }
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
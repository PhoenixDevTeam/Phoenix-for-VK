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
import biz.dealnote.messenger.model.Owner;
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
        holder.description.setText(favePage.getDescription());
        holder.name.setText(favePage.getOwner().getFullName());
        ViewUtils.displayAvatar(holder.avatar, null, favePage.getOwner().getMaxSquareAvatar(), Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPageClick(holder.getAdapterPosition(), favePage.getOwner());
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
        TextView description;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);

            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            final int position = recyclerView.getChildAdapterPosition(v);
            final FavePage favePage = data.get(position);
            menu.setHeaderTitle(favePage.getOwner().getFullName());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if (clickListener != null) {
                    clickListener.onDelete(position, favePage.getOwner());
                }
                return true;
            });
        }
    }

    private RecyclerView recyclerView;

    public interface ClickListener {
        void onPageClick(int index, Owner owner);

        void onDelete(int index, Owner owner);
    }

    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
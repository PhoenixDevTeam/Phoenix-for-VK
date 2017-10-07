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
import biz.dealnote.messenger.model.FaveLink;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;

public class FaveLinksAdapter extends RecyclerView.Adapter<FaveLinksAdapter.Holder> {

    private List<FaveLink> data;
    private Context context;

    public FaveLinksAdapter(List<FaveLink> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_fave_link, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final FaveLink link = data.get(position);
        holder.title.setText(link.getTitle());
        holder.description.setText(link.getDescription());

        String photo = Utils.firstNonEmptyString(link.getPhoto100(), link.getPhoto50());

        ViewUtils.displayAvatar(holder.image, null, photo, Constants.PICASSO_TAG);

        holder.itemView.setOnClickListener(v -> {
            if(clickListener != null){
                clickListener.onLinkClick(holder.getAdapterPosition(), link);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<FaveLink> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        ImageView image;
        TextView title;
        TextView description;

        public Holder(View itemView) {
            super(itemView);
            itemView.setOnCreateContextMenuListener(this);

            image = itemView.findViewById(R.id.link_image);
            title = itemView.findViewById(R.id.link_title);
            description = itemView.findViewById(R.id.link_description);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            final int position = recyclerView.getChildAdapterPosition(v);
            final FaveLink faveLink = data.get(position);
            menu.setHeaderTitle(faveLink.getTitle());

            menu.add(0, v.getId(), 0, R.string.delete).setOnMenuItemClickListener(item -> {
                if(clickListener != null){
                    clickListener.onLinkDelete(position, faveLink);
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
        void onLinkClick(int index, FaveLink link);
        void onLinkDelete(int index, FaveLink link);
    }

    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
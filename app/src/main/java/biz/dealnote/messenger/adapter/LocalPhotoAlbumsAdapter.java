package biz.dealnote.messenger.adapter;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.LocalPhoto;

public class LocalPhotoAlbumsAdapter extends RecyclerView.Adapter<LocalPhotoAlbumsAdapter.Holder> {

    private List<LocalImageAlbum> data;
    public static final String PICASSO_TAG = "LocalPhotoAlbumsAdapter.TAG";

    public LocalPhotoAlbumsAdapter(List<LocalImageAlbum> data) {
        this.data = data;
    }

    public void setData(List<LocalImageAlbum> data){
        this.data = data;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.local_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        final LocalImageAlbum album = data.get(position);

        Uri uri = LocalPhoto.buildUriForPicasso(album.getCoverImageId());
        PicassoInstance.with()
                .load(uri)
                .tag(PICASSO_TAG)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.background_gray)
                .into(holder.image);

        holder.title.setText(album.getName());
        holder.subtitle.setText(holder.itemView.getContext().getString(R.string.photos_count, album.getPhotoCount()));

        holder.itemView.setOnClickListener(v -> {
            if(clickListener != null){
                clickListener.onClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(LocalImageAlbum album);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView title;
        TextView subtitle;

        public Holder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_local_album_cover);
            title = itemView.findViewById(R.id.item_local_album_name);
            subtitle = itemView.findViewById(R.id.counter);
        }
    }
}
package biz.dealnote.messenger.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoSize;

public class VkPhotoAlbumsAdapter extends RecyclerView.Adapter<VkPhotoAlbumsAdapter.Holder> {

    private Context context;
    private List<PhotoAlbum> data;

    public VkPhotoAlbumsAdapter(Context context, List<PhotoAlbum> data) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.local_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final PhotoAlbum photoAlbum = data.get(position);

        String thumb = photoAlbum.getSizes().getUrlForSize(PhotoSize.X, false);

        PicassoInstance.with()
                .load(thumb)
                .tag(Constants.PICASSO_TAG)
                .placeholder(R.drawable.background_gray)
                .into(holder.imageView);

        holder.title.setText(photoAlbum.getTitle());

        holder.imageView.setOnClickListener(v -> {
            if(clickListener != null){
                clickListener.onVkPhotoAlbumClick(photoAlbum);
            }
        });

        holder.counterText.setText(context.getString(R.string.photos_count, photoAlbum.getSize()));
        holder.imageView.setOnLongClickListener(v -> clickListener != null && clickListener.onVkPhotoAlbumLongClick(photoAlbum));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title;
        TextView counterText;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_local_album_cover);
            title = itemView.findViewById(R.id.item_local_album_name);
            counterText = itemView.findViewById(R.id.counter);
        }
    }

    private ClickListener clickListener;

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onVkPhotoAlbumClick(@NonNull PhotoAlbum album);
        boolean onVkPhotoAlbumLongClick(@NonNull PhotoAlbum album);
    }

    public void setData(List<PhotoAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }
}
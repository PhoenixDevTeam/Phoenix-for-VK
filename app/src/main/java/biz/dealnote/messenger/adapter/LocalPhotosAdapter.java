package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.LocalPhoto;

public class LocalPhotosAdapter extends RecyclerView.Adapter<LocalPhotosAdapter.ViewHolder> {

    public static final String TAG = LocalPhotosAdapter.class.getSimpleName();

    private Context context;
    private List<LocalPhoto> data;
    private Set<ViewHolder> holders;

    public LocalPhotosAdapter(Context context, List<LocalPhoto> data) {
        this.context = context;
        this.data = data;
        this.holders = new HashSet<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.photo_item, parent, false));
        holders.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LocalPhoto photo = data.get(position);
        holder.attachPhoto(photo);

        PicassoInstance.with()
                .load(LocalPhoto.buildUriForPicasso(photo.getImageId()))
                .tag(TAG)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.background_gray)
                .into(holder.photoImageView);

        resolveSelectionVisibility(photo, holder);
        resolveIndexText(photo, holder);

        View.OnClickListener listener = v -> {
            if (clickListener != null) {
                clickListener.onPhotoClick(holder, photo);
            }
        };

        holder.photoImageView.setOnClickListener(listener);
        holder.selectedRoot.setOnClickListener(listener);
    }

    public void updateHoldersSelectionAndIndexes() {
        for (ViewHolder holder : holders) {
            LocalPhoto photo = (LocalPhoto) holder.itemView.getTag();

            if(photo == null){
                // TODO: 13.12.2017 Photo can bee null !!!! WTF?
                continue;
            }

            resolveSelectionVisibility(photo, holder);
            resolveIndexText(photo, holder);
        }
    }

    private void resolveSelectionVisibility(LocalPhoto photo, ViewHolder holder) {
        holder.selectedRoot.setVisibility(photo.isSelected() ? View.VISIBLE : View.GONE);
    }

    private void resolveIndexText(LocalPhoto photo, ViewHolder holder) {
        holder.tvIndex.setText(photo.getIndex() == 0 ? "" : String.valueOf(photo.getIndex()));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private ClickListener clickListener;

    public interface ClickListener {
        void onPhotoClick(ViewHolder holder, LocalPhoto photo);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView photoImageView;
        View selectedRoot;
        TextView tvIndex;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.photoImageView = itemView.findViewById(R.id.imageView);
            this.selectedRoot = itemView.findViewById(R.id.selected);
            this.tvIndex = itemView.findViewById(R.id.item_photo_index);
        }

        private void attachPhoto(LocalPhoto photo) {
            itemView.setTag(photo);
        }
    }
}
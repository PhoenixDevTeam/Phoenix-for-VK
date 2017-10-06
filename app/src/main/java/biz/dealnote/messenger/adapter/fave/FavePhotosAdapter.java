package biz.dealnote.messenger.adapter.fave;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;

public class FavePhotosAdapter extends RecyclerView.Adapter<FavePhotosAdapter.ViewHolder> {

    private List<Photo> data;
    private int colorPrimary;

    public FavePhotosAdapter(Context context, List<Photo> data) {
        this.data = data;
        this.colorPrimary = CurrentTheme.getColorPrimary(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fave_photo, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final FavePhotosAdapter.ViewHolder viewHolder, int position) {
        final Photo photo = data.get(position);

        viewHolder.tvLike.setText(AppTextUtils.getCounterWithK(photo.getLikesCount()));

        viewHolder.tvLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);
        viewHolder.ivLike.setVisibility(photo.getLikesCount() > 0 ? View.VISIBLE : View.GONE);

        viewHolder.tvComment.setText(AppTextUtils.getCounterWithK(photo.getCommentsCount()));

        viewHolder.tvComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);
        viewHolder.ivComment.setVisibility(photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        viewHolder.vgBottom.setBackgroundColor(Utils.adjustAlpha(colorPrimary, 0.75F));
        viewHolder.vgBottom.setVisibility(photo.getLikesCount() + photo.getCommentsCount() > 0 ? View.VISIBLE : View.GONE);

        PicassoInstance.with()
                .load(photo.getUrlForSize(PhotoSize.X, false))
                .tag(Constants.PICASSO_TAG)
                .placeholder(R.drawable.background_gray)
                .into(viewHolder.photoImageView);

        viewHolder.cardView.setOnClickListener(v -> {
            if(photoSelectionListener != null){
                photoSelectionListener.onPhotoClicked(viewHolder.getAdapterPosition(), photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Photo> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View cardView;
        ImageView photoImageView;
        ViewGroup vgBottom;
        TextView tvLike;
        TextView tvComment;
        ImageView ivLike;
        ImageView ivComment;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            photoImageView = itemView.findViewById(R.id.imageView);
            vgBottom = itemView.findViewById(R.id.vk_photo_item_bottom);
            ivLike = itemView.findViewById(R.id.vk_photo_item_like);
            tvLike = itemView.findViewById(R.id.vk_photo_item_like_counter);
            ivComment = itemView.findViewById(R.id.vk_photo_item_comment);
            tvComment = itemView.findViewById(R.id.vk_photo_item_comment_counter);
        }
    }

    private PhotoSelectionListener photoSelectionListener;

    public void setPhotoSelectionListener(PhotoSelectionListener photoSelectionListener) {
        this.photoSelectionListener = photoSelectionListener;
    }

    public interface PhotoSelectionListener {
        void onPhotoClicked(int position, Photo photo);
    }
}
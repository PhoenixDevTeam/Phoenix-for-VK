package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.StickerSet;

public class StickersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_ANIMATED = 1;

    private Context context;
    private StickerSet stickers;
    private StickersGridView.OnStickerClickedListener stickerClickedListener;

    public StickersAdapter(Context context, StickerSet stickers) {
        this.context = context;
        this.stickers = stickers;
    }

//    @Override
//    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
//        holder.setIsRecyclable(false);
//        super.onViewAttachedToWindow(holder);
//    }
//
//    @Override
//    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
//        holder.setIsRecyclable(true);
//        super.onViewDetachedFromWindow(holder);
//    }

    public void setStickerClickedListener(StickersGridView.OnStickerClickedListener listener) {
        this.stickerClickedListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_IMAGE:
                return new StickerHolder(LayoutInflater.from(context).inflate(R.layout.sticker_grid_item, parent, false));
            case TYPE_ANIMATED:
                return new StickerAnimatedHolder(LayoutInflater.from(context).inflate(R.layout.sticker_grid_item_animated, parent, false));
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return stickers.getStickers().get(position).isAnimated() ? TYPE_ANIMATED : TYPE_IMAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Sticker item = stickers.getStickers().get(position);
        switch (getItemViewType(position)) {
            default:
            case TYPE_ANIMATED:
                StickerAnimatedHolder animatedHolder = (StickerAnimatedHolder) holder;
                animatedHolder.animation.setAnimationFromUrl(item.getAnimationUrl());
                animatedHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
                animatedHolder.root.setOnLongClickListener(v -> {
                    animatedHolder.animation.playAnimation();
                    return true;
                });

                break;
            case TYPE_IMAGE:
                StickerHolder normalHolder = (StickerHolder) holder;
                normalHolder.image.setVisibility(View.VISIBLE);
                String url = item.getImage(256, true).getUrl();

                PicassoInstance.with()
                        .load(url)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(normalHolder.image, new LoadOnErrorCallback(normalHolder.image, url));
                normalHolder.root.setOnClickListener(v -> stickerClickedListener.onStickerClick(item));
                break;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof StickerAnimatedHolder) {
            StickerAnimatedHolder animatedHolder = (StickerAnimatedHolder) holder;
            animatedHolder.animation.cancelAnimation();
            if (animatedHolder.animation.getDrawable() instanceof LottieDrawable) {
                ((LottieDrawable) animatedHolder.animation.getDrawable()).clearComposition();
            }
        }
    }

    @Override
    public int getItemCount() {
        return stickers.getStickers().size();
    }

    private static class LoadOnErrorCallback implements Callback {

        final WeakReference<ImageView> ref;
        final String link;

        private LoadOnErrorCallback(ImageView view, String link) {
            this.ref = new WeakReference<>(view);
            this.link = link;
        }

        @Override
        public void onSuccess() {
            // do nothink
        }

        @Override
        public void onError(Exception e) {
            ImageView view = ref.get();
            try {
                if (view != null) {
                    PicassoInstance.with()
                            .load(link)
                            .into(view);
                }
            } catch (Exception ignored) {

            }
        }
    }

    static final class StickerHolder extends RecyclerView.ViewHolder {
        final View root;
        final ImageView image;

        StickerHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView.getRootView();
            this.image = itemView.findViewById(R.id.sticker);
        }
    }

    static final class StickerAnimatedHolder extends RecyclerView.ViewHolder {
        final View root;
        final LottieAnimationView animation;

        StickerAnimatedHolder(@NonNull View itemView) {
            super(itemView);
            this.root = itemView.getRootView();
            this.animation = itemView.findViewById(R.id.sticker_animated);
        }
    }
}
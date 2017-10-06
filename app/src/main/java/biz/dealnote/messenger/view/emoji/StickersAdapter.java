package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.api.model.VKApiStickerSet;
import biz.dealnote.messenger.model.StickerSet;
import biz.dealnote.messenger.util.AssertUtils;

public class StickersAdapter extends ArrayAdapter<Integer> {

    private StickersGridView.OnStickerClickedListener stickerClickedListener;

    public StickersAdapter(Context context, StickerSet data) {
        super(context, R.layout.sticker_grid_item, data.getIds());
    }

    public void setStickerClickedListener(StickersGridView.OnStickerClickedListener listener) {
        this.stickerClickedListener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        Integer strickerId = getItem(position);
        AssertUtils.requireNonNull(strickerId);

        View viewToUse;

        if (convertView == null) {
            holder = new ViewHolder();
            viewToUse = View.inflate(getContext(), R.layout.sticker_grid_item, null);
            holder.icon = viewToUse.findViewById(R.id.sticker_grid_item_image);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        String url = VKApiStickerSet.buildImgUrl256(strickerId);

        PicassoInstance.with()
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.icon, new LoadOnErrorCallback(holder.icon, url));

        holder.icon.setOnClickListener(v -> {
            Integer id = getItem(position);
            AssertUtils.requireNonNull(id);
            stickerClickedListener.onStickerClick(id);
        });

        return viewToUse;
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
                if(view != null){
                    PicassoInstance.with()
                            .load(link)
                            .into(view);
                }
            } catch (Exception ignored){

            }
        }
    }

    class ViewHolder {
        ImageView icon;
    }
}
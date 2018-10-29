package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.StickerSet;
import biz.dealnote.messenger.util.AssertUtils;

public class StickersAdapter extends ArrayAdapter<Sticker> {

    private StickersGridView.OnStickerClickedListener stickerClickedListener;

    public StickersAdapter(Context context, StickerSet data) {
        super(context, R.layout.sticker_grid_item, data.getStickers());
    }

    public void setStickerClickedListener(StickersGridView.OnStickerClickedListener listener) {
        this.stickerClickedListener = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        Sticker sticker = getItem(position);

        AssertUtils.requireNonNull(sticker);

        View view;

        if (convertView == null) {
            view = View.inflate(getContext(), R.layout.sticker_grid_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        String url = sticker.getImage(256, true).getUrl();

        PicassoInstance.with()
                .load(url)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(holder.icon, new LoadOnErrorCallback(holder.icon, url));

        holder.icon.setOnClickListener(v -> stickerClickedListener.onStickerClick(sticker));
        return view;
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

    static final class ViewHolder {
        final ImageView icon;
        ViewHolder(View itemView) {
            this.icon = itemView.findViewById(R.id.sticker_grid_item_image);
        }
    }
}
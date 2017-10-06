package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.settings.CurrentTheme;

public class SectionsAdapter extends RecyclerView.Adapter<SectionsAdapter.Holder> {

    private Context mContext;
    private List<AbsSection> data;

    public SectionsAdapter(List<AbsSection> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(mContext).inflate(R.layout.emoji_section_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        AbsSection section = data.get(position);
        switch (section.type){
            case AbsSection.TYPE_EMOJI:
                EmojiSection emojiSection = (EmojiSection) section;

                PicassoInstance.with()
                        .cancelRequest(holder.icon);

                holder.icon.setImageDrawable(emojiSection.drawable);
                holder.icon.setColorFilter(CurrentTheme.getIconColorStatic(mContext), PorterDuff.Mode.MULTIPLY);
                break;

            case AbsSection.TYPE_STICKER:
                StickerSection stickerSection = (StickerSection) section;
                PicassoInstance.with()
                        .load(stickerSection.stickerSet.getPhoto70())
                        .placeholder(R.drawable.sticker_pack_with_alpha)
                        .into(holder.icon);
                holder.icon.setColorFilter(null);
                break;
            case AbsSection.TYPE_PHOTO_ALBUM:
                PicassoInstance.with()
                        .cancelRequest(holder.icon);

                holder.icon.setImageResource(R.drawable.image);
                holder.icon.setColorFilter(CurrentTheme.getIconColorStatic(mContext), PorterDuff.Mode.MULTIPLY);
                break;
        }

        if(section.active){
            holder.root.setBackgroundResource(R.drawable.circle_back_white);
            holder.root.getBackground().setColorFilter(CurrentTheme.getMessageBackgroundSquare(mContext), PorterDuff.Mode.MULTIPLY);
        } else {
            holder.root.setBackground(null);
        }

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        View root;
        ImageView icon;

        public Holder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onClick(int position);
    }
}
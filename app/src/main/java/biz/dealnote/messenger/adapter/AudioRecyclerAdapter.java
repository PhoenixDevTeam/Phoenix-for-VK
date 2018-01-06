package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.AppTextUtils;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.AudioHolder>{

    private Context mContext;
    private List<Audio> mData;
    private Drawable unselectedDrawable;

    public AudioRecyclerAdapter(Context context, List<Audio> data) {
        this.mContext = context;
        this.mData = data;
        this.unselectedDrawable = ContextCompat.getDrawable(context, R.drawable.circle_back_white);
        this.unselectedDrawable.setColorFilter(CurrentTheme.getIconColorActive(context), PorterDuff.Mode.MULTIPLY);
    }

    @Override
    public AudioHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioHolder(LayoutInflater.from(mContext).inflate(R.layout.item_audio, parent, false));
    }

    @Override
    public void onBindViewHolder(final AudioHolder holder, int position) {
        final Audio item = mData.get(position);

        holder.artist.setText(item.getArtist());
        holder.title.setText(item.getTitle());
        holder.time.setText(AppTextUtils.getDurationString(item.getDuration()));
        holder.play.setBackground(unselectedDrawable);
        holder.play.setImageResource(R.drawable.play);

        holder.play.setOnClickListener(v -> {
            if(mClickListener != null){
                mClickListener.onClick(holder.getAdapterPosition(), item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Audio> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    class AudioHolder extends RecyclerView.ViewHolder {

        TextView artist;
        TextView title;
        ImageView play;
        TextView time;

        AudioHolder(View itemView) {
            super(itemView);
            artist = itemView.findViewById(R.id.dialog_title);
            title = itemView.findViewById(R.id.dialog_message);
            play = itemView.findViewById(R.id.item_audio_play);
            time = itemView.findViewById(R.id.item_audio_time);
        }
    }

    private ClickListener mClickListener;

    public void setClickListener(ClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, Audio audio);
    }
}

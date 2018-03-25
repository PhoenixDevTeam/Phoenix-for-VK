package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.view.emoji.section.Emojicon;

class EmojiAdapter extends ArrayAdapter<Emojicon> {

	private EmojiconGridView.OnEmojiconClickedListener emojiClickListener;

    public EmojiAdapter(Context context, Emojicon[] data) {
        super(context, R.layout.emojicon_item, data);
    }
    
    public void setEmojiClickListener(@NonNull EmojiconGridView.OnEmojiconClickedListener listener){
    	this.emojiClickListener = listener;
    }
    
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.emojicon_item, parent, false);

            ViewHolder holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.emojicon_icon);
            v.setTag(holder);
        }

        Emojicon emoji = getItem(position);
        ViewHolder holder = (ViewHolder) v.getTag();

        holder.icon.setText(emoji.getEmoji(), TextView.BufferType.SPANNABLE);
        holder.icon.setOnClickListener(v1 -> emojiClickListener.onEmojiconClicked(getItem(position)));
        return v;
    }

    class ViewHolder {
        TextView icon;
    }
}
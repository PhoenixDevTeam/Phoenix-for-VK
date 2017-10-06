/*
 * Copyright 2014 Ankush Sachdeva
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            holder.icon = (TextView) v.findViewById(R.id.emojicon_icon);
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
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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import java.util.Arrays;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.view.emoji.section.Emojicon;
import biz.dealnote.messenger.view.emoji.section.People;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 * @author Ankush Sachdeva (sankush@yahoo.co.in)
 */
public class EmojiconGridView {
    public View rootView;
    EmojiconsPopup mEmojiconPopup;
    Emojicon[] mData;

    public EmojiconGridView(Context context, Emojicon[] emojicons, EmojiconsPopup emojiconPopup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mEmojiconPopup = emojiconPopup;
        rootView = inflater.inflate(R.layout.emojicon_grid, null);
        GridView gridView = (GridView) rootView.findViewById(R.id.Emoji_GridView);
        if (emojicons == null) {
            mData = People.DATA;
        } else {
            mData = Arrays.asList(emojicons).toArray(new Emojicon[emojicons.length]);
        }
        EmojiAdapter mAdapter = new EmojiAdapter(rootView.getContext(), mData);
        mAdapter.setEmojiClickListener(emojicon -> {
            if (mEmojiconPopup.getOnEmojiconClickedListener() != null) {
                mEmojiconPopup.getOnEmojiconClickedListener().onEmojiconClicked(emojicon);
            }
        });
        gridView.setAdapter(mAdapter);
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }

}

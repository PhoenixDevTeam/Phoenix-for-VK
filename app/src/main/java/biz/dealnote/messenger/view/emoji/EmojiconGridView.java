package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.view.emoji.section.Emojicon;
import biz.dealnote.messenger.view.emoji.section.People;

public class EmojiconGridView {

    public View rootView;
    EmojiconsPopup mEmojiconPopup;
    Emojicon[] mData;

    public EmojiconGridView(Context context, Emojicon[] emojicons, EmojiconsPopup emojiconPopup) {
        LayoutInflater inflater = LayoutInflater.from(context);

        mEmojiconPopup = emojiconPopup;
        rootView = inflater.inflate(R.layout.emojicon_grid, null);
        GridView gridView = rootView.findViewById(R.id.Emoji_GridView);

        if (emojicons == null) {
            mData = People.DATA;
        } else {
            mData = emojicons.clone();
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
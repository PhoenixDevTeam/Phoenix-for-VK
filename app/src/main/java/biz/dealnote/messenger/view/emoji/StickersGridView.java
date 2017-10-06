package biz.dealnote.messenger.view.emoji;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.StickerSet;

public class StickersGridView {

    public View rootView;
    private EmojiconsPopup mEmojiconPopup;

    public StickersGridView(Context context, StickerSet set, EmojiconsPopup emojiconPopup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        mEmojiconPopup = emojiconPopup;
        rootView = inflater.inflate(R.layout.stickers_grid, null);
        GridView gridView = rootView.findViewById(R.id.grid_stickers);

        StickersAdapter mAdapter = new StickersAdapter(rootView.getContext(), set);
        mAdapter.setStickerClickedListener(stickerId -> {
            if (mEmojiconPopup.getOnStickerClickedListener() != null) {
                mEmojiconPopup.getOnStickerClickedListener().onStickerClick(stickerId);
            }
        });

        gridView.setAdapter(mAdapter);
    }

    public interface OnStickerClickedListener {
        void onStickerClick(int stickerId);
    }
}

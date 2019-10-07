package biz.dealnote.messenger.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.StickerSet;

public class StickersGridView {

    public View rootView;
    private EmojiconsPopup mEmojiconPopup;

    public StickersGridView(Context context, StickerSet set, EmojiconsPopup emojiconPopup) {
        rootView = LayoutInflater.from(context).inflate(R.layout.stickers_grid, null);
        mEmojiconPopup = emojiconPopup;
        RecyclerView recyclerView = rootView.findViewById(R.id.grid_stickers);

        StickersAdapter mAdapter = new StickersAdapter(rootView.getContext(), set);
        mAdapter.setStickerClickedListener(stickerId -> {
            if (mEmojiconPopup.getOnStickerClickedListener() != null) {
                mEmojiconPopup.getOnStickerClickedListener().onStickerClick(stickerId);
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    public interface OnStickerClickedListener {
        void onStickerClick(Sticker stickerId);
    }
}

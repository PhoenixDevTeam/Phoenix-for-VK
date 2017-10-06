package biz.dealnote.messenger.view.emoji;

import biz.dealnote.messenger.model.StickerSet;

public class StickerSection extends AbsSection {

    public StickerSet stickerSet;

    public StickerSection(StickerSet set) {
        super(TYPE_STICKER);
        this.stickerSet = set;
    }
}

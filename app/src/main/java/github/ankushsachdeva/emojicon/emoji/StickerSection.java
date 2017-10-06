package github.ankushsachdeva.emojicon.emoji;

import biz.dealnote.messenger.model.StickerSet;
import github.ankushsachdeva.emojicon.section.AbsSection;

public class StickerSection extends AbsSection {

    public StickerSet stickerSet;

    public StickerSection(StickerSet set) {
        super(TYPE_STICKER);
        this.stickerSet = set;
    }
}

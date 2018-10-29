package biz.dealnote.messenger.model;

import java.util.List;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public class StickerSet {

    private final String photo70;

    private final List<Sticker> stickers;

    public StickerSet(String photo70, List<Sticker> stickers) {
        this.photo70 = photo70;
        this.stickers = stickers;
    }

    public List<Sticker> getStickers() {
        return stickers;
    }

    public String getPhoto70() {
        return photo70;
    }
}
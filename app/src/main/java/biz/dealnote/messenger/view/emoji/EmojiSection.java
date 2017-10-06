package biz.dealnote.messenger.view.emoji;

import android.graphics.drawable.Drawable;

public class EmojiSection extends AbsSection {

    public static final int TYPE_PEOPLE = 0;
    public static final int TYPE_NATURE = 1;
    public static final int TYPE_FOOD = 2;
    public static final int TYPE_SPORT = 3;
    public static final int TYPE_CARS = 4;
    public static final int TYPE_ELECTRONICS = 5;
    public static final int TYPE_SYMBOLS = 6;

    public int emojiType;
    public Drawable drawable;

    public EmojiSection(int emojiType, Drawable drawable) {
        super(TYPE_EMOJI);
        this.emojiType = emojiType;
        this.drawable = drawable;
    }
}

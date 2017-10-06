package biz.dealnote.messenger.model.menu;

import android.support.annotation.DrawableRes;

import biz.dealnote.messenger.model.Text;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public class Item {

    @DrawableRes
    private Integer icon;

    private final int key;

    private final Text title;

    private Section section;

    public Item(int key, Text title) {
        this.key = key;
        this.title = title;
    }

    public int getKey() {
        return key;
    }

    public Item setSection(Section section) {
        this.section = section;
        return this;
    }

    public Item setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }

    public Section getSection() {
        return section;
    }

    public Integer getIcon() {
        return icon;
    }

    public Text getTitle() {
        return title;
    }
}

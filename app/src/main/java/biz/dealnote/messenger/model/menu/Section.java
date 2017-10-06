package biz.dealnote.messenger.model.menu;

import android.support.annotation.DrawableRes;

import biz.dealnote.messenger.model.Text;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public class Section {

    @DrawableRes
    private Integer icon;

    private final Text title;

    public Section(Text title) {
        this.title = title;
    }

    public Section setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }

    public Text getTitle() {
        return title;
    }

    public Integer getIcon() {
        return icon;
    }
}

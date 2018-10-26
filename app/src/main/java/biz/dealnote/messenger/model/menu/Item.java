package biz.dealnote.messenger.model.menu;

import androidx.annotation.DrawableRes;
import biz.dealnote.messenger.model.Icon;
import biz.dealnote.messenger.model.Text;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public class Item {

    private Icon icon;

    private final int key;

    private final Text title;

    private Section section;

    private int extra;

    public Item setExtra(int extra) {
        this.extra = extra;
        return this;
    }

    public int getExtra() {
        return extra;
    }

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

    public Item setIcon(@DrawableRes int res) {
        this.icon = Icon.fromResources(res);
        return this;
    }

    public Item setIcon(String remoteUrl) {
        this.icon = Icon.fromUrl(remoteUrl);
        return this;
    }

    public Item setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public Section getSection() {
        return section;
    }

    public Icon getIcon() {
        return icon;
    }

    public Text getTitle() {
        return title;
    }
}
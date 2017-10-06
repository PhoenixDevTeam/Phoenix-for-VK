package biz.dealnote.messenger.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import biz.dealnote.messenger.adapter.horizontal.Entry;

/**
 * Created by Ruslan Kolbasa on 17.05.2017.
 * phoenix
 */
public class DocFilter implements Entry {

    private final int type;

    @StringRes
    private final int title;

    private boolean active;

    public DocFilter(int type,  @StringRes int title) {
        this.type = type;
        this.title = title;
    }

    public static class Type {
        public static final int ALL = 0;
        public static final int TEXT = 1;
        public static final int ARCHIVE = 2;
        public static final int GIF = 3;
        public static final int IMAGE = 4;
        public static final int AUDIO = 5;
        public static final int VIDEO = 6;
        public static final int BOOKS = 7;
        public static final int OTHER = 8;
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(title);
    }

    public DocFilter setActive(boolean active) {
        this.active = active;
        return this;
    }

    public int getType() {
        return type;
    }

    public int getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}

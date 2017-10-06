package biz.dealnote.messenger.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import biz.dealnote.messenger.adapter.horizontal.Entry;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogEventType implements Entry {

    private final int type;

    @StringRes
    private final int title;

    private boolean active;

    public int getType() {
        return type;
    }

    public LogEventType(int type, int title) {
        this.type = type;
        this.title = title;
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(title);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public LogEventType setActive(boolean active) {
        this.active = active;
        return this;
    }
}

package biz.dealnote.messenger.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ErrorIgnoreDrawerLayout extends DrawerLayout {

    public ErrorIgnoreDrawerLayout(Context context) {
        super(context);
    }

    public ErrorIgnoreDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ignored) {
        }
        return false;
    }
}
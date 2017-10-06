package biz.dealnote.messenger.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ErrorIgnoreViewPager extends ViewPager {

    public ErrorIgnoreViewPager(Context context) {
        super(context);
    }

    public ErrorIgnoreViewPager(Context context, AttributeSet attrs) {
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
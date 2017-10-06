package biz.dealnote.messenger.view.pager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.github.chrisbanes.photoview.OnSingleFlingListener;

import biz.dealnote.messenger.view.FlingRelativeLayout;

import static biz.dealnote.messenger.util.Utils.dpToPx;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
public abstract class CloseOnFlingListener implements OnSingleFlingListener,
        FlingRelativeLayout.OnSingleFlingListener {

    private static final int MIN_Y_DP = 80; // минимальная дистанция
    private static final int MAX_X_DP = 100; // отклонение по оси X
    private static final int THRESHOLD_VELOCITY = 200; // влияет на скорость + зависит от девайса

    private final int maxXPx;
    private final int minYPx;

    public CloseOnFlingListener(@NonNull Context context){
        maxXPx = (int) dpToPx(MAX_X_DP, context);
        minYPx = (int) dpToPx(MIN_Y_DP, context);
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        final float distanceByY = motionEvent.getY() - motionEvent1.getY();

        if (Math.abs(motionEvent.getX() - motionEvent1.getX()) > maxXPx) {
            return false;
        }

        if (Math.abs(v1) < THRESHOLD_VELOCITY) {
            return false;
        }

        if (Math.abs(distanceByY) < minYPx) {
            return false;
        }

        return onVerticalFling(distanceByY);
    }

    public abstract boolean onVerticalFling(float distanceByY);
}

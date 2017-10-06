package biz.dealnote.messenger.view.square;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AlternativeSquareRelativeLayout extends RelativeLayout {

    public AlternativeSquareRelativeLayout(Context context) {
        super(context);
    }

    public AlternativeSquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlternativeSquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }
}

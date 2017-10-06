package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import biz.dealnote.messenger.R;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class AlternativeAspectRatioFrameLayout extends FrameLayout {

    private int mAspectRatioWidth = 16;
    private int mAspectRatioHeight = 9;

    public AlternativeAspectRatioFrameLayout(Context context) {
        super(context);
    }

    public AlternativeAspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AlternativeAspectRatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AlternativeAspectRatioFrameLayout);

        mAspectRatioWidth = a.getInt(R.styleable.AlternativeAspectRatioFrameLayout_altAspectRatioWidth, 16);
        mAspectRatioHeight = a.getInt(R.styleable.AlternativeAspectRatioFrameLayout_altAspectRatioHeight, 9);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    public void setAspectRatio(int w, int h){
        mAspectRatioWidth = w;
        mAspectRatioHeight = h;

        // force re-calculating the layout dimension and the redraw of the view
        requestLayout();
        invalidate();
    }
}
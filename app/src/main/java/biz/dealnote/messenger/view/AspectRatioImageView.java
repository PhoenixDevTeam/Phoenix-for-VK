package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import biz.dealnote.messenger.R;

public class AspectRatioImageView extends ImageView {

    private static final int DEFAULT_PROPORTION_WIDTH = 16;
    private static final int DEFAULT_PROPORTION_HEIGHT = 9;

    private int proportionWidth;
    private int proportionHeight;

    public AspectRatioImageView(Context context) {
        super(context);
        proportionWidth = DEFAULT_PROPORTION_WIDTH;
        proportionHeight = DEFAULT_PROPORTION_HEIGHT;
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AspectRatioImageView,
                0, 0);

        try {
            proportionWidth = a.getInt(R.styleable.AspectRatioImageView_aspect_ration_width, DEFAULT_PROPORTION_WIDTH);
            proportionHeight = a.getInt(R.styleable.AspectRatioImageView_aspect_ration_height, DEFAULT_PROPORTION_HEIGHT);
        } finally {
            a.recycle();
        }
    }

    public void setAspectRatio(int w, int h){
        this.proportionHeight = h;
        this.proportionWidth = w;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * proportionHeight / proportionWidth;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * proportionWidth / proportionHeight;
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));

        //we save widthMeasureSpec in private field to use it for our child measurment in onLayout()
        mWidthMeasureSpec = widthMeasureSpec;
    }

    private int mWidthMeasureSpec;
}

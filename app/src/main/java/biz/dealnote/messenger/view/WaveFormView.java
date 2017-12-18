package biz.dealnote.messenger.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.util.Exestime;

/**
 * Created by admin on 09.10.2016.
 * phoenix
 */
public class WaveFormView extends View {

    public WaveFormView(Context context) {
        this(context, null);
    }

    public WaveFormView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveFormView, 0, 0);
        try {
            mActiveColor = a.getColor(R.styleable.WaveFormView_waveform_active_color, Color.BLUE);
            mNoactiveColor = a.getInt(R.styleable.WaveFormView_waveform_noactive_color, Color.GRAY);
        } finally {
            a.recycle();
        }

        if (isInEditMode()) {
            setCurrentActiveProgress(0.4f);
            setWaveForm(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 6, 6, 9, 10, 13, 9, 8, 12, 13,
                    1, 5, 7, 18, 1, 10, 16, 6, 12, 31, 8, 15, 5, 13, 11, 11, 13, 10, 13, 9, 23, 17,
                    8, 7, 5, 7, 6, 3, 6, 2, 8, 9, 9, 1, 2, 29, 16, 8, 10, 10, 6, 3, 1, 1, 3, 2, 5,
                    9, 11, 13, 14, 7, 3, 6, 2, 3, 5, 5, 9, 10, 11, 11, 2, 0, 1, 2, 6, 7, 8, 5, 2,
                    3, 1, 1, 1, 3, 1, 5, 4, 1, 1, 3});
        }
    }

    @ColorInt
    private int mActiveColor;

    @ColorInt
    private int mNoactiveColor;

    private int mSectionCount = 64;

    private byte[] mWaveForm = new byte[0];
    private float mMaxValue = 50;

    private float mCurrentActiveProgress;

    private float mDisplayedProgress;

    private static final Paint PAINT = new Paint(Paint.FILTER_BITMAP_FLAG
            | Paint.DITHER_FLAG
            | Paint.ANTI_ALIAS_FLAG);

    static {
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeCap(Paint.Cap.ROUND);
        PAINT.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setCurrentActiveProgress(float progress) {
        if (mCurrentActiveProgress == progress) {
            return;
        }

        this.mCurrentActiveProgress = progress;
        this.mDisplayedProgress = progress;

        releaseAnimation();
        invalidate();
    }

    private void releaseAnimation(){
        ObjectAnimator animator = mAnimator.get();

        if(animator != null){
            animator.cancel();
            mAnimator = new WeakReference<>(null);
        }
    }

    private static final Property<WaveFormView, Float> PROGRESS_PROPERTY = new Property<WaveFormView, Float>(Float.class, "displayed-precentage") {
        @Override
        public Float get(WaveFormView view) {
            return view.mDisplayedProgress;
        }

        @Override
        public void set(WaveFormView view, Float value) {
            view.mDisplayedProgress = value;
            view.invalidate();
        }
    };

    private WeakReference<ObjectAnimator> mAnimator = new WeakReference<>(null);

    public void setCurrentActiveProgressSmoothly(float progress){
        if (mCurrentActiveProgress == progress) {
            return;
        }

        this.mCurrentActiveProgress = progress;

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, PROGRESS_PROPERTY, progress);
        mAnimator = new WeakReference<>(animator);

        animator.setDuration(750);
        //animator.setInterpolator(new AccelerateInterpolator(1.75f));
        animator.start();
    }

    public void setCurrentActiveProgress(float progress, boolean anim){
        if(anim){
            setCurrentActiveProgressSmoothly(progress);
        } else {
            setCurrentActiveProgress(progress);
        }
    }

    public void setSectionCount(int sectionCount) {
        this.mSectionCount = sectionCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = calculateSectionWidth();

        PAINT.setStrokeWidth(size * 2f);

        float offset = 0;

        for (int i = 0; i < mWaveForm.length; i++) {
            boolean active = (float) i / (float) mWaveForm.length <= mDisplayedProgress;

            @ColorInt
            int color = active ? mActiveColor : mNoactiveColor;
            PAINT.setColor(color);

            byte value = mWaveForm[i];
            float pxHeight = (float) getHeight() * ((float) value / mMaxValue);

            float verticalPadding = ((float) getHeight() - pxHeight) / 2f;

            float startX = offset + size;

            canvas.drawLine(startX, verticalPadding, startX, pxHeight + verticalPadding, PAINT);

            offset += size * 3;
        }
    }

    private float calculateSectionWidth() {
        int count = mWaveForm.length * 3 - 1;
        return (float) getWidth() / (float) count;
    }

    public void setWaveForm(byte[] waveForm) {
        long start = System.currentTimeMillis();

        this.mWaveForm = new byte[waveForm.length];

        for (int i = 0; i < waveForm.length; i++) {
            mWaveForm[i] = (byte) (waveForm[i] + 1);
        }

        cut();

        Exestime.log("setWaveForm", start, "count: " + waveForm.length);

        invalidateMaxValue();
        invalidate();
    }

    private void cut() {
        byte[] newValues = new byte[mSectionCount];

        for (int i = 0; i < mSectionCount; i++) {
            newValues[i] = getValueAt(mWaveForm, (float) i / (float) mSectionCount);
        }

        this.mWaveForm = newValues;
    }

    private byte getValueAt(byte[] values, float coef) {
        if (values == null){
            return 0;
        }
        int index = (int) ((float) values.length * coef);

        return values[index];
    }

    private void invalidateMaxValue() {
        byte newMaxValue = calculateMaxValue(mWaveForm);
        if (newMaxValue > mMaxValue) {
            mMaxValue = newMaxValue;
        }
    }

    private static byte calculateMaxValue(byte[] values) {
        byte max = values.length > 0 ? values[0] : 0;
        for (byte value : values) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    public void setActiveColor(@ColorInt int activeColor) {
        this.mActiveColor = activeColor;
    }

    public void setNoactiveColor(@ColorInt int noactiveColor) {
        this.mNoactiveColor = noactiveColor;
    }
}

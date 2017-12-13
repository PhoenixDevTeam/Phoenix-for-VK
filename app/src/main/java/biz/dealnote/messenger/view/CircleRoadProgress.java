package biz.dealnote.messenger.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import biz.dealnote.messenger.R;

public class CircleRoadProgress extends View {

    private float circleCenterPointX;
    private float circleCenterPointY;
    private int roadColor;
    private float roadStrokeWidth;
    private float roadRadius;
    private int arcLoadingColor;
    private float arcLoadingStrokeWidth;
    private float arcLoadingStartAngle;

    private int percent;

    public CircleRoadProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        circleCenterPointX = w / 2;
        circleCenterPointY = h / 2;
        int paddingInContainer = 3;
        roadRadius = (w / 2) - (roadStrokeWidth / 2) - paddingInContainer;
    }

    private static final Paint PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRoad(PAINT, canvas);
        drawArcLoading(PAINT, canvas);
    }

    private void initializeAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleRoadProgressWidget);
        circleCenterPointX = ta.getFloat(R.styleable.CircleRoadProgressWidget_circleCenterPointX, 54f);
        circleCenterPointY = ta.getFloat(R.styleable.CircleRoadProgressWidget_circleCenterPointY, 54f);
        roadColor = ta.getColor(R.styleable.CircleRoadProgressWidget_roadColor, Color.parseColor("#575757"));
        roadStrokeWidth = ta.getDimensionPixelSize(R.styleable.CircleRoadProgressWidget_roadStrokeWidth, 10);
        roadRadius = ta.getDimensionPixelSize(R.styleable.CircleRoadProgressWidget_roadRadius, 42);
        arcLoadingColor = ta.getColor(R.styleable.CircleRoadProgressWidget_arcLoadingColor, Color.parseColor("#f5d600"));
        arcLoadingStrokeWidth = ta.getDimensionPixelSize(R.styleable.CircleRoadProgressWidget_arcLoadingStrokeWidth, 3);

        arcLoadingStartAngle = ta.getFloat(R.styleable.CircleRoadProgressWidget_arcLoadingStartAngle, 270f);

        ta.recycle();
    }

    private void drawRoad(Paint paint, Canvas canvas) {
        paint.setDither(true);
        paint.setColor(roadColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(roadStrokeWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawCircle(circleCenterPointX, circleCenterPointY, roadRadius, paint);
    }

    private void drawArcLoading(Paint paint, Canvas canvas) {
        paint.setColor(arcLoadingColor);
        paint.setStrokeWidth(arcLoadingStrokeWidth);

        float delta = circleCenterPointX - roadRadius;
        float arcSize = (circleCenterPointX - (delta / 2f)) * 2f;

        RectF box = new RectF(delta, delta, arcSize, arcSize);
        //float sweep = 360 * percent * 0.01f;
        float sweep = 360 * displayedPercentage * 0.01f;
        canvas.drawArc(box, arcLoadingStartAngle, sweep, false, paint);
    }

    public void changePercentage(int percent) {
        this.percent = percent;
        this.displayedPercentage = percent;

        invalidate();
    }

    private static final Property<CircleRoadProgress, Float> PROGRESS_PROPERTY = new Property<CircleRoadProgress, Float>(Float.class, "displayed-precentage") {
        @Override
        public Float get(CircleRoadProgress view) {
            return view.displayedPercentage;
        }

        @Override
        public void set(CircleRoadProgress view, Float value) {
            view.displayedPercentage = value;
            view.invalidate();
        }
    };

    public void changePercentageSmoothly(int percent){
        this.percent = percent;

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, PROGRESS_PROPERTY, percent);
        animator.setDuration(750);
        //animator.setInterpolator(new AccelerateInterpolator(1.75f));
        animator.start();
    }

    private float displayedPercentage;
}
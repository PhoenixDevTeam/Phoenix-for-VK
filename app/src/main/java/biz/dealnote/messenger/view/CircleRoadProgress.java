package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.lang.ref.WeakReference;

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
    private int textColor;
    private float textSize;

    private int percent;
    private ProgressHandler handler;

    public CircleRoadProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.handler = new ProgressHandler(this);
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
        drawPercents(canvas);
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
        textColor = ta.getColor(R.styleable.CircleRoadProgressWidget_textColor, Color.parseColor("#ffffff"));
        textSize = ta.getDimensionPixelSize(R.styleable.CircleRoadProgressWidget_textSize, 20);
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

    private void drawPercents(Canvas canvas) {
        String percentsString = String.valueOf(percent);

        Paint textPaint = new Paint();
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int positionX = (getMeasuredWidth() / 2);
        int positionY = (int) ((getMeasuredHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        canvas.drawText(percentsString, positionX, positionY, textPaint);
    }

    public void changePercentage(int percent) {
        this.percent = percent;
        this.displayedPercentage = percent;

        invalidate();
        //invalidateCircle();
        //invalidate();

        //invalidatePercentage();
    }

    public void changePercentageSmoothly(int percent){
        this.percent = percent;
        invalidatePercentage();
    }

    private int displayedPercentage;

    private void invalidatePercentage(){
        if(!handler.hasMessages(M_UPDATE_PROGRESS)){
            handler.sendEmptyMessage(M_UPDATE_PROGRESS);
        }
    }

    private static final int M_UPDATE_PROGRESS = 1;

    private static final class ProgressHandler extends Handler {

        private WeakReference<CircleRoadProgress> reference;

        public ProgressHandler(CircleRoadProgress circleRoadProgress) {
            this.reference = new WeakReference<>(circleRoadProgress);
        }

        @Override
        public void handleMessage(Message msg) {
            CircleRoadProgress instance = reference.get();
            if(instance == null) return;

            if(msg.what == M_UPDATE_PROGRESS){
                if(instance.percent > instance.displayedPercentage){
                    instance.displayedPercentage++;
                }

                if(instance.percent < instance.displayedPercentage){
                    instance.displayedPercentage--;
                }

                try{
                    instance.invalidate();
                } catch (Exception ignored){}

                if(instance.percent != instance.displayedPercentage){
                    instance.handler.sendEmptyMessageDelayed(M_UPDATE_PROGRESS, 20);
                }
            }
        }
    }
}
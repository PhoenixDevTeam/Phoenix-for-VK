package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import biz.dealnote.messenger.R;

/**
 * Created by admin on 26.03.2017.
 * phoenix
 */
public class ColorFilterImageView extends AppCompatImageView {

    public ColorFilterImageView(Context context) {
        this(context, null);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ColorFilterImageView);

        try {
            int color = attrArray.getColor(R.styleable.ColorFilterImageView_filter_color, Color.BLACK);
            setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        } finally {
            attrArray.recycle();
        }
    }
}
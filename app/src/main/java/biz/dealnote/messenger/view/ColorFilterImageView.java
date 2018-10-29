package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;
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

    private int color;
    private boolean disabledColorFilter;

    private void init(Context context, AttributeSet attrs) {
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ColorFilterImageView);

        try {
            color = attrArray.getColor(R.styleable.ColorFilterImageView_filter_color, Color.BLACK);
        } finally {
            attrArray.recycle();
        }

        resolveColorFilter();
    }

    private void resolveColorFilter() {
        if (disabledColorFilter) {
            setImageTintList(null);
        } else {
            setImageTintList(ColorStateList.valueOf(color));
        }
    }

    public void setColorFilterEnabled(boolean enabled) {
        this.disabledColorFilter = !enabled;
        resolveColorFilter();
    }
}
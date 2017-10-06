package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import biz.dealnote.messenger.R;

/**
 * Created by golde on 11.04.2017.
 * phoenix
 */
public class CircleImageButton extends AppCompatImageView {

    public CircleImageButton(Context context) {
        this(context, null);
    }

    public CircleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public void init(Context context, AttributeSet attrs){
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageButton);

        setBackgroundResource(R.drawable.circle_back_white);

        try {
            int bgColor = a.getColor(R.styleable.CircleImageButton_backgroundColor, Color.RED);
            int iconColor = a.getColor(R.styleable.CircleImageButton_iconColor, Color.WHITE);

            getBackground().setColorFilter(bgColor, PorterDuff.Mode.MULTIPLY);
            setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        } finally {
            a.recycle();
        }
    }
}

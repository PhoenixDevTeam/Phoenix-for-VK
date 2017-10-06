package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;

import biz.dealnote.messenger.R;

/**
 * Created by ruslan.kolbasa on 06-Jun-16.
 * phoenix
 */
public class WrapWidthTextView extends AppCompatTextView {

    public WrapWidthTextView(Context context) {
        this(context, null);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private boolean mFixWrapText;

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.WrapWidthTextView, 0, 0);
        try {
            mFixWrapText = a.getBoolean(R.styleable.WrapWidthTextView_fixWrapText, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFixWrapText) {
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                int width = getMaxWidth(getLayout());
                if (width > 0 && width < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), heightMeasureSpec);
                }
            }
        }
    }

    private int getMaxWidth(Layout layout) {
        int linesCount = layout.getLineCount();
        if (linesCount < 2) {
            return 0;
        }

        float maxWidth = 0;
        for (int i = 0; i < linesCount; i++) {
            maxWidth = Math.max(maxWidth, layout.getLineWidth(i));
        }

        return (int) Math.ceil(maxWidth);
    }
}
package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.util.Utils;

public class RoundedButton extends android.support.v7.widget.AppCompatTextView {

    private boolean active;

    public RoundedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public RoundedButton(Context context) {
        super(context);
        init(null, 0);
    }

    public RoundedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    protected void init(AttributeSet attrs, int defStyle) {
        setAllCaps(true);

        final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedButton, defStyle, 0);
        initAttributes(attrArray);
        attrArray.recycle();

        int p8 = (int) Utils.dpToPx(8, getContext());
        int p16 = (int) Utils.dpToPx(16, getContext());
        setPadding(p16, p8, p16, p8);

        setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(Utils.dpToPx(1, getContext()));
        }

        setBackgroundResource(R.drawable.button_round);
        setupColors();
    }

    private void setupColors(){
        setTextColor(active ? mActiveTextColor : mNoActiveTextColor);
        getBackground().setColorFilter(active ? mActiveBackgroundColor : mNoactiveBackgroundColor, PorterDuff.Mode.MULTIPLY);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        setupColors();
    }

    private int mActiveTextColor;
    private int mNoActiveTextColor;
    private int mActiveBackgroundColor;
    private int mNoactiveBackgroundColor;

    protected void initAttributes(TypedArray attrArray) {
        mActiveTextColor = attrArray.getColor(R.styleable.RoundedButton_enabled_text_color, Color.BLACK);
        mNoActiveTextColor = attrArray.getColor(R.styleable.RoundedButton_noenabled_text_color, Color.GRAY);
        mActiveBackgroundColor = attrArray.getColor(R.styleable.RoundedButton_enabled_background_color, Color.WHITE);
        mNoactiveBackgroundColor = attrArray.getColor(R.styleable.RoundedButton_noenabled_background_color, Color.WHITE);
    }
}

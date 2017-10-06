package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import biz.dealnote.messenger.R;

import static biz.dealnote.messenger.util.Utils.dpToPx;
import static biz.dealnote.messenger.util.Utils.spToPx;

/**
 * Created by ruslan.kolbasa on 10.08.2016.
 * mobilebankingandroid
 */
public class TextInputLayout extends LinearLayout {

    private ViewGroup mEditTextRoot;
    private TextView mEditText;

    public TextInputLayout(Context context) {
        super(context);
    }

    public TextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.content_text_input, this, true);
        mEditTextRoot = (ViewGroup) findViewById(R.id.input_edittext_root);

        TextView decriptionTextView = (TextView) findViewById(R.id.input_description_text);

        View dividerView = findViewById(R.id.input_divider);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextInputLayout);

        try {
            String description = ta.getString(R.styleable.TextInputLayout_input_description);
            int descriptionTextSize = ta.getDimensionPixelSize(R.styleable.TextInputLayout_input_description_text_size, (int) spToPx(8f, context));

            @ColorInt
            int descriptionTextColor = ta.getColor(R.styleable.TextInputLayout_input_description_text_color, Color.parseColor("#8a000000"));

            @ColorInt
            int dividerColor = ta.getColor(R.styleable.TextInputLayout_input_divider_color, Color.parseColor("#3d999999"));

            int dividerHeight = ta.getDimensionPixelSize(R.styleable.TextInputLayout_input_divider_height, (int) dpToPx(2f, context));

            dividerView.setBackgroundColor(dividerColor);

            ViewGroup.LayoutParams dividerParams = dividerView.getLayoutParams();
            dividerParams.height = dividerHeight;
            dividerView.setLayoutParams(dividerParams);

            decriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, descriptionTextSize);
            decriptionTextView.setTextColor(descriptionTextColor);
            decriptionTextView.setText(description);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void addView(View child) {
        if(!interceptChildAdding(child)){
            super.addView(child);
        }
    }

    @Override
    public void addView(View child, int index) {
        if(!interceptChildAdding(child)){
            super.addView(child, index);
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if(!interceptChildAdding(child)){
            super.addView(child, index, params);
        }
    }

    private boolean interceptChildAdding(View child){
        if (child instanceof TextView && child.getId() != R.id.input_description_text) {
            mEditText = (TextView) child;

            prepareEditText(mEditText);
            mEditTextRoot.addView(mEditText);
            return true;
        } else {
            return false;
        }
    }

    private void prepareEditText(TextView editText) {
        int dp2 = (int) dpToPx(2f, getContext());
        int dp4 = (int) dpToPx(4f, getContext());

        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setPadding(dp2, dp4, dp2, dp4);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if(!interceptChildAdding(child)){
            super.addView(child, params);
        }
    }

    @Override
    public void addView(View child, int width, int height) {
        if(!interceptChildAdding(child)){
            super.addView(child, width, height);
        }
    }
}

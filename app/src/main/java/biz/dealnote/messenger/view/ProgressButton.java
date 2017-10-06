package biz.dealnote.messenger.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import biz.dealnote.messenger.R;

/**
 * Created by ruslan.kolbasa on 07-Jun-16.
 * mobilebankingandroid
 */
public class ProgressButton extends FrameLayout {

    private ImageView mProgressIcon;
    private View mTitleRoot;
    private TextView mTitleTextView;

    private boolean mProgressNow;

    public ProgressButton(Context context) {
        super(context);
        init(context, null);
    }

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton);

        int layout;
        int buttonColor;
        String buttonTitle;
        boolean allCaps;

        try {
            layout = a.getResourceId(R.styleable.ProgressButton_button_layout, R.layout.content_progress_button);
            buttonColor = a.getColor(R.styleable.ProgressButton_button_color, Color.BLUE);
            buttonTitle = a.getString(R.styleable.ProgressButton_button_text);
            allCaps = a.getBoolean(R.styleable.ProgressButton_button_all_caps, true);
        } finally {
            a.recycle();
        }

        View view = LayoutInflater.from(context).inflate(layout, this, false);
        view.setBackgroundColor(buttonColor);

        mTitleTextView = (TextView) view.findViewById(R.id.progress_button_title_text);

        mTitleTextView.setText(buttonTitle);
        mTitleTextView.setAllCaps(allCaps);

        mProgressIcon = (ImageView) view.findViewById(R.id.progress_button_progress_icon);
        mTitleRoot = view.findViewById(R.id.progress_button_title_root);

        addView(view);

        resolveViews();
    }

    public void setText(CharSequence charSequence){
        mTitleTextView.setText(charSequence);
    }

    private void resolveViews(){
        mProgressIcon.setVisibility(mProgressNow ? View.VISIBLE : View.INVISIBLE);
        mTitleRoot.setVisibility(mProgressNow ? View.INVISIBLE : View.VISIBLE);

        if (mProgressNow) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_button_progress);
            mProgressIcon.startAnimation(animation);
        } else {
            mProgressIcon.clearAnimation();
        }
    }

    public void changeState(boolean progress){
        this.mProgressNow = progress;
        resolveViews();
    }
}

package biz.dealnote.messenger.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import biz.dealnote.messenger.R;

public class CenteredToolbar extends Toolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;

    public CenteredToolbar(Context context) {
        super(context);
        setupTextViews();
    }

    public CenteredToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setupTextViews();
    }

    public CenteredToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupTextViews();
    }

    @Override
    public void setTitle(@StringRes int resId) {
        String s = getResources().getString(resId);
        setTitle(s);
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setText(title);
    }

    @Override
    public void setSubtitle(@StringRes int resId) {
        String s = getResources().getString(resId);
        setSubtitle(s);
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        tvSubtitle.setVisibility(VISIBLE);
        tvSubtitle.setText(subtitle);
    }

    @Override
    public CharSequence getTitle() {
        return tvTitle.getText().toString();
    }

    @Override
    public CharSequence getSubtitle() {
        return tvSubtitle.getText().toString();
    }

    private void setupTextViews() {
        tvTitle = new TextView(getContext());
        tvTitle.setSingleLine();
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setTextAppearance(getContext(), R.style.TextAppearance_MaterialComponents_Headline6);
        tvTitle.setTypeface(ResourcesCompat.getFont(getContext(), R.font.open_sans));

        tvSubtitle = new TextView(getContext());
        tvSubtitle.setSingleLine();
        tvSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        tvSubtitle.setTextAppearance(getContext(), R.style.TextAppearance_MaterialComponents_Subtitle1);
        tvSubtitle.setTypeface(ResourcesCompat.getFont(getContext(), R.font.open_sans));

        LinearLayout linear = new LinearLayout(getContext());
        linear.setGravity(Gravity.CENTER);
        linear.setOrientation(LinearLayout.VERTICAL);
        linear.addView(tvTitle);
        linear.addView(tvSubtitle);

        tvSubtitle.setVisibility(GONE);

        Toolbar.LayoutParams lp = new Toolbar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        linear.setLayoutParams(lp);

        addView(linear);
    }
}

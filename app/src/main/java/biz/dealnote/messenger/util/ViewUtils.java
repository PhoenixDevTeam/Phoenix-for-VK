package biz.dealnote.messenger.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Property;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.settings.CurrentTheme;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class ViewUtils {

    public interface ICountFormatter {
        String format(int count);
    }

    private static final ICountFormatter DEFAULT_COUNT_FORMATTER = String::valueOf;

    public static ObjectAnimator setCountText(TextView view, int count, boolean animate) {
        if (Objects.nonNull(view)) {
            if (animate) {
                ObjectAnimator animator = ObjectAnimator.ofInt(view, createAmountAnimatorProperty(DEFAULT_COUNT_FORMATTER), count);
                animator.setDuration(250);
                animator.start();
                return animator;
            } else {
                view.setTag(count);
                view.setText(DEFAULT_COUNT_FORMATTER.format(count));
            }
        }

        return null;
    }

    private static Property<TextView, Integer> createAmountAnimatorProperty(final ICountFormatter formatter) {
        return new Property<TextView, Integer>(Integer.class, "counter_text") {
            @Override
            public Integer get(TextView view) {
                try {
                    return (Integer) view.getTag();
                } catch (Exception e) {
                    return 0;
                }
            }

            @Override
            public void set(TextView view, Integer value) {
                view.setText(formatter.format(value));
                view.setTag(value);
            }
        };
    }

    public static Integer getOnlineIcon(boolean online, boolean onlineMobile, int platform, int app) {
        if (!online) {
            return null;
        }

        boolean onlinePhoenix = Constants.PHOENIX_FULL_API_ID == app || Constants.PHOENIX_LITE_API_ID == app;

        if(app == 5961172){
            return R.drawable.ic_olivka;
        }

        if(app == 2685278){
            return R.drawable.ic_kate_mobile;
        }

        if (onlinePhoenix) {
            return R.drawable.online_phoenix;
        }

        switch (platform) {
            case VKApiUser.Platform.WEB:
                return R.drawable.web;

            case VKApiUser.Platform.MOBILE:
                return R.drawable.cellphone;

            case VKApiUser.Platform.IPHONE:
            case VKApiUser.Platform.IPAD:
                return R.drawable.apple;

            case VKApiUser.Platform.WINDOWS:
            case VKApiUser.Platform.WPHONE:
                return R.drawable.windows;

            case VKApiUser.Platform.ANDROID:
                return R.drawable.android;

            default:
                if (onlineMobile) {
                    return R.drawable.cellphone;
                } else {
                    return R.drawable.ic_online_web;
                }
        }
    }

    public static void setupSwipeRefreshLayoutWithCurrentTheme(Activity activity, SwipeRefreshLayout swipeRefreshLayout) {
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(CurrentTheme.getMessageBackgroundSquare(activity));

        int primaryColor = CurrentTheme.getIconColorActive(activity);
        int accentColor = CurrentTheme.getColorAccent(activity);
        swipeRefreshLayout.setColorSchemeColors(primaryColor, accentColor);
    }

    public static void displayAvatar(@NonNull ImageView dest, Transformation transformation, String url, String tag, @DrawableRes int ifEmpty) {
        Picasso picasso = PicassoInstance.with();

        RequestCreator requestCreator;

        if (nonEmpty(url)) {
            requestCreator = picasso.load(url);
        } else {
            requestCreator = picasso.load(ifEmpty);
        }

        if (transformation != null) {
            requestCreator.transform(transformation);
        }

        if (tag != null) {
            requestCreator.tag(tag);
        }

        requestCreator.into(dest);
    }

    public static void displayAvatar(@NonNull ImageView dest, Transformation transformation, String url, String tag) {
        displayAvatar(dest, transformation, url, tag, R.drawable.ic_avatar_unknown);
    }

    public static void showProgress(@NonNull final Fragment fragment, final SwipeRefreshLayout swipeRefreshLayout, boolean show) {
        if (!fragment.isAdded() || swipeRefreshLayout == null) return;

        if (!show) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (fragment.isResumed()) {
            swipeRefreshLayout.setRefreshing(true);
        } else {
            swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(true));
        }
    }

    public static void keyboardHide(Context context) {
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(((Activity) context).getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
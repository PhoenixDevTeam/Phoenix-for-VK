package biz.dealnote.messenger.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.squareup.picasso.Transformation;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.util.MaskTransformation;
import biz.dealnote.messenger.util.RoundTransformation;

public class CurrentTheme {

    private static final String KEY_CHAT_BACKGROUND = "chat_background";

    public static Drawable getChatBackground(Activity activity){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String page = preferences.getString(KEY_CHAT_BACKGROUND, "1");
        switch (page){
            case "1":
                return CurrentTheme.getDrawableFromAttribute(activity, R.attr.chat_background_cookies);
            case "2":
                return CurrentTheme.getDrawableFromAttribute(activity, R.attr.chat_background_lines);
            //case "3":
            //    return CurrentTheme.getDrawableFromAttribute(activity, R.attr.chat_background_circular);
            default: //"0
                int color = CurrentTheme.getColorFromAttrs(activity, R.attr.messages_background_color, Color.WHITE);
                return new ColorDrawable(color);
        }
    }

    public static Transformation createTransformationForAvatar(Context context) {
        int style = Settings.get()
                .ui()
                .getAvatarStyle();

        switch (style) {
            case AvatarStyle.OVAL:
                return new MaskTransformation(context, R.drawable.avatar_mask);
            case AvatarStyle.CIRCLE:
                return new RoundTransformation();
            default:
                return new RoundTransformation();
        }
    }

    public static int getColorPrimaryDark(Context context) {
        return getColorFromAttrs(R.attr.colorPrimaryDark, context, "#000000");
    }

    public static int getColorPrimary(Context context) {
        return getColorFromAttrs(R.attr.colorPrimary, context, "#000000");
    }

    public static int getNavigationBarColor(Context context) {
        boolean isWhiteTheme = PreferenceManager.getDefaultSharedPreferences(context).getString("app_theme", "theme6").equals("theme10");
        return isWhiteTheme ? Color.BLACK : getColorFromAttrs(R.attr.colorPrimary, context, "#000000");
    }

    public static int getColorAccent(Context context) {
        return getColorFromAttrs(R.attr.colorAccent, context, "#000000");
    }

    public static int getStatusBarNonColored(Context context) {
        return getColorFromAttrs(R.attr.statusbarNonColoredColor, context, "#000000");
    }

    public static int getMessageUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.message_unread_color, context, "#ffffff");
    }

    public static int getIconColorActive(Context context) {
        return getColorFromAttrs(R.attr.icon_color_active, context, "#000000");
    }

    public static int getIconColorStatic(Context context) {
        return getColorFromAttrs(R.attr.icon_color_static, context, "#000000");
    }

    public static int getMessageBackgroundSquare(Context context) {
        return getColorFromAttrs(R.attr.message_bubble_color, context, "#000000");
    }

    public static int getPrimaryTextColorCode(Context context) {
        return getColorFromAttrs(R.attr.textColorPrimary, context, "#ffffff");
    }

    public static int getIconColorOnColoredBackgroundCode(Context context) {
        return getColorFromAttrs(R.attr.icon_color_on_colored_back, context, "#ffffff");
    }

    public static int getPrimaryTextColorOnColoredBackgroundCode(Context context) {
        return getColorFromAttrs(R.attr.textColorPrimaryOnColoredBack, context, "#ffffff");
    }

    public static int getSecondaryTextColorOnColoredBackgroundCode(Context context) {
        return getColorFromAttrs(R.attr.textColorSecondaryOnColoredBack, context, "#ffffff");
    }

    public static int getDialogsUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.dialogs_unread_color, context, "#20b0b0b0");
    }

    public static int getColorFromAttrs(int resId, Context context, String defaultColor) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(resId, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return a.data;
        } else {
            return Color.parseColor(defaultColor);
        }
    }

    public static int getColorFromAttrs(Context context, int resId, int defaultColor) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(resId, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return a.data;
        } else {
            return defaultColor;
        }
    }

    public static int getBackgroundRes(Activity context, int attrId) {
        //int[] textSizeAttr = new int[]{attrId};
        //int indexOfAttrTextSize = 0;
        //TypedArray a = context.obtainStyledAttributes(textSizeAttr);
        //int resId = a.getResourceId(indexOfAttrTextSize, -1);
        //a.recycle();
        //return resId;
        return getResIdFromAttribute(context, attrId);
    }

    public static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0) {
            return 0;
        }

        final TypedValue typedvalueattr = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public static Drawable getDrawableFromAttribute(final Activity activity, final int attr) {
        int resId = getResIdFromAttribute(activity, attr);
        return ContextCompat.getDrawable(activity, resId);
    }

    private static String intToHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int getSecondaryTextColorCode(Context context) {
        return getColorFromAttrs(R.attr.textColorSecondary, context, "#000000");
    }

    public static int getDisableTextColorCode(Context context) {
        return getColorFromAttrs(R.attr.textColorDisabled, context, "#000000");
    }
}

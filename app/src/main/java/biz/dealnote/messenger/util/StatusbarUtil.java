package biz.dealnote.messenger.util;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Ruslan Kolbasa on 12.06.2017.
 * phoenix
 */
public class StatusbarUtil {

    private static final String STATUS_BAR_SETUP_TAG = "StatusbarTag";

    public static void setCustomStatusbarDarkMode(Activity activity, boolean dark){
        try {
            //http://blog.isming.me/2016/01/09/chang-android-statusbar-text-color/
            if(setMiuiStatusBarDarkMode(activity, dark)){
                Logger.d(STATUS_BAR_SETUP_TAG, "This is MIUI");
            } else if(setMeizuStatusBarDarkIcon(activity, dark)){
                Logger.d(STATUS_BAR_SETUP_TAG, "This is FLYME");
            }
        } catch (Exception ignored){

        }
    }

    private static boolean setMeizuStatusBarDarkIcon(Activity activity, boolean dark) {
        boolean result = false;

        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();

            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");

            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);

            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }

            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);

            result = true;
        } catch (Exception ignored) {

        }

        return result;
    }

    private static boolean setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");

            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
            return true;
        } catch (Exception ignored) {

        }

        return false;
    }
}
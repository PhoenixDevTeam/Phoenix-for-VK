package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import biz.dealnote.messenger.BuildConfig;

public class AppPrefs {

    public static final boolean FULL_APP = BuildConfig.FULL_APP;

    public static boolean isCoubInstalled(Context context) {
        return isPackageIntalled(context, "com.coub.android");
    }

    public static boolean isYoutubeInstalled(Context context) {
        return isPackageIntalled(context, "com.google.android.youtube");
    }

    public static boolean isAudioPlayAllowed(Context context) {
        return isPackageIntalled(context, "ru.test.musicenabler");
    }

    private static boolean isPackageIntalled(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    public static long restorePts(Context context, int accountId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("pts" + accountId, 0L);
    }

    public static void setPts(Context context, int acooutnId, long pts) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("pts" + acooutnId, pts).apply();
    }

    public static void storeTs(Context context, int accountId, long ts) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("ts" + accountId, ts).apply();
    }

    public static long restoreTs(Context context, int accountId) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong("ts" + accountId, 0L);
    }

    public static boolean isFullApp() {
        return FULL_APP;
    }

    public static final String[] ONLY_FULL_APP_PREFS = {
            "night_switch", "app_theme", "night_mode_time", "night_theme", "avatar_style",
            "light_sidebar_background", "dark_sidebar_background", "reset_sidebar_background"
    };
}

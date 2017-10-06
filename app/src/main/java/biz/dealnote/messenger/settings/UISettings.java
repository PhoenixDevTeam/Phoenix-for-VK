package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.NavigationFragment;
import biz.dealnote.messenger.fragment.PreferencesFragment;
import biz.dealnote.messenger.model.drawer.AbsDrawerItem;

/**
 * Created by admin on 01.12.2016.
 * phoenix
 */
class UISettings implements ISettings.IUISettings {

    private static final String KEY_NIGHT_START_TIME = "night_mode_start_time";
    private static final String KEY_NIGHT_END_TIME = "night_mode_end_time";

    private final Context app;

    UISettings(Context context) {
        this.app = context.getApplicationContext();
    }

    @Override
    public int getAvatarStyle() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        //noinspection ResourceType
        return preferences.getInt(PreferencesFragment.KEY_AVATAR_STYLE, AvatarStyle.CIRCLE);
    }

    @Override
    public boolean isNavigationbarColored() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        return preferences.getBoolean(PreferencesFragment.KEY_NAVIGATION_COLORED, false);
    }

    @Override
    public void storeAvatarStyle(@AvatarStyle int style) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(PreferencesFragment.KEY_AVATAR_STYLE, style)
                .apply();
    }

    @Override
    public int getMainTheme() {
        boolean night = isDarkModeEnabled();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String theme = preferences.getString("app_theme", "theme6");
        if (!night) {
            switch (theme) {
                case "theme1":
                    return R.style.Theme1;
                case "theme2":
                    return R.style.Theme2;
                case "theme3":
                    return R.style.Theme3;
                case "theme4":
                    return R.style.Theme4;
                case "theme5":
                    return R.style.Theme5;
                case "theme6":
                    return R.style.Theme6;
                case "theme7":
                    return R.style.Theme7;
                case "theme8":
                    return R.style.Theme8;
                case "theme9":
                    return R.style.Theme9;
                case "theme10":
                    return R.style.Theme10;
                case "theme11":
                    return R.style.Theme11;
                case "theme12":
                    return R.style.Theme12;
                default:
                    return R.style.Theme6;
            }
        } else {
            String nightback = preferences.getString("night_theme", "dark");
            if (nightback.equals("dark")) {
                switch (theme) {
                    case "theme1":
                        return R.style.DarkTheme1;
                    case "theme2":
                        return R.style.DarkTheme2;
                    case "theme3":
                        return R.style.DarkTheme3;
                    case "theme4":
                        return R.style.DarkTheme4;
                    case "theme5":
                        return R.style.DarkTheme5;
                    case "theme6":
                        return R.style.DarkTheme6;
                    case "theme7":
                        return R.style.DarkTheme7;
                    case "theme8":
                        return R.style.DarkTheme8;
                    case "theme9":
                        return R.style.DarkTheme9;
                    case "theme10":
                        return R.style.DarkTheme10;
                    case "theme11":
                        return R.style.DarkTheme11;
                    case "theme12":
                        return R.style.DarkTheme12;
                    default:
                        return R.style.DarkTheme6;
                }
            }
            if (nightback.equals("black")) {
                switch (theme) {
                    case "theme1":
                        return R.style.BlackTheme1;
                    case "theme2":
                        return R.style.BlackTheme2;
                    case "theme3":
                        return R.style.BlackTheme3;
                    case "theme4":
                        return R.style.BlackTheme4;
                    case "theme5":
                        return R.style.BlackTheme5;
                    case "theme6":
                        return R.style.BlackTheme6;
                    case "theme7":
                        return R.style.BlackTheme7;
                    case "theme8":
                        return R.style.BlackTheme8;
                    case "theme9":
                        return R.style.BlackTheme9;
                    case "theme10":
                        return R.style.BlackTheme10;
                    case "theme11":
                        return R.style.BlackTheme11;
                    case "theme12":
                        return R.style.BlackTheme12;

                    default:
                        return R.style.BlackTheme6;
                }
            }

            if (nightback.equals("lightgray")) {
                switch (theme) {
                    case "theme1":
                        return R.style.LightGrayTheme1;
                    case "theme2":
                        return R.style.LightGrayTheme2;
                    case "theme3":
                        return R.style.LightGrayTheme3;
                    case "theme4":
                        return R.style.LightGrayTheme4;
                    case "theme5":
                        return R.style.LightGrayTheme5;
                    case "theme6":
                        return R.style.LightGrayTheme6;
                    case "theme7":
                        return R.style.LightGrayTheme7;
                    case "theme8":
                        return R.style.LightGrayTheme8;
                    case "theme9":
                        return R.style.LightGrayTheme9;
                    case "theme10":
                        return R.style.LightGrayTheme10;
                    case "theme11":
                        return R.style.LightGrayTheme11;
                    case "theme12":
                        return R.style.LightGrayTheme12;

                    default:
                        return R.style.LightGrayTheme6;
                }
            }
        }

        return R.style.Theme6;
    }

    @Override
    public boolean isDarkModeEnabled() {
        boolean night = (getNightMode() == NightMode.ENABLE);
        boolean autonight = (getNightMode() == NightMode.AUTO) && (getAutoTheme() == NightMode.ENABLE);
        return (night || autonight);
    }

    @Override
    public int getQuickReplyTheme() {
        if (!isDarkModeEnabled()) {
            return R.style.QuickReply;
        } else {
            return R.style.DarkQuickReply;
        }
    }

    @NightMode
    @Override
    public int getNightMode() {
        String mode = PreferenceManager.getDefaultSharedPreferences(app)
                .getString("night_switch", String.valueOf(NightMode.DISABLE));
        //noinspection ResourceType
        return Integer.parseInt(mode);
    }

    @Override
    public void setNightStartTime(int hour) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_NIGHT_START_TIME, hour)
                .apply();
    }

    @Override
    public int getNightStartTime() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getInt(KEY_NIGHT_START_TIME, 23 * 60);
    }

    @Override
    public void setNightEndTime(int hour) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_NIGHT_END_TIME, hour)
                .apply();
    }

    @Override
    public int getNightEndTime() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getInt(KEY_NIGHT_END_TIME, 5 * 60);
    }

    @Override
    public AbsDrawerItem getDefaultPage() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String page = preferences.getString(PreferencesFragment.KEY_DEFAULT_CATEGORY, "2");
        switch (page) {
            case "1":
                return NavigationFragment.SECTION_ITEM_FRIENDS;
            case "2":
                return NavigationFragment.SECTION_ITEM_DIALOGS;
            case "3":
                return NavigationFragment.SECTION_ITEM_FEED;
            case "4":
                return NavigationFragment.SECTION_ITEM_FEEDBACK;
            case "5":
                return NavigationFragment.SECTION_ITEM_GROUPS;
            case "6":
                return NavigationFragment.SECTION_ITEM_PHOTOS;
            case "7":
                return NavigationFragment.SECTION_ITEM_VIDEOS;
            case "8":
                return NavigationFragment.SECTION_ITEM_AUDIOS;
            case "9":
                return NavigationFragment.SECTION_ITEM_DOCS;
            case "10":
                return NavigationFragment.SECTION_ITEM_BOOKMARKS;
            default:
                return NavigationFragment.SECTION_ITEM_DIALOGS;
        }
    }

    @Override
    public boolean isSystemEmoji() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("emojis_type", false);
    }

    private int getAutoTheme() {
        Calendar c = Calendar.getInstance();

        int minutes = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        int startTime = getNightStartTime();
        int endTime = getNightEndTime();

        boolean isNight;

        if (startTime >= endTime) {
            isNight = minutes >= startTime || minutes <= endTime;
        } else {
            isNight = minutes >= startTime && minutes <= endTime;
        }

        if (isNight) {
            return NightMode.ENABLE;
        }

        return NightMode.DISABLE;
    }

    @Override
    public boolean isMonochromeWhite(){
        return getMainTheme() == R.style.Theme10;
    }
}

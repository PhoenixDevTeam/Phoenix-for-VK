package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.PreferencesFragment;
import biz.dealnote.messenger.fragment.fave.FaveTabsFragment;
import biz.dealnote.messenger.fragment.friends.FriendsTabsFragment;
import biz.dealnote.messenger.fragment.search.SeachTabsFragment;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;

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
    public Place getDefaultPage(int accountId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String page = preferences.getString(PreferencesFragment.KEY_DEFAULT_CATEGORY, "last_closed");

        if("last_closed".equals(page)){
            int type = PreferenceManager.getDefaultSharedPreferences(app).getInt("last_closed_place_type", Place.DIALOGS);
            switch (type){
                case Place.DIALOGS:
                    return PlaceFactory.getDialogsPlace(accountId, accountId, null);
                case Place.FEED:
                    return PlaceFactory.getFeedPlace(accountId);
                case Place.FRIENDS_AND_FOLLOWERS:
                    return PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
                case Place.NOTIFICATIONS:
                    return PlaceFactory.getNotificationsPlace(accountId);
                case Place.NEWSFEED_COMMENTS:
                    return PlaceFactory.getNewsfeedCommentsPlace(accountId);
                case Place.COMMUNITIES:
                    return PlaceFactory.getCommunitiesPlace(accountId, accountId);
                case Place.VK_PHOTO_ALBUMS:
                    return PlaceFactory.getVKPhotoAlbumsPlace(accountId, accountId, null, null);
                case Place.AUDIOS:
                    return PlaceFactory.getAudiosPlace(accountId, accountId);
                case Place.DOCS:
                    return PlaceFactory.getDocumentsPlace(accountId, accountId, null);
                case Place.BOOKMARKS:
                    return PlaceFactory.getBookmarksPlace(accountId, FaveTabsFragment.TAB_PHOTOS);
                case Place.SEARCH:
                    return PlaceFactory.getSearchPlace(accountId, SeachTabsFragment.TAB_PEOPLE, null);
                case Place.PREFERENCES:
                    return PlaceFactory.getPreferencesPlace(accountId);
            }
        }

        switch (page) {
            case "1":
                return PlaceFactory.getFriendsFollowersPlace(accountId, accountId, FriendsTabsFragment.TAB_ALL_FRIENDS, null);
            case "2":
                return PlaceFactory.getDialogsPlace(accountId, accountId, null);
            case "3":
                return PlaceFactory.getFeedPlace(accountId);
            case "4":
                return PlaceFactory.getNotificationsPlace(accountId);
            case "5":
                return PlaceFactory.getCommunitiesPlace(accountId, accountId);
            case "6":
                return PlaceFactory.getVKPhotoAlbumsPlace(accountId, accountId, null, null);
            case "7":
                return PlaceFactory.getVideosPlace(accountId, accountId, null);
            case "8":
                return PlaceFactory.getAudiosPlace(accountId, accountId);
            case "9":
                return PlaceFactory.getDocumentsPlace(accountId, accountId, null);
            case "10":
                return PlaceFactory.getBookmarksPlace(accountId, FaveTabsFragment.TAB_PHOTOS);
            default:
                return PlaceFactory.getDialogsPlace(accountId, accountId, null);
        }
    }

    @Override
    public void notifyPlaceResumed(int type) {
        PreferenceManager.getDefaultSharedPreferences(app).edit()
                .putInt("last_closed_place_type", type)
                .apply();
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

package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

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

    private static final String KEY_LOCKSCREEN_ART = "lockscreen_art";

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String theme = preferences.getString("app_theme", "theme6");
        boolean amoledMode = preferences.getBoolean("amoled_night_mode", false);
        switch (theme) {
            case "theme1":
                return amoledMode ? R.style.DayNightBlack1 : R.style.Theme1;
            case "theme2":
                return amoledMode ? R.style.DayNightBlack2 : R.style.Theme2;
            case "theme3":
                return amoledMode ? R.style.DayNightBlack3 : R.style.Theme3;
            case "theme4":
                return amoledMode ? R.style.DayNightBlack4 : R.style.Theme4;
            case "theme5":
                return amoledMode ? R.style.DayNightBlack5 : R.style.Theme5;
            case "theme6":
                return amoledMode ? R.style.DayNightBlack6 : R.style.Theme6;
            case "theme7":
                return amoledMode ? R.style.DayNightBlack7 : R.style.Theme7;
            case "theme8":
                return amoledMode ? R.style.DayNightBlack8 : R.style.Theme8;
            case "theme9":
                return amoledMode ? R.style.DayNightBlack9 : R.style.Theme9;
            case "theme10":
                return amoledMode ? R.style.DayNightBlack10 : R.style.Theme10;
            case "theme11":
                return amoledMode ? R.style.DayNightBlack11 : R.style.Theme11;
            case "theme12":
                return amoledMode ? R.style.DayNightBlack12 : R.style.Theme12;
            default:
                return amoledMode ? R.style.DayNightBlack6 : R.style.Theme6;
        }
    }

    @Override
    public boolean isDarkModeEnabled(Context context) {
        int nightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            default:
                return false;
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
    public Place getDefaultPage(int accountId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        String page = preferences.getString(PreferencesFragment.KEY_DEFAULT_CATEGORY, "last_closed");

        if ("last_closed".equals(page)) {
            int type = PreferenceManager.getDefaultSharedPreferences(app).getInt("last_closed_place_type", Place.DIALOGS);
            switch (type) {
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
                case Place.VIDEOS:
                    return PlaceFactory.getVideosPlace(accountId, accountId, null);
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

    @Override
    public boolean isMonochromeWhite(Context context) {
        return (getMainTheme() == R.style.Theme10 || getMainTheme() == R.style.DayNightBlack10) && !isDarkModeEnabled(context);
    }

    @Override
    public boolean showLockscreenArt() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean(KEY_LOCKSCREEN_ART, false);
    }
}

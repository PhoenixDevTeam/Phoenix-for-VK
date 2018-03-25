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
                return amoledMode ? R.style.App_DayNight_Amoled_Blue : R.style.App_DayNight_Blue;
            case "theme2":
                return amoledMode ? R.style.App_DayNight_Amoled_LightBlue : R.style.App_DayNight_LightBlue;
            case "theme3":
                return amoledMode ? R.style.App_DayNight_Amoled_Grey : R.style.App_DayNight_Grey;
            case "theme4":
                return amoledMode ? R.style.App_DayNight_Amoled_Teal : R.style.App_DayNight_Teal;
            case "theme5":
                return amoledMode ? R.style.App_DayNight_Amoled_Red : R.style.App_DayNight_Red;
            case "theme6":
                return amoledMode ? R.style.App_DayNight_Amoled_Indigo : R.style.App_DayNight_Indigo;
            case "theme7":
                return amoledMode ? R.style.App_DayNight_Amoled_Pink : R.style.App_DayNight_Pink;
            case "theme8":
                return amoledMode ? R.style.App_DayNight_Amoled_Orange : R.style.App_DayNight_Orange;
            case "theme9":
                return amoledMode ? R.style.App_DayNight_Amoled_Purple : R.style.App_DayNight_Purple;
            case "theme10":
                return amoledMode ? R.style.App_DayNight_Amoled_Monochrome : R.style.App_DayNight_Monochrome;
            case "theme11":
                return amoledMode ? R.style.App_DayNight_Amoled_Green : R.style.App_DayNight_Green;
            case "theme12":
                return amoledMode ? R.style.App_DayNight_Amoled_Pixel : R.style.App_DayNight_Pixel;
            default:
                return amoledMode ? R.style.App_DayNight_Amoled_Indigo : R.style.App_DayNight_Indigo;
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
        return (getMainTheme() == R.style.App_DayNight_Monochrome || getMainTheme() == R.style.App_DayNight_Amoled_Monochrome) && !isDarkModeEnabled(context);
    }
}
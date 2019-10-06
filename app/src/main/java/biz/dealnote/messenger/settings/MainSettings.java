package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.util.Optional;

/**
 * Created by ruslan.kolbasa on 02.12.2016.
 * phoenix
 */
class MainSettings implements ISettings.IMainSettings {

    private static final String KEY_IMAGE_SIZE = "image_size";
    private static final String KEY_RUN_COUNT = "run_count";
    private static final String KEY_DOUBLE_PRESS_TO_EXIT = "double_press_to_exit";
    private static final String KEY_CUSTOM_TABS = "custom_tabs";

    private final Context app;

    private Optional<Integer> prefferedPhotoPreviewSize;

    MainSettings(Context context) {
        app = context.getApplicationContext();
        prefferedPhotoPreviewSize = Optional.empty();
    }

    @Override
    public boolean isSendByEnter() {
        return getDefaultPreferences().getBoolean("send_by_enter", false);
    }

    @Override
    public boolean isNeedDoublePressToExit() {
        return getDefaultPreferences().getBoolean(KEY_DOUBLE_PRESS_TO_EXIT, true);
    }

    @Nullable
    @Override
    public Integer getUploadImageSize() {
        String i = getDefaultPreferences().getString(KEY_IMAGE_SIZE, "0");
        switch (i) {
            case "1":
                return Upload.IMAGE_SIZE_800;
            case "2":
                return Upload.IMAGE_SIZE_1200;
            case "3":
                return Upload.IMAGE_SIZE_FULL;
            default:
                return null;
        }
    }

    @Override
    public int getPrefPreviewImageSize() {
        if (prefferedPhotoPreviewSize.isEmpty()) {
            prefferedPhotoPreviewSize = Optional.wrap(restorePhotoPreviewSize());
        }

        return prefferedPhotoPreviewSize.get();
    }

    @PhotoSize
    private int restorePhotoPreviewSize() {
        try {
            return Integer.parseInt(getDefaultPreferences().getString("photo_preview_size", String.valueOf(PhotoSize.X)));
        } catch (Exception e) {
            return PhotoSize.X;
        }
    }

    private SharedPreferences getDefaultPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Override
    public void notifyPrefPreviewSizeChanged() {
        prefferedPhotoPreviewSize = Optional.empty();
    }

    @PhotoSize
    @Override
    public int getPrefDisplayImageSize(@PhotoSize int byDefault) {
        return getDefaultPreferences().getInt("pref_display_photo_size", byDefault);
    }

    @Override
    public void setPrefDisplayImageSize(@PhotoSize int size) {
        getDefaultPreferences()
                .edit()
                .putInt("pref_display_photo_size", size)
                .apply();
    }

    @Override
    public boolean isCustomTabEnabled() {
        return getDefaultPreferences().getBoolean(KEY_CUSTOM_TABS, true);
    }
}
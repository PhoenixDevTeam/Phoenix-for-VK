package biz.dealnote.messenger.settings;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.upload.UploadObject;

/**
 * Created by ruslan.kolbasa on 02.12.2016.
 * phoenix
 */
class MainSettings implements ISettings.IMainSettings {

    private static final String KEY_IMAGE_SIZE = "image_size";
    private static final String KEY_RUN_COUNT = "run_count";
    private static final String KEY_DOUBLE_PRESS_TO_EXIT = "double_press_to_exit";

    private final Context app;

    MainSettings(Context context) {
        this.app = context.getApplicationContext();
    }

    @Override
    public void incrementRunCount() {
        int current = getRunCount();
        setRunCount(current + 1);
    }

    @Override
    public int getRunCount() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getInt(KEY_RUN_COUNT, 0);
    }

    @Override
    public void setRunCount(int count) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_RUN_COUNT, count)
                .apply();
    }

    @Override
    public boolean isSendByEnter() {
        return PreferenceManager.getDefaultSharedPreferences(app).getBoolean("send_by_enter", false);
    }

    @Override
    public boolean isNeedDoublePressToExit() {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(KEY_DOUBLE_PRESS_TO_EXIT, true);
    }

    @Nullable
    @Override
    public Integer getUploadImageSize() {
        String i = PreferenceManager.getDefaultSharedPreferences(app).getString(KEY_IMAGE_SIZE, "0");
        switch (i) {
            case "1":
                return UploadObject.IMAGE_SIZE_800;
            case "2":
                return UploadObject.IMAGE_SIZE_1200;
            case "3":
                return UploadObject.IMAGE_SIZE_FULL;
            default:
                return null;
        }
    }

    @PhotoSize
    @Override
    public int getPrefDisplayImageSize(@PhotoSize int byDefault) {
        //noinspection ResourceType
        return PreferenceManager.getDefaultSharedPreferences(app).getInt("pref_display_photo_size", byDefault);

    }

    @Override
    public void setPrefDisplayImageSize(@PhotoSize int size) {
        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt("pref_display_photo_size", size)
                .apply();
    }
}

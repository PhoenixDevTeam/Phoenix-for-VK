package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;

public class NotificationPreferencesFragment extends PreferenceFragment {

    private static final String TAG = NotificationPreferencesFragment.class.getSimpleName();

    public static final int REQUEST_CODE_RINGTONE = 116;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notication_settings);

        findPreference("notif_sound").setOnPreferenceClickListener(preference -> {
            showAlertDialog();
            return true;
        });
    }

    @Override
    protected void finalize() throws Throwable {
        Logger.d(TAG, "finalize");
        super.finalize();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.preference_list_fragment, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));
        return root;
    }

    private Ringtone current;

    private void stopRingtoneIfExist() {
        if (current != null && current.isPlaying()) {
            current.stop();
        }
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private int selection;

    private void showAlertDialog() {
        final Map<String, String> ringrones = getNotifications();

        final Set<String> keys = ringrones.keySet();
        final String[] array = keys.toArray(new String[keys.size()]);

        String selectionKey = getKeyByValue(ringrones, Settings.get()
                .notifications()
                .getNotificationRingtone());

        selection = Arrays.asList(array).indexOf(selectionKey);

        new AlertDialog.Builder(getActivity()).setSingleChoiceItems(array, selection, (dialog, which) -> {
            selection = which;
            stopRingtoneIfExist();
            String title = array[which];
            String uri = ringrones.get(title);
            Ringtone r = RingtoneManager.getRingtone(getActivity(), Uri.parse(uri));
            current = r;
            r.play();
        }).setPositiveButton("OK", (dialog, which) -> {
            if (selection == -1) {
                Toast.makeText(getActivity(), R.string.ringtone_not_selected, Toast.LENGTH_SHORT).show();
            } else {
                String title = array[selection];
                Settings.get()
                        .notifications()
                        .setNotificationRingtoneUri(ringrones.get(title));
                stopRingtoneIfExist();
            }
        })
                .setNegativeButton(R.string.cancel, (dialog, which) -> stopRingtoneIfExist())
                .setNeutralButton(R.string.ringtone_custom, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/audio");
                    startActivityForResult(intent, REQUEST_CODE_RINGTONE);
                }).setOnDismissListener(dialog -> stopRingtoneIfExist()).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRingtoneIfExist();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_RINGTONE:
                if (resultCode == Activity.RESULT_OK) {
                    String uri = data.getData().getPath();
                    Settings.get()
                            .notifications()
                            .setNotificationRingtoneUri(uri);
                }
                break;
        }
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager = new RingtoneManager(getActivity());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();
        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri notificationUri = manager.getRingtoneUri(cursor.getPosition());
            list.put(notificationTitle, notificationUri.toString());
        }

        list.put(getString(R.string.ringtone_vk), Settings.get()
                .notifications()
                .getDefNotificationRingtone());
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(R.string.notif_setting_title);
        }

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onSectionResume(NavigationFragment.SECTION_ITEM_SETTINGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }
}
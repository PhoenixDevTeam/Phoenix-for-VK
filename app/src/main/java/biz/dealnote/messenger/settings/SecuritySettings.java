package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.util.AeSimpleSHA1;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by admin on 20.10.2016.
 * phoenix
 */
public class SecuritySettings implements ISettings.ISecuritySettings {

    private static final String PREFS_NAME = "security_prefs";
    private static final String KEY_PIN_HASH = "app_pin";
    private static final String KEY_PIN_ENTER_HISTORY = "pin_enter_history";
    public static final String KEY_USE_PIN_FOR_SECURITY = "use_pin_for_security";
    private static final String KEY_USE_PIN_FOR_ENTRANCE = "use_pin_for_entrance";
    public static final String KEY_CHANGE_PIN = "change_pin";
    public static final String KEY_DELETE_KEYS = "delete_all_encryption_keys";

    private static final String KEY_ENCRYPTION_POLICY_ACCEPTED = "encryption_policy_accepted";

    private Context mApplication;
    private final SharedPreferences mPrefs;
    private String mPinHash;
    private List<Long> mPinEnterHistory;
    private boolean mKeyEncryptionPolicyAccepted;

    SecuritySettings(Context context) {
        mApplication = context.getApplicationContext();
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPinHash = mPrefs.getString(KEY_PIN_HASH, null);
        mPinEnterHistory = extractPinEnterHistrory(mPrefs);
        mKeyEncryptionPolicyAccepted = mPrefs.getBoolean(KEY_ENCRYPTION_POLICY_ACCEPTED, false);
    }

    @NonNull
    private static ArrayList<Long> extractPinEnterHistrory(@NonNull SharedPreferences preferences) {
        Set<String> set = preferences.getStringSet(KEY_PIN_ENTER_HISTORY, null);
        ArrayList<Long> result = new ArrayList<>(Utils.safeCountOf(set));
        if (set != null) {
            int index = 0;
            for (String value : set) {
                result.add(index, Long.parseLong(value));
                index++;
            }
        }

        Collections.sort(result);
        return result;
    }

    private String getPinHash() {
        return mPinHash;
    }

    private void setPinHash(String pinHash) {
        this.mPinHash = pinHash;
        if (Objects.isNull(pinHash)) {
            mPrefs.edit().remove(KEY_PIN_HASH).apply();
        } else {
            mPrefs.edit().putString(KEY_PIN_HASH, pinHash).apply();
        }
    }

    @NonNull
    public List<Long> getPinEnterHistory() {
        return mPinEnterHistory;
    }

    private static final int PIN_HISTORY_DEPTH = 3;

    private void storePinHistory() {
        Set<String> target = new HashSet<>(mPinEnterHistory.size());
        for (long value : mPinEnterHistory) {
            target.add(String.valueOf(value));
        }

        mPrefs.edit().putStringSet(KEY_PIN_ENTER_HISTORY, target).apply();
    }

    public void clearPinHistory() {
        mPinEnterHistory.clear();
        mPrefs.edit().remove(KEY_PIN_ENTER_HISTORY).apply();
    }

    public void firePinAttemptNow() {
        long now = System.currentTimeMillis();
        mPinEnterHistory.add(now);
        if (mPinEnterHistory.size() > PIN_HISTORY_DEPTH) {
            mPinEnterHistory.remove(0);
        }

        storePinHistory();
    }

    private static String encryptionKeyFor(int accountId, int peerId) {
        return "encryptionkeypolicy" + accountId + "_" + peerId;
    }

    public void enableMessageEncryption(int accountId, int peerId, @KeyLocationPolicy int policy) {
        mPrefs.edit()
                .putInt(encryptionKeyFor(accountId, peerId), policy)
                .apply();
    }

    public boolean isMessageEncryptionEnabled(int accountId, int peerId) {
        return mPrefs.contains(encryptionKeyFor(accountId, peerId));
    }

    public void disableMessageEncryption(int accountId, int peerId) {
        mPrefs.edit()
                .remove(encryptionKeyFor(accountId, peerId))
                .apply();
    }

    @KeyLocationPolicy
    public int getEncryptionLocationPolicy(int accountId, int peerId) {
        @KeyLocationPolicy
        int result = mPrefs.getInt(encryptionKeyFor(accountId, peerId), KeyLocationPolicy.PERSIST);
        return result;
    }

    public boolean hasPinHash() {
        return !TextUtils.isEmpty(mPinHash);
    }

    @Override
    public int getPinHistoryDepth() {
        return PIN_HISTORY_DEPTH;
    }

    @Override
    public boolean needHideMessagesBodyForNotif() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication)
                .getBoolean("hide_notif_message_body", false);
    }

    public boolean isUsePinForSecurity() {
        return hasPinHash() && PreferenceManager.getDefaultSharedPreferences(mApplication)
                .getBoolean(KEY_USE_PIN_FOR_SECURITY, false);
    }

    @Override
    public boolean isEntranceByFingerprintAllowed() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication).getBoolean("allow_fingerprint", false);
    }

    public boolean isUsePinForEntrance() {
        return hasPinHash() && PreferenceManager.getDefaultSharedPreferences(mApplication)
                .getBoolean(KEY_USE_PIN_FOR_ENTRANCE, false);
    }

    public void setPin(@Nullable int[] pin) {
        setPinHash(Objects.isNull(pin) ? null : calculatePinHash(pin));
    }

    private String calculatePinHash(@NonNull int[] values) {
        StringBuilder builder = new StringBuilder();
        for (int value : values) {
            builder.append(value);
        }

        return calculateHash(builder.toString());
    }

    private static String calculateHash(String value) {
        try {
            return AeSimpleSHA1.sha1(value);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }

    public boolean isPinValid(@NonNull int[] values) {
        String hash = calculatePinHash(values);
        return hash.equals(getPinHash());
    }

    public void setKeyEncryptionPolicyAccepted(boolean accepted) {
        this.mKeyEncryptionPolicyAccepted = accepted;
        this.mPrefs.edit()
                .putBoolean(KEY_ENCRYPTION_POLICY_ACCEPTED, accepted)
                .apply();
    }

    public boolean isKeyEncryptionPolicyAccepted() {
        return mKeyEncryptionPolicyAccepted;
    }
}

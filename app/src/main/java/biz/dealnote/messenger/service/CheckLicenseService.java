package biz.dealnote.messenger.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.settings.AppPrefs;
import biz.dealnote.messenger.util.Logger;

public class CheckLicenseService extends Service {

    private static final String TAG = CheckLicenseService.class.getSimpleName();

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        @Override
        public void allow(int reason) {
            Logger.d(TAG, "allow, reason=" + reason);
            sendBroadcast(true);
        }

        @Override
        public void dontAllow(int reason) {
            switch (reason){
                case Policy.NOT_LICENSED:
                    sendBroadcast(false);
                    break;
                default:
                    sendBroadcast(true);
            }

            Logger.d(TAG, "dontAllow, reason=" + reason);
        }

        @Override
        public void applicationError(int errorCode) {
            sendBroadcast(true);
            Logger.d(TAG, "applicationError, errorCode=" + errorCode);
        }
    }

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApmICtebjIwE/fhUPcjNdwpOQD/hjxcVuY/T4CINXHfeCkZsZs39E1PEth0hgfGLYK5t0h5FqkCDLLl8AZRsoZ6z/kkImlqf6HnfQ9CwsRRpUvbqcadpGlfWcMdp6ElxbQ9k30X6Yz4b7yJoQ5kEeCrdNtQ5OJOwxQdA6c7Mg6SHslP8202f7UqesLDwvXD5mmfPjvV2XkNdGDsfDqHLBt35qnpmG1gnOnOkmBT46xJ9uEPRJhPJc52pkzCy6nxkWfIefGb6A5v+3j33SJoCTV3QhNfdUqvfdZLhAavT3d2TKSAOArPv+nVKlR1TWQfGQECzsfaZCwT2KCwakE3W+nQIDAQAB";
    private static final byte[] SALT = new byte[]{5, 4, 9, 5, 0, 1, 4, 7, 2, 7, 2, 7, 9, 2, 5, 1, 8, 3, 7, 9};
    private LicenseChecker mChecker;

    @Override
    public void onCreate() {
        super.onCreate();
        // Try to use more data here. ANDROID_ID is a single point of attack.
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(), deviceId)), BASE64_PUBLIC_KEY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!BuildConfig.DEBUG && AppPrefs.FULL_APP){
            doCheck();
            Logger.d(TAG, "onStartCommand");
        }

        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private LicenseCheckerCallback mLicenseCheckerCallback;

    private void doCheck() {
        mChecker.checkAccess(mLicenseCheckerCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public final static String EXTRA_LICENSE_SUCCESS = "success";
    public final static String BROADCAST_ACTION = "biz.dealnote.phoenix.service.CheckLicenseService.broadcast";

    private void sendBroadcast(boolean success){
        Intent intent = new Intent(BROADCAST_ACTION);
        intent.putExtra(EXTRA_LICENSE_SUCCESS, success);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

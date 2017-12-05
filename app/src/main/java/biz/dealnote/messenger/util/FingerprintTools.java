package biz.dealnote.messenger.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;

/**
 * Created by Ruslan Kolbasa on 2/6/17.
 * mosst-moneytransfer-android
 */
public class FingerprintTools {

    public enum SensorState {
        NOT_SUPPORTED,
        NOT_BLOCKED, // device has no pin lock screen
        NO_FINGERPRINTS, // device has no valid fingerprints
        READY
    }

    public static boolean checkFingerprintCompatibility(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (manager != null) {
                return manager.isHardwareDetected();
            }
        }

        /*if (manager.isHardwareDetected()) {
            //code here
        }

        return FingerprintManagerCompat.from(context).isHardwareDetected();*/
        return false;
    }

    public static SensorState checkSensorState(Context context) {
        if (checkFingerprintCompatibility(context)) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

            if (!keyguardManager.isKeyguardSecure()) {
                return SensorState.NOT_BLOCKED;
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !((FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE)).hasEnrolledFingerprints()) {
                return SensorState.NO_FINGERPRINTS;
            }

            return SensorState.READY;
        } else {
            return SensorState.NOT_SUPPORTED;
        }
    }
}
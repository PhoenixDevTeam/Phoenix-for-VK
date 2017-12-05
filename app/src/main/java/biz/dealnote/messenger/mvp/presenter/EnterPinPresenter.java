package biz.dealnote.messenger.mvp.presenter;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IEnterPinView;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Action;
import biz.dealnote.messenger.util.FingerprintTools;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class EnterPinPresenter extends RxSupportPresenter<IEnterPinView> {

    private static final String TAG = EnterPinPresenter.class.getSimpleName();

    private static final String SAVE_VALUE = "save_value";
    private static final int LAST_CIRCLE_VISIBILITY_DELAY = 200;
    private static final int NO_VALUE = -1;

    private int[] mValues;
    private final IOwnersInteractor ownersInteractor;
    private final ISettings.ISecuritySettings securitySettings;

    public EnterPinPresenter(@Nullable Bundle savedState) {
        super(savedState);
        this.securitySettings = Settings.get().security();
        this.ownersInteractor = InteractorFactory.createOwnerInteractor();

        if (savedState != null) {
            this.mValues = savedState.getIntArray(SAVE_VALUE);
        } else {
            this.mValues = new int[Constants.PIN_DIGITS_COUNT];
            resetPin();
        }

        if (Objects.isNull(mOwner)) {
            loadOwnerInfo();
        }

        if(canUseFingerprint()){
            fingerprintCallback = new WeakFingerprintCallback(this);
        } else {
            fingerprintCallback = null;
        }
    }

    private void loadOwnerInfo() {
        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId != ISettings.IAccountsSettings.INVALID_ID) {
            appendDisposable(ownersInteractor.getBaseOwnerInfo(accountId, accountId, IOwnersInteractor.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onOwnerInfoReceived, t -> {/*ignore*/}));
        }
    }

    private void onOwnerInfoReceived(Owner owner){
        this.mOwner = owner;
        resolveAvatarView();
    }

    private Owner mOwner;

    public void onFingerprintClicked() {
        if(!securitySettings.isEntranceByFingerprintAllowed()){
            safeShowError(getView(), R.string.error_login_by_fingerprint_not_allowed);
            return;
        }

        FingerprintTools.SensorState sensorState = FingerprintTools.checkSensorState(getApplicationContext());
        switch (sensorState){
            case NOT_BLOCKED:
            case NO_FINGERPRINTS:
                safeShowError(getView(), R.string.error_fingerprint_not_blocked);
                break;
            case NOT_SUPPORTED:
                safeShowError(getView(), R.string.error_fingerprint_not_supported);
                break;
            case READY:
                safeShowToast(getView(), R.string.message_fingerprint_touch_sensor, false);
                break;
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();

        if(canUseFingerprint()){
            observeFingerprintActions();
        }
    }

    private CancellationSignal fingerprintCancellationSignal;

    private void observeFingerprintActions(){
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return;
        }

        FingerprintManager manager = (FingerprintManager) getApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);

        this.fingerprintCancellationSignal = new CancellationSignal();

        final FingerprintManager.AuthenticationCallback callback = (FingerprintManager.AuthenticationCallback) fingerprintCallback;
        manager.authenticate(null, fingerprintCancellationSignal, 0, callback, null);
    }

    private final Object fingerprintCallback;

    @TargetApi(Build.VERSION_CODES.M)
    private static final class WeakFingerprintCallback extends FingerprintManager.AuthenticationCallback {

        final WeakReference<EnterPinPresenter> reference;

        WeakFingerprintCallback(EnterPinPresenter instance) {
            this.reference = new WeakReference<>(instance);
        }

        void callPresenter(final Action<EnterPinPresenter> action){
            final EnterPinPresenter presenter = reference.get();
            if(nonNull(presenter)){
                action.call(presenter);
            }
        }

        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            if(errMsgId != FingerprintManager.FINGERPRINT_ERROR_CANCELED){
                callPresenter(targer -> targer.safeShowError(targer.getView(), errString.toString()));
            }
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            callPresenter(targer -> targer.safeShowError(targer.getView(), helpString.toString()));
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            callPresenter(EnterPinPresenter::onFingerprintRecognizeSuccess);
        }

        @Override
        public void onAuthenticationFailed() {
            callPresenter(targer -> targer.safeShowError(targer.getView(), R.string.error_fingerprint_failed));
        }
    }

    private void onFingerprintRecognizeSuccess(){
        if(isGuiReady()){
            getView().sendSuccessAndClose();
        }
    }

    private boolean canUseFingerprint(){
        if(!securitySettings.isEntranceByFingerprintAllowed()){
            return false;
        }

        FingerprintTools.SensorState sensorState = FingerprintTools.checkSensorState(getApplicationContext());
        return sensorState == FingerprintTools.SensorState.READY;
    }

    @Override
    public void onGuiPaused() {
        super.onGuiPaused();
        if(nonNull(fingerprintCancellationSignal)){
            fingerprintCancellationSignal.cancel();
            fingerprintCancellationSignal = null;
        }
    }

    @OnGuiCreated
    private void resolveAvatarView() {
        if(!isGuiReady()) return;

        String avatar = Objects.isNull(mOwner) ? null : mOwner.getMaxSquareAvatar();
        if(isEmpty(avatar)){
            getView().displayDefaultAvatar();
        } else {
            getView().displayAvatarFromUrl(avatar);
        }
    }

    private static final int MAX_ATTEMPT_DELAY = 3 * 60 * 1000;

    private long getNextPinAttemptTimeout() {
        List<Long> history = Settings.get()
                .security()
                .getPinEnterHistory();

        if (history.size() < Settings.get().security().getPinHistoryDepth()) {
            return 0;
        }

        long howLongAgoWasFirstAttempt = System.currentTimeMillis() - history.get(0);
        return howLongAgoWasFirstAttempt < MAX_ATTEMPT_DELAY ? MAX_ATTEMPT_DELAY - howLongAgoWasFirstAttempt : 0;
    }

    @OnGuiCreated
    private void refreshViewCirclesVisibility() {
        if (isGuiReady()) {
            getView().displayPin(mValues, NO_VALUE);
        }
    }

    public void onBackspaceClicked() {
        int currentIndex = getCurrentIndex();
        if (currentIndex == -1) {
            mValues[mValues.length - 1] = NO_VALUE;
        } else if (currentIndex > 0) {
            mValues[currentIndex - 1] = NO_VALUE;
        }

        refreshViewCirclesVisibility();
    }

    private Handler mHandler = new Handler();

    private void onFullyEntered() {
        if (!isFullyEntered()) return;

        long timeout = getNextPinAttemptTimeout();
        if (timeout > 0) {
            safeShowError(getView(), R.string.limit_exceeded_number_of_attempts_message, timeout / 1000);

            resetPin();
            refreshViewCirclesVisibility();
            return;
        }

        Settings.get()
                .security()
                .firePinAttemptNow();

        if (Settings.get().security().isPinValid(mValues)) {
            onEnteredRightPin();
        } else {
            onEnteredWrongPin();
        }
    }

    private void onEnteredRightPin() {
        Settings.get()
                .security()
                .clearPinHistory();

        getView().sendSuccessAndClose();
    }

    private void onEnteredWrongPin() {
        resetPin();
        refreshViewCirclesVisibility();

        getView().showError(R.string.pin_is_invalid_message);
        getView().displayErrorAnimation();
    }

    public void onNumberClicked(int value) {
        if (isFullyEntered()) return;

        mValues[getCurrentIndex()] = value;
        refreshViewCirclesVisibility();

        if (isFullyEntered()) {
            mHandler.removeCallbacks(mOnFullyEnteredRunnable);
            mHandler.postDelayed(mOnFullyEnteredRunnable, LAST_CIRCLE_VISIBILITY_DELAY);
        }
    }

    private Runnable mOnFullyEnteredRunnable = this::onFullyEntered;

    private boolean isFullyEntered() {
        for (int value : mValues) {
            if (value == NO_VALUE) {
                return false;
            }
        }

        return true;
    }

    private int getCurrentIndex() {
        for (int i = 0; i < mValues.length; i++) {
            if (mValues[i] == NO_VALUE) {
                return i;
            }
        }

        return -1;
    }

    private void resetPin() {
        for (int i = 0; i < mValues.length; i++) {
            mValues[i] = NO_VALUE;
        }
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putIntArray(SAVE_VALUE, mValues);
    }

    @Override
    protected String tag() {
        return TAG;
    }
}

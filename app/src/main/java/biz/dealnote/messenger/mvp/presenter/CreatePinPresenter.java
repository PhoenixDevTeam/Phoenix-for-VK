package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.os.Handler;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.ICreatePinView;
import biz.dealnote.mvp.reflect.OnGuiCreated;

/**
 * Created by ruslan.kolbasa on 10-Jun-16.
 * mobilebankingandroid
 */
public class CreatePinPresenter extends RxSupportPresenter<ICreatePinView> {

    private static final String TAG = CreatePinPresenter.class.getSimpleName();
    private static final int LAST_CIRCLE_VISIBILITY_DELAY = 200;
    private static final int NO_VALUE = -1;

    private static final String SAVE_STEP = "save_step";
    private static final String SAVE_CREATED_PIN = "save_created_pin";
    private static final String SAVE_REPEATED_PIN = "save_repeated_pin";

    private static final int STEP_CREATE = 1;
    private static final int STEP_REPEAT = 2;

    private int mCurrentStep;
    private int[] mCreatedPin;
    private int[] mRepeatedPin;
    private Handler mHandler = new Handler();

    public CreatePinPresenter(Bundle savedInstanceState){
        super(savedInstanceState);
        if(savedInstanceState != null){
            mCurrentStep = savedInstanceState.getInt(SAVE_STEP);
            mCreatedPin = savedInstanceState.getIntArray(SAVE_CREATED_PIN);
            mRepeatedPin = savedInstanceState.getIntArray(SAVE_REPEATED_PIN);
        } else {
            mCurrentStep = STEP_CREATE;
            mCreatedPin = new int[Constants.PIN_DIGITS_COUNT];
            mRepeatedPin = new int[Constants.PIN_DIGITS_COUNT];
            resetPin(mCreatedPin);
            resetPin(mRepeatedPin);
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public boolean fireBackButtonClick(){
        if(mCurrentStep == STEP_CREATE) {
            return true;
        }

        mCurrentStep = STEP_CREATE;
        resetPin(mCreatedPin);
        resetPin(mRepeatedPin);
        resolveAllViews();
        return false;
    }

    public void fireFingerPrintClick(){
        getView().showError(R.string.not_yet_implemented_message);
    }

    public void fireBackspaceClick(){
        int[] targetPin = mCurrentStep == STEP_CREATE ? mCreatedPin : mRepeatedPin;
        int currentIndex = getNextNoEnteredIndex(targetPin);
        if (currentIndex == -1) {
            targetPin[targetPin.length - 1] = NO_VALUE;
        } else if (currentIndex > 0) {
            targetPin[currentIndex - 1] = NO_VALUE;
        }

        refreshViewCirclesVisibility();
    }

    private void resolveAllViews(){
        refreshViewCirclesVisibility();
        resolveTitles();
    }

    @OnGuiCreated
    private void resolveTitles(){
        if(mCurrentStep == STEP_CREATE){
            getView().displayTitle(R.string.create_pin_code_title);
        } else {
            getView().displayTitle(R.string.repeat_pin_code_title);
        }
    }

    public void fireDigitClick(int digit){
        int[] targetPin = mCurrentStep == STEP_CREATE ? mCreatedPin : mRepeatedPin;
        if(isFullyEntered(targetPin)){
            return;
        }

        appendDigit(targetPin, digit);
        refreshViewCirclesVisibility();

        if(isFullyEntered(targetPin)){
            mHandler.removeCallbacks(mOnFullyEnteredRunnable);
            mHandler.postDelayed(mOnFullyEnteredRunnable, LAST_CIRCLE_VISIBILITY_DELAY);
        }
    }

    private void appendDigit(int[] pin, int digit){
        pin[getNextNoEnteredIndex(pin)] = digit;
    }

    private Runnable mOnFullyEnteredRunnable = new Runnable() {
        @Override
        public void run() {
            if(mCurrentStep == STEP_CREATE){
                onCreatedPinFullyEntered();
            } else {
                onRepeatedPinFullyEntered();
            }
        }
    };

    private void onRepeatedPinFullyEntered(){
        if(!isPinsMatch()){
            resetPin(mRepeatedPin);
            resetPin(mCreatedPin);
            mCurrentStep = STEP_CREATE;

            if(isGuiReady()){
                getView().showError(R.string.entered_pin_codes_do_not_match);
                resolveAllViews();
                getView().displayErrorAnimation();
            }

            return;
        }

        if(isGuiReady()) {
            getView().sendSuccessAndClose(mCreatedPin);
        }
    }

    private void onCreatedPinFullyEntered(){
        resetPin(mRepeatedPin);
        mCurrentStep = STEP_REPEAT;
        resolveAllViews();
    }

    public void fireSkipButtonClick(){
        getView().sendSkipAndClose();
    }

    private boolean isPinsMatch(){
        for(int i = 0; i < Constants.PIN_DIGITS_COUNT; i++){
            if(mCreatedPin[i] == NO_VALUE || mRepeatedPin[i] == NO_VALUE || mCreatedPin[i] != mRepeatedPin[i]){
                return false;
            }
        }

        return true;
    }

    private int getNextNoEnteredIndex(int[] pin) {
        for (int i = 0; i < pin.length; i++) {
            if (pin[i] == NO_VALUE) {
                return i;
            }
        }

        return -1;
    }

    private boolean isFullyEntered(int[] pin) {
        for (int value : pin) {
            if (value == NO_VALUE) {
                return false;
            }
        }

        return true;
    }

    @OnGuiCreated
    private void refreshViewCirclesVisibility(){
        if(mCurrentStep == STEP_CREATE){
            getView().displayPin(mCreatedPin, NO_VALUE);
        } else {
            getView().displayPin(mRepeatedPin, NO_VALUE);
        }
    }

    private void resetPin(int[] pin) {
        for (int i = 0; i < pin.length; i++) {
            pin[i] = NO_VALUE;
        }
    }
}

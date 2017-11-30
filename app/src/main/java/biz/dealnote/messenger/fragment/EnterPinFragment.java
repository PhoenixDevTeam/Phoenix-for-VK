package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.mvp.presenter.EnterPinPresenter;
import biz.dealnote.messenger.mvp.view.IEnterPinView;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.view.KeyboardView;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class EnterPinFragment extends BasePresenterFragment<EnterPinPresenter, IEnterPinView>
        implements IEnterPinView, KeyboardView.OnKeyboardClickListener {

    private static final String TAG = EnterPinFragment.class.getSimpleName();

    public static EnterPinFragment newInstance(){
        Bundle bundle = new Bundle();
        EnterPinFragment fragment = new EnterPinFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ImageView mAvatar;
    private View mValuesRoot;
    private View[] mValuesCircles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_enter_pin, container, false);

        KeyboardView keyboardView = root.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardClickListener(this);

        mAvatar = root.findViewById(R.id.avatar);
        mValuesRoot = root.findViewById(R.id.value_root);

        mValuesCircles = new View[Constants.PIN_DIGITS_COUNT];
        mValuesCircles[0] = root.findViewById(R.id.pincode_digit_0_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[1] = root.findViewById(R.id.pincode_digit_1_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[2] = root.findViewById(R.id.pincode_digit_2_root).findViewById(R.id.pincode_digit_circle);
        mValuesCircles[3] = root.findViewById(R.id.pincode_digit_3_root).findViewById(R.id.pincode_digit_circle);
        return root;
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    public IPresenterFactory<EnterPinPresenter> getPresenterFactory(@Nullable final Bundle saveInstanceState) {
        return () -> new EnterPinPresenter(saveInstanceState);
    }

    @Override
    public void displayPin(int[] values, int noValueConstant) {
        if(Objects.isNull(mValuesCircles)) return;

        if(values.length != mValuesCircles.length){
            throw new IllegalStateException("Invalid pin length, view: " + mValuesCircles.length + ", target: " + values.length);
        }

        for(int i = 0; i < mValuesCircles.length; i++){
            boolean visible = values[i] != noValueConstant;
            mValuesCircles[i].setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void sendSuccessAndClose() {
        if(isAdded()){
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    @Override
    public void displayErrorAnimation() {
        if(Objects.nonNull(mValuesRoot)){
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_invalid_pin);
            mValuesRoot.startAnimation(animation);
        }
    }

    @Override
    public void displayAvatarFromUrl(@NonNull String url) {
        if(Objects.nonNull(mAvatar)){
            PicassoInstance.with()
                    .load(url)
                    .error(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar(getActivity()))
                    .into(mAvatar);
        }
    }

    @Override
    public void displayDefaultAvatar() {
        if(Objects.nonNull(mAvatar)){
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar(getActivity()))
                    .into(mAvatar);
        }
    }

    @Override
    public void onButtonClick(int number) {
        getPresenter().onNumberClicked(number);
    }

    @Override
    public void onBackspaceClick() {
        getPresenter().onBackspaceClicked();
    }

    @Override
    public void onFingerPrintClick() {
        getPresenter().onFingerprintClicked();
    }
}

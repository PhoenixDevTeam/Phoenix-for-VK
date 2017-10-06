package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.ApiVersion;
import biz.dealnote.messenger.api.Auth;
import biz.dealnote.messenger.api.CaptchaNeedException;
import biz.dealnote.messenger.api.NeedValidationException;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.LoginResponse;
import biz.dealnote.messenger.model.Captcha;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IDirectAuthView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.trimmedNonEmpty;

/**
 * Created by admin on 16.07.2017.
 * phoenix
 */
public class DirectAuthPresenter extends RxSupportPresenter<IDirectAuthView> {

    private final INetworker networker;

    private Captcha requieredCaptcha;
    private boolean requireSmsCode;
    private boolean requireAppCode;

    private boolean loginNow;

    private String username;
    private String pass;
    private String smsCode;
    private String captcha;
    private String appCode;

    public DirectAuthPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.networker = Injection.provideNetworkInterfaces();
    }

    @Override
    protected String tag() {
        return DirectAuthPresenter.class.getSimpleName();
    }

    public void fireLoginClick() {
        doLogin(false);
    }

    private void doLogin(boolean forceSms) {
        getView().hideKeyboard();

        final String trimmedUsername = nonEmpty(username) ? username.trim() : "";
        final String trimmedPass = nonEmpty(pass) ? pass.trim() : "";
        final String captchaSid = Objects.nonNull(requieredCaptcha) ? requieredCaptcha.getSid() : null;
        final String captchaCode = nonEmpty(captcha) ? captcha.trim() : null;

        final String code;

        if(requireSmsCode){
            code = (nonEmpty(smsCode) ? smsCode.trim() : null);
        } else if(requireAppCode){
            code = (nonEmpty(appCode) ? appCode.trim() : null);
        } else {
            code = null;
        }

        setLoginNow(true);
        appendDisposable(networker.vkDirectAuth()
                .directLogin("password", Constants.API_ID, Constants.SECRET,
                        trimmedUsername, trimmedPass, ApiVersion.CURRENT, true,
                        Auth.getScope(), code, captchaSid, captchaCode, forceSms)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLoginResponse, t -> onLoginError(getCauseIfRuntime(t))));
    }

    private void onLoginError(Throwable t) {
        t.printStackTrace();

        setLoginNow(false);

        this.requieredCaptcha = null;
        this.requireAppCode = false;
        this.requireSmsCode = false;

        if (t instanceof CaptchaNeedException) {
            String sid = ((CaptchaNeedException) t).getSid();
            String img = ((CaptchaNeedException) t).getImg();
            this.requieredCaptcha = new Captcha(sid, img);
        } else if (t instanceof NeedValidationException) {
            String type = ((NeedValidationException) t).getValidationType();

            if ("2fa_sms".equalsIgnoreCase(type)) {
                requireSmsCode = true;
            } else if("2fa_app".equalsIgnoreCase(type)){
                requireAppCode = true;
            }
        } else {
            showError(getView(), t);
        }

        resolveCaptchaViews();
        resolveSmsRootVisibility();
        resolveAppCodeRootVisibility();
        resolveButtonLoginState();

        if (Objects.nonNull(requieredCaptcha)) {
            callView(IDirectAuthView::moveFocusToCaptcha);
        } else if (requireSmsCode) {
            callView(IDirectAuthView::moveFocusToSmsCode);
        } else if(requireAppCode){
            callView(IDirectAuthView::moveFocusToAppCode);
        }
    }

    @OnGuiCreated
    private void resolveSmsRootVisibility() {
        if (isGuiReady()) {
            getView().setSmsRootVisible(requireSmsCode);
        }
    }

    @OnGuiCreated
    private void resolveAppCodeRootVisibility(){
        if (isGuiReady()) {
            getView().setAppCodeRootVisible(requireAppCode);
        }
    }

    @OnGuiCreated
    private void resolveCaptchaViews() {
        if (isGuiReady()) {
            getView().setCaptchaRootVisible(Objects.nonNull(requieredCaptcha));

            if (Objects.nonNull(requieredCaptcha)) {
                getView().displayCaptchaImage(requieredCaptcha.getImg());
            }
        }
    }

    private void onLoginResponse(LoginResponse response) {
        setLoginNow(false);

        if (nonEmpty(response.access_token) && response.user_id > 0) {
            callView(view -> view.returnSuccessToParent(response.user_id, response.access_token));
        }
    }

    private void setLoginNow(boolean loginNow) {
        this.loginNow = loginNow;
        resolveLoadingViews();
    }

    @OnGuiCreated
    private void resolveLoadingViews() {
        if (isGuiReady()) {
            getView().displayLoading(loginNow);
        }
    }

    public void fireLoginViaWebClick() {
        getView().returnLoginViaWebAction();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveButtonLoginState();
    }

    private void resolveButtonLoginState() {
        if (isGuiResumed()) {
            getView().setLoginButtonEnabled(trimmedNonEmpty(username)
                    && nonEmpty(pass)
                    && (Objects.isNull(requieredCaptcha) || trimmedNonEmpty(captcha))
                    && (!requireSmsCode || trimmedNonEmpty(smsCode))
                    && (!requireAppCode || trimmedNonEmpty(appCode)));
        }
    }

    public void fireLoginEdit(CharSequence sequence) {
        this.username = sequence.toString();
        resolveButtonLoginState();
    }

    public void firePasswordEdit(CharSequence s) {
        this.pass = s.toString();
        resolveButtonLoginState();
    }

    public void fireSmsCodeEdit(CharSequence sequence) {
        this.smsCode = sequence.toString();
        resolveButtonLoginState();
    }

    public void fireCaptchaEdit(CharSequence s) {
        this.captcha = s.toString();
        resolveButtonLoginState();
    }

    public void fireButtonSendCodeViaSmsClick() {
        doLogin(true);
    }

    public void fireAppCodeEdit(CharSequence s) {
        this.appCode = s.toString();
        resolveButtonLoginState();
    }
}
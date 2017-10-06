package biz.dealnote.mvp.core;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public interface PresenterAction<P extends IPresenter<V>, V extends IMvpView> {
    void call(P presenter);
}

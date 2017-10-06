package biz.dealnote.mvp.core;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public interface IPresenterFactory<T extends IPresenter> {
    T create();
}

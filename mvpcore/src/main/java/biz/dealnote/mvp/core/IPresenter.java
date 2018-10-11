package biz.dealnote.mvp.core;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public interface IPresenter<V extends IMvpView> {
    void saveState(@NonNull Bundle savedState);

    void destroy();
    void resumeView();
    void pauseView();
    void attachViewHost(@NonNull V view);
    void detachViewHost();
    void createView(@NonNull V view);
    void destroyView();
}
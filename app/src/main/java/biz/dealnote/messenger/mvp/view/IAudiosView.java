package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioFilter;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 1/4/2018.
 * Phoenix-for-VK
 */
public interface IAudiosView extends IMvpView, IErrorView, IAccountDependencyView {
    void fillFilters(List<AudioFilter> sources);
    void displayList(List<Audio> audios);
    void notifyListChanged();

    void notifyFilterListChanged();
    void displayRefreshing(boolean refresing);
    void setBlockedScreen(boolean visible);

    void showFilters(boolean canFilter);
}
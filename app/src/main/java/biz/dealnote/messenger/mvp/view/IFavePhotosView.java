package biz.dealnote.messenger.mvp.view;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public interface IFavePhotosView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Photo> photos);
    void notifyDataSetChanged();
    void notifyDataAdded(int position, int count);
    void showRefreshing(boolean refreshing);
    void goToGallery(int accountId, ArrayList<Photo> photos, int position);
}
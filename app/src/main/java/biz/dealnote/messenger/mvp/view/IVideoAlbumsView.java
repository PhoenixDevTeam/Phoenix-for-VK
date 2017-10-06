package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.VideoAlbum;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideoAlbumsView extends IMvpView, IAccountDependencyView, IErrorView {

    void displayData(@NonNull List<VideoAlbum> data);
    void notifyDataAdded(int position, int count);
    void displayLoading(boolean loading);
    void notifyDataSetChanged();

    void openAlbum(int accountId, int ownerId, int albumId, String action, String title);
}

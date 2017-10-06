package biz.dealnote.messenger.mvp.view;

import android.support.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public interface IVideosListView extends IAccountDependencyView, IMvpView, IToolbarView, IErrorView {

    String ACTION_SELECT = "VideosFragment.ACTION_SELECT";
    String ACTION_SHOW = "VideosFragment.ACTION_SHOW";

    void displayData(@NonNull List<Video> data);
    void notifyDataAdded(int position, int count);
    void displayLoading(boolean loading);

    void notifyDataSetChanged();

    void returnSelectionToParent(Video video);

    void showVideoPreview(int accountId, Video video);
}

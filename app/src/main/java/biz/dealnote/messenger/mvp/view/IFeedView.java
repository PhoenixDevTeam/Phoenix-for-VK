package biz.dealnote.messenger.mvp.view;

import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.model.FeedSource;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by ruslan.kolbasa on 14.12.2016.
 * phoenix
 */
public interface IFeedView extends IAccountDependencyView, IAttachmentsPlacesView, IMvpView, IErrorView {

    void displayFeedSources(List<FeedSource> sources);
    void notifyFeedSourcesChanged();

    void displayFeed(List<News> data, @Nullable String rawScrollState);
    void notifyFeedDataChanged();
    void notifyDataAdded(int position, int count);
    void notifyItemChanged(int position);

    void setupLoadMoreFooter(@LoadMoreState int state);
    void showRefreshing(boolean refreshing);
    void scrollFeedSourcesToPosition(int position);

    void goToLikes(int accountId, String type, int ownerId, int id);
    void goToReposts(int accountId, String type, int ownerId, int id);

    void goToPostComments(int accountId, int postId, int ownerId);
}
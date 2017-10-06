package biz.dealnote.messenger.listener;

import android.widget.AbsListView;
import android.widget.ListView;

public class OnLoadMoreScrollListener implements AbsListView.OnScrollListener {

    private Callback callback;
    private ListView listView;

    public OnLoadMoreScrollListener(Callback callback, ListView listView) {
        this.callback = callback;
        this.listView = listView;
    }

    private int currentVisibleItemCount;
    private int currentScrollState;
    private int lastItem;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentVisibleItemCount = visibleItemCount;
        lastItem = firstVisibleItem + visibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        this.isScrollCompleted();
    }

    private void isScrollCompleted() {
        if (this.currentVisibleItemCount > 0 && this.currentScrollState == SCROLL_STATE_IDLE) {
            if (lastItem - listView.getHeaderViewsCount() == listView.getAdapter().getCount()) {
                callback.onScrollToEndOfList();
            }
        }
    }

    public interface Callback {
        void onScrollToEndOfList();
    }
}

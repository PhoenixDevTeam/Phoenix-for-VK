package biz.dealnote.messenger.listener;

import android.support.v7.widget.RecyclerView;

import biz.dealnote.messenger.api.PicassoInstance;

public class PicassoPauseOnScrollListener extends RecyclerView.OnScrollListener {

    private String tag;

    public PicassoPauseOnScrollListener(String tag) {
        this.tag = tag;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            PicassoInstance.with().resumeTag(tag);
        } else {
            PicassoInstance.with().pauseTag(tag);
        }
    }
}
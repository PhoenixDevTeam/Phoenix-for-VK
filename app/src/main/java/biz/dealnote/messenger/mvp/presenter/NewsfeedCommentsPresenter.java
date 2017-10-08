package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.INewsfeedInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.NewsfeedComment;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.INewsfeedCommentsView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedCommentsPresenter extends PlaceSupportPresenter<INewsfeedCommentsView> {

    private static final String TAG = NewsfeedCommentsPresenter.class.getSimpleName();

    private final List<NewsfeedComment> data;
    private String nextFrom;

    private final INewsfeedInteractor interactor;

    public NewsfeedCommentsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.data = new ArrayList<>();
        this.interactor = InteractorFactory.createNewsfeedInteractor();

        loadAtLast();
    }

    private boolean loadingNow;

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveLoadingView();
    }

    private void resolveLoadingView(){
        if(isGuiResumed()){
            getView().showLoading(loadingNow);
        }
    }

    private void loadAtLast() {
        setLoadingNow(true);

        load(null);
    }

    private void load(final String startFrom){
        appendDisposable(interactor.getNewsfeedComments(getAccountId(), 10, startFrom, "post,photo,video")
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onDataReceived(startFrom, pair.getSecond(), pair.getFirst()),
                        throwable -> onRequestError(Utils.getCauseIfRuntime(throwable))));
    }

    private void loadNext() {
        setLoadingNow(true);

        final String startFrom = this.nextFrom;
        load(startFrom);
    }

    private void onRequestError(Throwable throwable){
        throwable.printStackTrace();
        setLoadingNow(false);
    }

    private void onDataReceived(String startFrom, String nextFrom, List<NewsfeedComment> comments){
        setLoadingNow(false);

        boolean atLast = isEmpty(startFrom);
        this.nextFrom = nextFrom;

        if(atLast){
            this.data.clear();
            this.data.addAll(comments);
            callView(INewsfeedCommentsView::notifyDataSetChanged);
        } else {
            int startCount = this.data.size();
            this.data.addAll(comments);
            callView(view -> view.notifyDataAdded(startCount, comments.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull INewsfeedCommentsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private boolean canLoadMore(){
        return nonEmpty(nextFrom) && !loadingNow;
    }

    public void fireScrollToEnd() {
        if(canLoadMore()){
            loadNext();
        }
    }

    public void fireRefresh() {
        if(loadingNow){
            return;
        }

        loadAtLast();
    }

    public void fireCommentBodyClick(NewsfeedComment newsfeedComment) {
        Comment comment = newsfeedComment.getComment();
        AssertUtils.requireNonNull(comment);
        
        getView().openComments(getAccountId(), comment.getCommented(), null);
    }
}
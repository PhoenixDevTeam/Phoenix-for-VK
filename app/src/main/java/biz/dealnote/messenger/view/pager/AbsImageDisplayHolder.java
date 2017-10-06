package biz.dealnote.messenger.view.pager;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;

import biz.dealnote.messenger.api.PicassoInstance;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
public abstract class AbsImageDisplayHolder extends AbsPagerHolder implements Callback {

    protected PhotoView mPhotoView;
    private ProgressBar mProgressBar;

    private boolean mLoadingNow;
    private WeakPicassoLoadCallback mPicassoLoadCallback;

    private Context mContext;

    public AbsImageDisplayHolder(int adapterPosition, @NonNull View itemView) {
        super(adapterPosition, itemView);
        mContext = itemView.getContext().getApplicationContext();

        mPhotoView = (PhotoView) itemView.findViewById(idOfImageView());
        mPhotoView.setMaximumScale(5f);

        mProgressBar = (ProgressBar) itemView.findViewById(idOfProgressBar());
        mPicassoLoadCallback = new WeakPicassoLoadCallback(this);
    }

    protected void loadImage(@NonNull String url) {
        mLoadingNow = true;

        resolveProgressVisibility();

        PicassoInstance.with()
                .load(url)
                .into(mPhotoView, mPicassoLoadCallback);
    }

    private void resolveProgressVisibility() {
        mProgressBar.setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PicassoInstance.with()
                .cancelRequest(mPhotoView);

        //must be cleanup. Seems to be automatically handled inside
        mLoadingNow = false;
    }

    @IdRes
    protected abstract int idOfImageView();

    @IdRes
    protected abstract int idOfProgressBar();

    @Override
    public void onSuccess() {
        if(isDestroyed()) {
            return;
        }

        mLoadingNow = false;
        resolveProgressVisibility();
    }

    @Override
    public void onError(Exception e) {
        if(isDestroyed()) {
            return;
        }

        mLoadingNow = false;
        resolveProgressVisibility();
    }
}

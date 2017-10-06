package biz.dealnote.messenger.mvp.presenter.photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.service.RequestFactory;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class SimplePhotoPresenter extends PhotoPagerPresenter {

    private boolean mDataRefreshSuccessfull;

    public SimplePhotoPresenter(@NonNull ArrayList<Photo> photos, int index, boolean needToRefreshData,
                                int accountId, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, savedInstanceState);

        if(savedInstanceState == null){
            changePageTo(index);
        } else {
            mDataRefreshSuccessfull = savedInstanceState.getBoolean(SAVE_DATA_REFRESH_RESULT);
        }

        if(needToRefreshData && !mDataRefreshSuccessfull){
            refreshData();
        }
    }

    private void refreshData(){
        ArrayList<AccessIdPair> ids = new ArrayList<>(getData().size());
        for(Photo photo : getData()){
            ids.add(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey()));
        }

        Request request = RequestFactory.getPhotosByIdRequest(ids, true);
        executeRequest(request);
    }

    @Override
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {
        super.onRequestFinished(request, resultData);
        if(request.getRequestType() == RequestFactory.REQUEST_PHOTOS_BY_ID){
            ArrayList<Photo> result = resultData.getParcelableArrayList(Extra.PHOTOS);
            AssertUtils.requireNonNull(result);
            mDataRefreshSuccessfull = true;

            onPhotoListRefresh(result);
        }
    }

    private static final String TAG = SimplePhotoPresenter.class.getSimpleName();

    @Override
    protected String tag() {
        return TAG;
    }

    private void onPhotoListRefresh(@NonNull ArrayList<Photo> photos){
        List<Photo> originalData = super.getData();

        for(Photo photo : photos){
            //замена старых обьектов новыми
            for(int i = 0; i < originalData.size(); i++){
                Photo orig = originalData.get(i);

                if(orig.getId() == photo.getId() && orig.getOwnerId() == photo.getOwnerId()){
                    originalData.set(i, photo);

                    // если у фото до этого не было ссылок на файлы
                    if(isGuiReady() && (isNull(orig.getSizes()) || orig.getSizes().isEmpty())){
                        Logger.d(TAG, "Rebind holder at " + i);

                        getView().rebindPhotoAt(i);
                    }
                    break;
                }
            }
        }

        super.refreshInfoViews();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBoolean(SAVE_DATA_REFRESH_RESULT, mDataRefreshSuccessfull);
    }

    private static final String SAVE_DATA_REFRESH_RESULT = "save_data_refresh_result";
}

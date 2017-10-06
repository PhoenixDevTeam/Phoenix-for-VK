package biz.dealnote.messenger.mvp.presenter.photo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.Collections;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.service.RequestFactory;

/**
 * Created by admin on 28.09.2016.
 * phoenix
 */
public class FavePhotoPagerPresenter extends PhotoPagerPresenter {

    private static final String SAVE_UPDATED = "save_updated";

    private boolean[] mUpdated;

    public FavePhotoPagerPresenter(@NonNull ArrayList<Photo> photos, int index, int accountId, @Nullable Bundle savedInstanceState) {
        super(photos, accountId, savedInstanceState);
        if(savedInstanceState == null){
            mUpdated = new boolean[photos.size()];
            setCurrentIndex(index);
            refresh(index);
        } else {
            mUpdated = savedInstanceState.getBooleanArray(SAVE_UPDATED);
        }
    }

    private void refresh(int index){
        if(mUpdated[index]){
            return;
        }

        Photo photo = getData().get(index);
        ArrayList<AccessIdPair> forUpdate = new ArrayList<>(Collections.singletonList(new AccessIdPair(photo.getId(), photo.getOwnerId(), photo.getAccessKey())));
        Request request = RequestFactory.getPhotosByIdRequest(forUpdate, false);

        request.put(Extra.INDEX, index); // optimize
        executeRequest(request);
    }

    @Override
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {
        super.onRequestFinished(request, resultData);

        if(request.getRequestType() == RequestFactory.REQUEST_PHOTOS_BY_ID){
            int index = request.getInt(Extra.INDEX);

            ArrayList<Photo> result = resultData.getParcelableArrayList(Extra.PHOTOS);
            if(result != null && result.size() == 1){
                Photo p = result.get(0);

                getData().set(index, p);

                mUpdated[index] = true;

                if(getCurrentIndex() == index){
                    refreshInfoViews();
                }
            }
        }
    }

    @Override
    protected void afterPageChangedFromUi(int oldPage, int newPage) {
        super.afterPageChangedFromUi(oldPage, newPage);
        refresh(newPage);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putBooleanArray(SAVE_UPDATED, mUpdated);
    }
}

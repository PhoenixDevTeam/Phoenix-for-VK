package biz.dealnote.messenger.fragment.conversation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.fave.FavePhotosAdapter;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.mvp.presenter.history.ChatAttachmentPhotoPresenter;
import biz.dealnote.messenger.mvp.view.IChatAttachmentPhotosView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

public class ConversationPhotosFragment extends AbsChatAttachmentsFragment<Photo, ChatAttachmentPhotoPresenter,
        IChatAttachmentPhotosView> implements FavePhotosAdapter.PhotoSelectionListener, IChatAttachmentPhotosView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getContext().getResources().getInteger(R.integer.photos_column_count);
        return new GridLayoutManager(getActivity(), columns);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        FavePhotosAdapter apiPhotoFavePhotosAdapter = new FavePhotosAdapter(getActivity(), Collections.emptyList());
        apiPhotoFavePhotosAdapter.setPhotoSelectionListener(this);
        return apiPhotoFavePhotosAdapter;
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        getPresenter().firePhotoClick(position, photo);
    }

    @Override
    public IPresenterFactory<ChatAttachmentPhotoPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = getArguments().getInt(Extra.PEER_ID);
            return new ChatAttachmentPhotoPresenter(peerId, accountId, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return ConversationPhotosFragment.class.getSimpleName();
    }

    @Override
    public void displayAttachments(List<Photo> data) {
        FavePhotosAdapter adapter = (FavePhotosAdapter) getAdapter();
        adapter.setData(data);
    }

    @Override
    public void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index) {
        PlaceFactory.getTmpSourceGalleryPlace(accountId, source, index).tryOpenWith(getActivity());
    }
}
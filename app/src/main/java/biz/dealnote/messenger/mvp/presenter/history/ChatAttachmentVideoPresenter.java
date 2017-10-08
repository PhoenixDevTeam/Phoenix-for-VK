package biz.dealnote.messenger.mvp.presenter.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiVideo;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.IChatAttachmentVideoView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentVideoPresenter extends BaseChatAttachmentsPresenter<Video, IChatAttachmentVideoView> {

    public ChatAttachmentVideoPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Video>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "video", nextFrom, 50, null)
                .map(response -> {
                    List<Video> videos = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiVideo) {
                                VKApiVideo dto = (VKApiVideo) one.entry.attachment;
                                videos.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.create(response.next_from, videos);
                });
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.videos_count, safeCountOf(data)));
        }
    }

    @Override
    protected String tag() {
        return ChatAttachmentVideoPresenter.class.getSimpleName();
    }
}

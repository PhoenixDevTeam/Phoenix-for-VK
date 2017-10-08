package biz.dealnote.messenger.mvp.presenter.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.mvp.view.IChatAttachmentAudiosView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentAudioPresenter extends BaseChatAttachmentsPresenter<Audio, IChatAttachmentAudiosView> {

    public ChatAttachmentAudioPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Audio>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "audio", nextFrom, 50, null)
                .map(response -> {
                    List<Audio> audios = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiAudio) {
                                VKApiAudio dto = (VKApiAudio) one.entry.attachment;
                                audios.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.create(response.next_from, audios);
                });
    }

    @SuppressWarnings("unused")
    public void fireAudioPlayClick(int position, Audio audio){
        super.fireAudioPlayClick(position, new ArrayList<>(data));
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.audios_count, safeCountOf(data)));
        }
    }

    @Override
    protected String tag() {
        return ChatAttachmentAudioPresenter.class.getSimpleName();
    }
}

package biz.dealnote.messenger.mvp.presenter.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.IChatAttachmentDocsView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentDocsPresenter extends BaseChatAttachmentsPresenter<Document, IChatAttachmentDocsView> {

    public ChatAttachmentDocsPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Document>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, VKApiAttachment.TYPE_DOC, nextFrom, 50, null)
                .map(response -> {
                    List<Document> docs = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VkApiDoc) {
                                VkApiDoc dto = (VkApiDoc) one.entry.attachment;
                                docs.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.create(response.next_from, docs);
                });
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.documents_count, safeCountOf(data)));
        }
    }

    @Override
    protected String tag() {
        return ChatAttachmentDocsPresenter.class.getSimpleName();
    }
}

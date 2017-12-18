package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.view.IFwdsView;

/**
 * Created by r.kolbasa on 18.12.2017.
 * Phoenix-for-VK
 */
public class FwdsPresenter extends AbsMessageListPresenter<IFwdsView> {

    public FwdsPresenter(int accountId, List<Message> messages, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        getData().addAll(messages);
    }

    @Override
    protected String tag() {
        return FwdsPresenter.class.getSimpleName();
    }
}
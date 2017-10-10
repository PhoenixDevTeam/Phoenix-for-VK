package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.view.search.IMessagesSearchView;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.trimmedNonEmpty;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public class MessagesSearchPresenter extends AbsSearchPresenter<IMessagesSearchView, MessageSeachCriteria, Message, IntNextFrom> {

    private static final String TAG = MessagesSearchPresenter.class.getSimpleName();

    private final IMessagesInteractor messagesInteractor;

    public MessagesSearchPresenter(int accountId, @Nullable MessageSeachCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.messagesInteractor = InteractorFactory.createMessagesInteractor();

        if(canSearch(getCriteria())){
            doSearch();
        }
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    private static final int COUNT = 50;

    @Override
    Single<Pair<List<Message>, IntNextFrom>> doSearch(int accountId, MessageSeachCriteria criteria, IntNextFrom nextFrom) {
        final int offset = Objects.isNull(nextFrom) ? 0 : nextFrom.getOffset();
        return messagesInteractor
                .searchMessages(accountId, criteria.getPeerId(), COUNT, offset, criteria.getQuery())
                .map(messages -> Pair.create(messages, new IntNextFrom(offset + COUNT)));
    }

    @Override
    MessageSeachCriteria instantiateEmptyCriteria() {
        return new MessageSeachCriteria("");
    }

    @Override
    boolean canSearch(MessageSeachCriteria criteria) {
        return trimmedNonEmpty(criteria.getQuery());
    }

    @Override
    protected String tag() {
        return TAG;
    }

    public void fireMessageClick(Message message) {
        getView().goToMessagesLookup(getAccountId(), message.getPeerId(), message.getId());
    }
}

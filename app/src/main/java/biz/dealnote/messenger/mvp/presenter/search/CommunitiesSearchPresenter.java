package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.ICommunitiesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.criteria.GroupSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.fragment.search.options.SpinnerOption;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.mvp.view.search.ICommunitiesSearchView;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public class CommunitiesSearchPresenter extends AbsSearchPresenter<ICommunitiesSearchView,
        GroupSearchCriteria, Community, IntNextFrom> {

    private final ICommunitiesInteractor communitiesInteractor;

    public CommunitiesSearchPresenter(int accountId, @Nullable GroupSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        this.communitiesInteractor = InteractorFactory.createCommunitiesInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Community>, IntNextFrom>> doSearch(int accountId, GroupSearchCriteria criteria, IntNextFrom startFrom) {
        final String type = extractTypeFromCriteria(criteria);

        final Integer countryId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_COUNTRY);
        final Integer cityId = criteria.extractDatabaseEntryValueId(GroupSearchCriteria.KEY_CITY);
        final Boolean future = criteria.extractBoleanValueFromOption(GroupSearchCriteria.KEY_FUTURE_ONLY);

        final SpinnerOption sortOption = criteria.findOptionByKey(GroupSearchCriteria.KEY_SORT);
        final Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        final int offset = startFrom.getOffset();
        final IntNextFrom nextFrom = new IntNextFrom(offset + 50);

        return communitiesInteractor.search(accountId, criteria.getQuery(), type, countryId, cityId, future, sort, 50, offset)
                .map(communities -> Pair.create(communities, nextFrom));
    }

    private static String extractTypeFromCriteria(GroupSearchCriteria criteria) {
        SpinnerOption option = criteria.findOptionByKey(GroupSearchCriteria.KEY_TYPE);
        if (option != null && option.value != null) {
            switch (option.value.id) {
                case GroupSearchCriteria.TYPE_PAGE:
                    return "page";
                case GroupSearchCriteria.TYPE_GROUP:
                    return "group";
                case GroupSearchCriteria.TYPE_EVENT:
                    return "event";
            }
        }

        return null;
    }

    @Override
    GroupSearchCriteria instantiateEmptyCriteria() {
        return new GroupSearchCriteria("");
    }

    @Override
    boolean canSearch(GroupSearchCriteria criteria) {
        return nonEmpty(criteria.getQuery());
    }

    @Override
    protected String tag() {
        return CommunitiesSearchPresenter.class.getSimpleName();
    }

    public void fireCommunityClick(Community community) {
        getView().openCommunityWall(getAccountId(), community);
    }
}
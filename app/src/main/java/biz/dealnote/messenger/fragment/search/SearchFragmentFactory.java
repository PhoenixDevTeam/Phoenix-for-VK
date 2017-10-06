package biz.dealnote.messenger.fragment.search;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import biz.dealnote.messenger.fragment.search.criteria.BaseSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.DialogsSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.GroupSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.VideoSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.WallSearchCriteria;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public class SearchFragmentFactory {

    public static Fragment create(@SearchContentType int type, int accountId, @Nullable BaseSearchCriteria criteria) {
        switch (type) {
            case SearchContentType.PEOPLE:
                return PeopleSearchFragment.newInstance(accountId,
                        criteria instanceof PeopleSearchCriteria ? (PeopleSearchCriteria) criteria : null);

            case SearchContentType.COMMUNITIES:
                return GroupsSearchFragment.newInstance(accountId,
                        criteria instanceof GroupSearchCriteria ? (GroupSearchCriteria) criteria : null);

            case SearchContentType.VIDEOS:
                return VideoSearchFragment.newInstance(accountId,
                        criteria instanceof VideoSearchCriteria ? (VideoSearchCriteria) criteria : null);

            case SearchContentType.DOCUMENTS:
                return DocsSearchFragment.newInstance(accountId,
                        criteria instanceof DocumentSearchCriteria ? (DocumentSearchCriteria) criteria : null);

            case SearchContentType.NEWS:
                return NewsFeedSearchFragment.newInstance(accountId,
                        criteria instanceof NewsFeedCriteria ? (NewsFeedCriteria) criteria : null);

            case SearchContentType.MESSAGES:
                return MessagesSearchFragment.newInstance(accountId,
                        criteria instanceof MessageSeachCriteria ? (MessageSeachCriteria) criteria : null);

            case SearchContentType.WALL:
                return WallSearchFragment.newInstance(accountId,
                        criteria instanceof WallSearchCriteria ? (WallSearchCriteria) criteria : null);

                case SearchContentType.DIALOGS:
                    return DialogsSearchFragment.newInstance(accountId,
                            criteria instanceof DialogsSearchCriteria ? (DialogsSearchCriteria) criteria : null);

            default:
                throw new UnsupportedOperationException();
        }
    }

}

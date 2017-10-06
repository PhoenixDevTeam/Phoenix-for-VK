package biz.dealnote.messenger.mvp.view.search;

import biz.dealnote.messenger.model.Community;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public interface ICommunitiesSearchView extends IBaseSearchView<Community> {
    void openCommunityWall(int accountId, Community community);
}
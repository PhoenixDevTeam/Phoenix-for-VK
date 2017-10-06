package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.search.criteria.GroupSearchCriteria;
import biz.dealnote.messenger.service.operations.groups.JoinGroupOperation;

public class GroupsRequestFactory {

    public static final int REQUEST_JOIN = 3000;
    public static final int REQUEST_LEAVE = 3001;

    public static Request getJoinGroupRequest(int gid, Integer notSure) {
        Request request = new Request(REQUEST_JOIN);
        request.put(Extra.GROUP_ID, gid);
        if(notSure != null){
            request.put(JoinGroupOperation.EXTRA_NOT_SURE, notSure);
        }
        return request;
    }

    public static Request getLeaveGroupRequest(int gid) {
        Request request = new Request(REQUEST_LEAVE);
        request.put(Extra.GROUP_ID, gid);
        return request;
    }
}
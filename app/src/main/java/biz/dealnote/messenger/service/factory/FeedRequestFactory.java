package biz.dealnote.messenger.service.factory;

import com.foxykeep.datadroid.requestmanager.Request;

public class FeedRequestFactory {

    public static final int REQUEST_GET_LISTS = 16002;

    public static Request getFeedListsRequest(){
        return new Request(REQUEST_GET_LISTS);
    }
}

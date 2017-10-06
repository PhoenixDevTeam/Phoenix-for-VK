package biz.dealnote.messenger.api.interfaces;

import android.support.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.CustomCommentsResponse;
import io.reactivex.Single;

/**
 * Created by admin on 03.01.2017.
 * phoenix
 */
public interface ICommentsApi {

    // {"response":{"main":false,"first_id":null,"last_id":null,"admin_level":0},"execute_errors":[{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"execute.getComments","error_code":18,"error_msg":"User was deleted or banned"}]}
    @CheckResult
    Single<CustomCommentsResponse> get(String sourceType, int ownerId, int sourceId, Integer offset, Integer count,
                                       String sort, Integer startCommentId, String accessKey, String fields);
}

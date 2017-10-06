package biz.dealnote.messenger.api.interfaces;

import java.util.Map;

import biz.dealnote.messenger.util.Optional;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 31.07.2017.
 * phoenix
 */
public interface IOtherApi {
    Single<Optional<String>> rawRequest(String method, Map<String, String> postParams);
}
package biz.dealnote.messenger.interactor;

import java.util.List;

import biz.dealnote.messenger.model.Document;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 17.05.2017.
 * phoenix
 */
public interface IDocsInteractor {
    Single<List<Document>> request(int accountId, int ownerId, int filter);
    Single<List<Document>> getCacheData(int accountId, int ownerId, int filter);
}

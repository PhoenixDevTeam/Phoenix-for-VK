package biz.dealnote.messenger.interactor.impl;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.interfaces.IDocsRepository;
import biz.dealnote.messenger.interactor.IDocsInteractor;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.criteria.DocsCriteria;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 17.05.2017.
 * phoenix
 */
public class DocsInteractor implements IDocsInteractor {

    private final INetworker networker;
    private final IDocsRepository cache;

    public DocsInteractor(INetworker networker, IDocsRepository cache) {
        this.networker = networker;
        this.cache = cache;
    }

    @Override
    public Single<List<Document>> request(int accountId, int ownerId, int filter) {
        return networker.vkDefault(accountId)
                .docs()
                .get(ownerId, null, null, filter)
                .flatMap(dtos -> cache.store(accountId, ownerId, dtos.getItems(), true)
                        .toSingle(DatabaseIdRange.create(0, 0)) // because range not used
                        .map(ignored -> {
                            List<Document> documents = new ArrayList<>(dtos.getItems().size());
                            for (VkApiDoc dto : dtos.getItems()) {
                                documents.add(Dto2Model.transform(dto));
                            }
                            return documents;
                        }));
    }

    @Override
    public Single<List<Document>> getCacheData(int accountId, int ownerId, int filter) {
        return cache.get(new DocsCriteria(accountId, ownerId).setFilter(filter));
    }
}

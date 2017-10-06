package biz.dealnote.messenger.interactor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.db.DatabaseIdRange;
import biz.dealnote.messenger.db.interfaces.IDocsRepository;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.fragment.search.criteria.DocumentSearchCriteria;
import biz.dealnote.messenger.interactor.IDocsInteractor;
import biz.dealnote.messenger.interactor.mappers.Dto2Model;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.criteria.DocsCriteria;
import biz.dealnote.messenger.util.Utils;
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

    @Override
    public Single<Integer> add(int accountId, int docId, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .docs()
                .add(ownerId, docId, accessKey);
    }

    @Override
    public Single<Document> findById(int accountId, int ownerId, int docId) {
        return networker.vkDefault(accountId)
                .docs()
                .getById(Collections.singletonList(new IdPair(docId, ownerId)))
                .map(dtos -> {
                    if(dtos.isEmpty()){
                        throw new NotFoundException();
                    }

                    return Dto2Model.transform(dtos.get(0));
                });
    }

    @Override
    public Single<List<Document>> search(int accountId, DocumentSearchCriteria criteria, int count, int offset) {
        return networker.vkDefault(accountId)
                .docs()
                .search(criteria.getQuery(), count, offset)
                .map(items -> {
                    List<VkApiDoc> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<Document> documents = new ArrayList<>();

                    for(VkApiDoc dto : dtos){
                        documents.add(Dto2Model.transform(dto));
                    }

                    return documents;
                });
    }
}
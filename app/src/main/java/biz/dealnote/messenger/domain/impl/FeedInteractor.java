package biz.dealnote.messenger.domain.impl;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VKApiNews;
import biz.dealnote.messenger.api.model.VKApiPost;
import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.model.entity.FeedListEntity;
import biz.dealnote.messenger.db.model.entity.NewsEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.domain.IFeedInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.fragment.search.criteria.NewsFeedCriteria;
import biz.dealnote.messenger.model.FeedList;
import biz.dealnote.messenger.model.FeedSourceCriteria;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.criteria.FeedCriteria;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;

/**
 * Created by Ruslan Kolbasa on 06.09.2017.
 * phoenix
 */
public class FeedInteractor implements IFeedInteractor {

    private final INetworker networker;
    private final IStores stores;
    private final IOwnersInteractor ownersInteractor;
    private final ISettings.IOtherSettings otherSettings;

    public FeedInteractor(INetworker networker, IStores stores, ISettings.IOtherSettings otherSettings) {
        this.networker = networker;
        this.stores = stores;
        this.otherSettings = otherSettings;
        this.ownersInteractor = new OwnersInteractor(networker, stores.owners());
    }

    @Override
    public Single<Pair<List<News>, String>> getActualFeed(int accountId, int count, String startFrom, String filters, Integer maxPhotos, String sourceIds) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .get(filters, null, null, null, maxPhotos, sourceIds, startFrom, count, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    final String nextFrom = response.nextFrom;

                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);
                    List<VKApiNews> feed = listEmptyIfNull(response.items);
                    List<NewsEntity> dbos = new ArrayList<>(feed.size());

                    VKOwnIds ownIds = new VKOwnIds();

                    for(VKApiNews news : feed){
                        if(!hasNewsSupport(news)) continue;

                        dbos.add(Dto2Entity.buildNewsDbo(news));
                        ownIds.appendNews(news);
                    }

                    final OwnerEntities ownerEntities = Dto2Entity.buildOwnerDbos(response.profiles, response.groups);

                    return stores.feed()
                            .store(accountId, dbos, ownerEntities, Utils.isEmpty(startFrom))
                            .flatMap(ints -> {
                                otherSettings.storeFeedNextFrom(accountId, nextFrom);
                                otherSettings.setFeedSourceIds(accountId, sourceIds);

                                return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY, owners)
                                        .map(owners1 -> {
                                            List<News> news = new ArrayList<>(feed.size());

                                            for(VKApiNews dto : feed){
                                                if(!hasNewsSupport(dto)) continue;

                                                news.add(Dto2Model.buildNews(dto, owners1));
                                            }

                                            return Pair.create(news, nextFrom);
                                        });
                            });
                });
    }

    private static boolean hasNewsSupport(VKApiNews news){
        return "post".equals(news.type);
    }

    @Override
    public Single<Pair<List<Post>, String>> search(int accountId, NewsFeedCriteria criteria, int count, String startFrom) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .search(criteria.getQuery(), true, count, null, null, null, null, startFrom, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiPost> dtos = listEmptyIfNull(response.items);
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ownIds = new VKOwnIds();
                    for(VKApiPost post : dtos){
                        ownIds.append(post);
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .map(ownersBundle -> {
                                List<Post> posts = Dto2Model.transformPosts(dtos, ownersBundle);
                                return Pair.create(posts, response.nextFrom);
                            });
                });
    }

    @Override
    public Single<List<FeedList>> getCachedFeedLists(int accountId) {
        FeedSourceCriteria criteria = new FeedSourceCriteria(accountId);
        return stores.feed()
                .getAllLists(criteria)
                .map(entities -> {
                    List<FeedList> lists = new ArrayList<>(entities.size());
                    for(FeedListEntity entity : entities){
                        lists.add(createFeedListFromEntity(entity));
                    }
                    return lists;
                });
    }

    private static FeedList createFeedListFromEntity(FeedListEntity entity){
        return new FeedList(entity.getId(), entity.getTitle());
    }

    @Override
    public Single<List<FeedList>> getActualFeedLists(int accountId) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .getLists(null)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<FeedListEntity> entities = new ArrayList<>(dtos.size());
                    List<FeedList> lists = new ArrayList<>();

                    for(VkApiFeedList dto : dtos){
                        FeedListEntity entity = new FeedListEntity(dto.id)
                                .setTitle(dto.title)
                                .setNoReposts(dto.no_reposts)
                                .setSourceIds(dto.source_ids);
                        entities.add(entity);
                        lists.add(createFeedListFromEntity(entity));
                    }

                    return stores.feed()
                            .storeLists(accountId, entities)
                            .andThen(Single.just(lists));
                });
    }

    @Override
    public Single<List<News>> getCachedFeed(int accountId) {
        FeedCriteria criteria = new FeedCriteria(accountId);

        return stores.feed()
                .findByCriteria(criteria)
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();
                    for(NewsEntity dbo : dbos){
                        Entity2Model.fillOwnerIds(ownIds, dbo);
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY)
                            .map(owners -> {
                                List<News> news = new ArrayList<>(dbos.size());
                                for(NewsEntity dbo : dbos){
                                    news.add(Entity2Model.buildNewsFromDbo(dbo, owners));
                                }

                                return news;
                            });
                });
    }
}
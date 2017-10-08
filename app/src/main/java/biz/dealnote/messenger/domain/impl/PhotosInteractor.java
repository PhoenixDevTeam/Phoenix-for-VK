package biz.dealnote.messenger.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VKApiPhotoAlbum;
import biz.dealnote.messenger.db.column.PhotosColumns;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.model.PhotoPatch;
import biz.dealnote.messenger.db.model.entity.PhotoAlbumEntity;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.domain.IPhotosInteractor;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.criteria.PhotoAlbumsCriteria;
import biz.dealnote.messenger.model.criteria.PhotoCriteria;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 13.07.2017.
 * phoenix
 */
public class PhotosInteractor implements IPhotosInteractor {

    private final INetworker networker;
    private final IStores cache;

    public PhotosInteractor(INetworker networker, IStores cache) {
        this.networker = networker;
        this.cache = cache;
    }

    @Override
    public Single<List<Photo>> get(int accountId, int ownerId, int albumId, int count, int offset, boolean rev) {
        return networker.vkDefault(accountId)
                .photos()
                .get(ownerId, String.valueOf(albumId), null, rev, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());
                    List<PhotoEntity> dbos = new ArrayList<>(dtos.size());

                    for(VKApiPhoto dto : dtos){
                        photos.add(Dto2Model.transform(dto));
                        dbos.add(Dto2Entity.buildPhotoDbo(dto));
                    }

                    return cache.photos()
                            .insertPhotosRx(accountId, ownerId, albumId, dbos, offset == 0)
                            .andThen(Single.just(photos));
                });
    }

    @Override
    public Single<List<Photo>> getAllCachedData(int accountId, int ownerId, int albumId) {
        PhotoCriteria criteria = new PhotoCriteria(accountId).setAlbumId(albumId).setOwnerId(ownerId);

        if (albumId == -15) {
            criteria.setOrderBy(PhotosColumns._ID);
        }

        return cache.photos()
                .findPhotosByCriteriaRx(criteria)
                .map(dbos -> {
                    List<Photo> photos = new ArrayList<>(dbos.size());
                    for(PhotoEntity dbo : dbos){
                        photos.add(Entity2Model.buildPhotoFromDbo(dbo));
                    }
                    return photos;
                });
    }

    @Override
    public Single<PhotoAlbum> getAlbumById(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, Collections.singletonList(albumId), null, null, true, true)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .map(dtos -> {
                    if(dtos.isEmpty()){
                        throw new NotFoundException();
                    }

                    return Dto2Model.transform(dtos.get(0));
                });
    }

    @Override
    public Single<List<PhotoAlbum>> getCachedAlbums(int accountId, int ownerId) {
        PhotoAlbumsCriteria criteria = new PhotoAlbumsCriteria(accountId, ownerId);

        return cache.photoAlbums()
                .findAlbumsByCriteria(criteria)
                .map(dbos -> {
                    List<PhotoAlbum> albums = new ArrayList<>(dbos.size());
                    for(PhotoAlbumEntity dbo : dbos){
                        albums.add(Entity2Model.buildPhotoAlbumFromDbo(dbo));
                    }
                    return albums;
                });
    }

    @Override
    public Single<List<PhotoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, null, offset, count, true, true)
                .flatMap(items -> {
                    List<VKApiPhotoAlbum> dtos = Utils.listEmptyIfNull(items.getItems());

                    List<PhotoAlbumEntity> dbos = new ArrayList<>(dtos.size());
                    List<PhotoAlbum> albums = new ArrayList<>(dbos.size());

                    for(VKApiPhotoAlbum dto : dtos){
                        dbos.add(Dto2Entity.buildPhotoAlbumDbo(dto));
                        albums.add(Dto2Model.transform(dto));
                    }

                    return cache.photoAlbums()
                            .store(accountId, ownerId, dbos, offset == 0)
                            .andThen(Single.just(albums));
                });
    }

    @Override
    public Single<Integer> like(int accountId, int ownerId, int photoId, boolean add, String accessKey) {
        Single<Integer> single;

        if(add){
            single = networker.vkDefault(accountId)
                    .likes()
                    .add("photo", ownerId, photoId, accessKey);
        } else {
            single = networker.vkDefault(accountId)
                    .likes()
                    .delete("photo", ownerId, photoId);
        }

        return single.flatMap(count -> {
            final PhotoPatch patch = new PhotoPatch().setLike(new PhotoPatch.Like(count, add));
            return cache.photos()
                    .applyPatch(accountId, ownerId, photoId, patch)
                    .andThen(Single.just(count));
        });
    }

    @Override
    public Single<Integer> copy(int accountId, int ownerId, int photoId, String accessKey) {
        return networker.vkDefault(accountId)
                .photos()
                .copy(ownerId, photoId, accessKey);
    }

    @Override
    public Completable removedAlbum(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .deleteAlbum(albumId, ownerId < 0 ? Math.abs(ownerId) : null)
                .flatMapCompletable(ignored -> cache.photoAlbums()
                        .removeAlbumById(accountId, ownerId, albumId));
    }

    @Override
    public Completable deletePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .delete(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(true));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Completable restorePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .restore(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(false));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Single<List<Photo>> getPhotosByIds(int accountId, Collection<AccessIdPair> ids) {
        List<biz.dealnote.messenger.api.model.AccessIdPair> dtoPairs = new ArrayList<>(ids.size());

        for(AccessIdPair pair : ids){
            dtoPairs.add(new biz.dealnote.messenger.api.model.AccessIdPair(pair.getId(),
                    pair.getOwnerId(), pair.getAccessKey()));
        }

        return networker.vkDefault(accountId)
                .photos()
                .getById(dtoPairs)
                .map(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());
                    for(VKApiPhoto dto : dtos){
                        photos.add(Dto2Model.transform(dto));
                    }
                    return photos;
                });
    }
}
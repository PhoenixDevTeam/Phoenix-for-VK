package biz.dealnote.messenger.db.impl;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.db.interfaces.IAttachmentsStore;
import biz.dealnote.messenger.db.interfaces.ICommentsStore;
import biz.dealnote.messenger.db.interfaces.IDatabaseStore;
import biz.dealnote.messenger.db.interfaces.IDialogsStore;
import biz.dealnote.messenger.db.interfaces.IDocsStore;
import biz.dealnote.messenger.db.interfaces.IFaveStore;
import biz.dealnote.messenger.db.interfaces.IFeedStore;
import biz.dealnote.messenger.db.interfaces.IFeedbackStore;
import biz.dealnote.messenger.db.interfaces.IKeysStore;
import biz.dealnote.messenger.db.interfaces.ILocalMediaStore;
import biz.dealnote.messenger.db.interfaces.IMessagesStore;
import biz.dealnote.messenger.db.interfaces.IOwnersStore;
import biz.dealnote.messenger.db.interfaces.IPhotoAlbumsStore;
import biz.dealnote.messenger.db.interfaces.IPhotosStore;
import biz.dealnote.messenger.db.interfaces.IRelativeshipStore;
import biz.dealnote.messenger.db.interfaces.IStickersStore;
import biz.dealnote.messenger.db.interfaces.IStores;
import biz.dealnote.messenger.db.interfaces.ITempDataStore;
import biz.dealnote.messenger.db.interfaces.ITopicsStore;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.db.interfaces.IVideoAlbumsStore;
import biz.dealnote.messenger.db.interfaces.IVideoStore;
import biz.dealnote.messenger.db.interfaces.IWallStore;

import static biz.dealnote.messenger.util.Objects.isNull;

public class AppStores extends ContextWrapper implements IStores {

    private static AppStores sStoresInstance;

    private AppStores(Context base) {
        super(base);
    }

    public static AppStores getInstance(Context baseContext){
        if(isNull(sStoresInstance)){
            synchronized (AppStores.class){
                if(isNull(sStoresInstance)){
                    sStoresInstance = new AppStores(baseContext.getApplicationContext());
                }
            }
        }

        return sStoresInstance;
    }

    private IOwnersStore ownersStore;
    private IFeedStore feedStore;
    private IRelativeshipStore relativeshipStore;
    private IPhotosStore photosStore;
    private IFaveStore faveStore;
    private IWallStore wallStore;
    private IMessagesStore messagesStore;
    private IDialogsStore dialogsStore;
    private IFeedbackStore feedbackStore;
    private ILocalMediaStore localMediaStore;
    private KeysPersistStore keysPersistStore;
    private KeysRamStore keysRamStore;
    private IAttachmentsStore attachmentsStore;
    private volatile IVideoStore videoStore;
    private volatile IVideoAlbumsStore videoAlbumsStore;
    private volatile ICommentsStore commentsStore;
    private volatile IPhotoAlbumsStore photoAlbumsStore;
    private volatile ITopicsStore topicsStore;
    private volatile IDocsStore docsStore;
    private volatile IStickersStore stickersStore;
    private volatile IDatabaseStore databaseStore;

    @Override
    public ICommentsStore comments() {
        if(isNull(commentsStore)){
            synchronized (this){
                if(isNull(commentsStore)){
                    commentsStore = new CommentsStore(this);
                }
            }
        }

        return commentsStore;
    }

    @Override
    public IPhotoAlbumsStore photoAlbums() {
        if(isNull(photoAlbumsStore)){
            synchronized (this){
                if(isNull(photoAlbumsStore)){
                    photoAlbumsStore = new PhotoAlbumsStore(this);
                }
            }
        }

        return photoAlbumsStore;
    }

    @Override
    public ITopicsStore topics() {
        if(isNull(topicsStore)){
            synchronized (this){
                if(isNull(topicsStore)){
                    topicsStore = new TopicsStore(this);
                }
            }
        }

        return topicsStore;
    }

    @Override
    public IDocsStore docs() {
        if(isNull(docsStore)){
            synchronized (this){
                if(isNull(docsStore)){
                    docsStore = new DocsStore(this);
                }
            }
        }

        return docsStore;
    }

    @Override
    public IStickersStore stickers() {
        if(isNull(stickersStore)){
            synchronized (this){
                if(isNull(stickersStore)){
                    stickersStore = new StickersStore(this);
                }
            }
        }

        return stickersStore;
    }

    private volatile IUploadQueueStore uploadQueueRepository;

    @Override
    public IUploadQueueStore uploads() {
        if(isNull(uploadQueueRepository)){
            synchronized (this){
                if(isNull(uploadQueueRepository)){
                    uploadQueueRepository = new UploadQueueStore(this);
                }
            }
        }

        return uploadQueueRepository;
    }

    @Override
    public IDatabaseStore database() {
        if(isNull(databaseStore)){
            synchronized (this){
                if(isNull(databaseStore)){
                    databaseStore = new DatabaseStore(this);
                }
            }
        }
        return databaseStore;
    }

    private final ITempDataStore tempDataStore = new TempDataStore(this);

    @Override
    public ITempDataStore tempStore() {
        return tempDataStore;
    }

    public IVideoAlbumsStore videoAlbums(){
        if(isNull(videoAlbumsStore)){
            synchronized (this){
                if(isNull(videoAlbumsStore)){
                    videoAlbumsStore = new VideoAlbumsStore(this);
                }
            }
        }

        return videoAlbumsStore;
    }

    public IVideoStore videos(){
        if(isNull(videoStore)){
            synchronized (this){
                if(isNull(videoStore)){
                    videoStore = new VideoStore(this);
                }
            }
        }

        return videoStore;
    }

    @NonNull
    public synchronized IAttachmentsStore attachments(){
        if(attachmentsStore == null){
            attachmentsStore = new AttachmentsStore(this);
        }

        return attachmentsStore;
    }

    @NonNull
    public synchronized IKeysStore keys(@KeyLocationPolicy int policy){
        switch (policy){
            case KeyLocationPolicy.PERSIST:
                if(isNull(keysPersistStore)){
                    keysPersistStore = new KeysPersistStore(this);
                }

                return keysPersistStore;
            case KeyLocationPolicy.RAM:
                if(isNull(keysRamStore)){
                    keysRamStore = new KeysRamStore();
                }

                return keysRamStore;
            default:
                throw new IllegalArgumentException("Unsupported key location policy");
        }
    }

    @NonNull
    public synchronized ILocalMediaStore localPhotos(){
        if(localMediaStore == null){
            localMediaStore = new LocalMediaStore(this);
        }

        return localMediaStore;
    }

    @NonNull
    public synchronized IFeedbackStore notifications(){
        if(feedbackStore == null){
            feedbackStore = new FeedbackStore(this);
        }

        return feedbackStore;
    }

    @NonNull
    public synchronized IDialogsStore dialogs(){
        if(dialogsStore == null){
            dialogsStore = new DialogsStore(this);
        }

        return dialogsStore;
    }

    @NonNull
    public synchronized IMessagesStore messages(){
        if(messagesStore == null){
            messagesStore = new MessagesStore(this);
        }

        return messagesStore;
    }

    @NonNull
    public synchronized IWallStore wall(){
        if(wallStore == null){
            wallStore = new WallStore(this);
        }

        return wallStore;
    }

    @NonNull
    public synchronized IFaveStore fave(){
        if(faveStore == null){
            faveStore = new FaveStore(this);
        }

        return faveStore;
    }

    @NonNull
    public synchronized IPhotosStore photos(){
        if(photosStore == null){
            photosStore = new PhotosStore(this);
        }

        return photosStore;
    }

    @NonNull
    public synchronized IRelativeshipStore relativeship(){
        if(relativeshipStore == null){
            relativeshipStore = new RelativeshipStore(this);
        }

        return relativeshipStore;
    }

    @NonNull
    public synchronized IFeedStore feed(){
        if(feedStore == null){
            feedStore = new FeedStore(this);
        }

        return feedStore;
    }

    @NonNull
    public synchronized IOwnersStore owners(){
        if(ownersStore == null){
            ownersStore = new OwnersRepositiry(this);
        }

        return ownersStore;
    }
}

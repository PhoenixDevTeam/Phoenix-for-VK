package biz.dealnote.messenger.db.impl;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.db.interfaces.IAttachmentsStorage;
import biz.dealnote.messenger.db.interfaces.ICommentsStorage;
import biz.dealnote.messenger.db.interfaces.IDatabaseStore;
import biz.dealnote.messenger.db.interfaces.IDialogsStorage;
import biz.dealnote.messenger.db.interfaces.IDocsStorage;
import biz.dealnote.messenger.db.interfaces.IFaveStorage;
import biz.dealnote.messenger.db.interfaces.IFeedStorage;
import biz.dealnote.messenger.db.interfaces.IFeedbackStorage;
import biz.dealnote.messenger.db.interfaces.IKeysStorage;
import biz.dealnote.messenger.db.interfaces.ILocalMediaStorage;
import biz.dealnote.messenger.db.interfaces.IMessagesStorage;
import biz.dealnote.messenger.db.interfaces.IOwnersStorage;
import biz.dealnote.messenger.db.interfaces.IPhotoAlbumsStorage;
import biz.dealnote.messenger.db.interfaces.IPhotosStorage;
import biz.dealnote.messenger.db.interfaces.IRelativeshipStorage;
import biz.dealnote.messenger.db.interfaces.IStickersStorage;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.db.interfaces.ITempDataStorage;
import biz.dealnote.messenger.db.interfaces.ITopicsStore;
import biz.dealnote.messenger.db.interfaces.IUploadQueueStore;
import biz.dealnote.messenger.db.interfaces.IVideoAlbumsStorage;
import biz.dealnote.messenger.db.interfaces.IVideoStorage;
import biz.dealnote.messenger.db.interfaces.IWallStorage;

import static biz.dealnote.messenger.util.Objects.isNull;

public class AppStorages extends ContextWrapper implements IStorages {

    private static AppStorages sStoresInstance;

    private AppStorages(Context base) {
        super(base);
    }

    public static AppStorages getInstance(Context baseContext){
        if(isNull(sStoresInstance)){
            synchronized (AppStorages.class){
                if(isNull(sStoresInstance)){
                    sStoresInstance = new AppStorages(baseContext.getApplicationContext());
                }
            }
        }

        return sStoresInstance;
    }

    private IOwnersStorage ownersStore;
    private IFeedStorage feedStore;
    private IRelativeshipStorage relativeshipStore;
    private IPhotosStorage photosStore;
    private IFaveStorage faveStore;
    private IWallStorage wallStore;
    private IMessagesStorage messagesStore;
    private IDialogsStorage dialogsStore;
    private IFeedbackStorage feedbackStore;
    private ILocalMediaStorage localMediaStore;
    private KeysPersistStorage keysPersistStore;
    private KeysRamStorage keysRamStore;
    private IAttachmentsStorage attachmentsStore;
    private volatile IVideoStorage videoStore;
    private volatile IVideoAlbumsStorage videoAlbumsStore;
    private volatile ICommentsStorage commentsStore;
    private volatile IPhotoAlbumsStorage photoAlbumsStore;
    private volatile ITopicsStore topicsStore;
    private volatile IDocsStorage docsStore;
    private volatile IStickersStorage stickersStore;
    private volatile IDatabaseStore databaseStore;

    @Override
    public ICommentsStorage comments() {
        if(isNull(commentsStore)){
            synchronized (this){
                if(isNull(commentsStore)){
                    commentsStore = new CommentsStorage(this);
                }
            }
        }

        return commentsStore;
    }

    @Override
    public IPhotoAlbumsStorage photoAlbums() {
        if(isNull(photoAlbumsStore)){
            synchronized (this){
                if(isNull(photoAlbumsStore)){
                    photoAlbumsStore = new PhotoAlbumsStorage(this);
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
                    topicsStore = new TopicsStorage(this);
                }
            }
        }

        return topicsStore;
    }

    @Override
    public IDocsStorage docs() {
        if(isNull(docsStore)){
            synchronized (this){
                if(isNull(docsStore)){
                    docsStore = new DocsStorage(this);
                }
            }
        }

        return docsStore;
    }

    @Override
    public IStickersStorage stickers() {
        if(isNull(stickersStore)){
            synchronized (this){
                if(isNull(stickersStore)){
                    stickersStore = new StickersStorage(this);
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
                    uploadQueueRepository = new UploadQueueStorage(this);
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
                    databaseStore = new DatabaseStorage(this);
                }
            }
        }
        return databaseStore;
    }

    private final ITempDataStorage tempDataStore = new TempDataStorage(this);

    @Override
    public ITempDataStorage tempStore() {
        return tempDataStore;
    }

    public IVideoAlbumsStorage videoAlbums(){
        if(isNull(videoAlbumsStore)){
            synchronized (this){
                if(isNull(videoAlbumsStore)){
                    videoAlbumsStore = new VideoAlbumsStorage(this);
                }
            }
        }

        return videoAlbumsStore;
    }

    public IVideoStorage videos(){
        if(isNull(videoStore)){
            synchronized (this){
                if(isNull(videoStore)){
                    videoStore = new VideoStorage(this);
                }
            }
        }

        return videoStore;
    }

    @NonNull
    public synchronized IAttachmentsStorage attachments(){
        if(attachmentsStore == null){
            attachmentsStore = new AttachmentsStorage(this);
        }

        return attachmentsStore;
    }

    @NonNull
    public synchronized IKeysStorage keys(@KeyLocationPolicy int policy){
        switch (policy){
            case KeyLocationPolicy.PERSIST:
                if(isNull(keysPersistStore)){
                    keysPersistStore = new KeysPersistStorage(this);
                }

                return keysPersistStore;
            case KeyLocationPolicy.RAM:
                if(isNull(keysRamStore)){
                    keysRamStore = new KeysRamStorage();
                }

                return keysRamStore;
            default:
                throw new IllegalArgumentException("Unsupported key location policy");
        }
    }

    @NonNull
    public synchronized ILocalMediaStorage localPhotos(){
        if(localMediaStore == null){
            localMediaStore = new LocalMediaStorage(this);
        }

        return localMediaStore;
    }

    @NonNull
    public synchronized IFeedbackStorage notifications(){
        if(feedbackStore == null){
            feedbackStore = new FeedbackStorage(this);
        }

        return feedbackStore;
    }

    @NonNull
    public synchronized IDialogsStorage dialogs(){
        if(dialogsStore == null){
            dialogsStore = new DialogsStorage(this);
        }

        return dialogsStore;
    }

    @NonNull
    public synchronized IMessagesStorage messages(){
        if(messagesStore == null){
            messagesStore = new MessagesStorage(this);
        }

        return messagesStore;
    }

    @NonNull
    public synchronized IWallStorage wall(){
        if(wallStore == null){
            wallStore = new WallStorage(this);
        }

        return wallStore;
    }

    @NonNull
    public synchronized IFaveStorage fave(){
        if(faveStore == null){
            faveStore = new FaveStorage(this);
        }

        return faveStore;
    }

    @NonNull
    public synchronized IPhotosStorage photos(){
        if(photosStore == null){
            photosStore = new PhotosStorage(this);
        }

        return photosStore;
    }

    @NonNull
    public synchronized IRelativeshipStorage relativeship(){
        if(relativeshipStore == null){
            relativeshipStore = new RelativeshipStorage(this);
        }

        return relativeshipStore;
    }

    @NonNull
    public synchronized IFeedStorage feed(){
        if(feedStore == null){
            feedStore = new FeedStorage(this);
        }

        return feedStore;
    }

    @NonNull
    public synchronized IOwnersStorage owners(){
        if(ownersStore == null){
            ownersStore = new OwnersStorage(this);
        }

        return ownersStore;
    }
}

package biz.dealnote.messenger.db.impl;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.NonNull;

import biz.dealnote.messenger.api.impl.BlacklistRepository;
import biz.dealnote.messenger.api.interfaces.IBlacklistRepository;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.db.interfaces.IAttachmentsStore;
import biz.dealnote.messenger.db.interfaces.ICommentsRepository;
import biz.dealnote.messenger.db.interfaces.IDatabaseStore;
import biz.dealnote.messenger.db.interfaces.IDialogsStore;
import biz.dealnote.messenger.db.interfaces.IDocsRepository;
import biz.dealnote.messenger.db.interfaces.IFaveRepository;
import biz.dealnote.messenger.db.interfaces.IFeedRepository;
import biz.dealnote.messenger.db.interfaces.IKeysRepository;
import biz.dealnote.messenger.db.interfaces.ILocalMediaRepository;
import biz.dealnote.messenger.db.interfaces.IMessagesStore;
import biz.dealnote.messenger.db.interfaces.INotificationsRepository;
import biz.dealnote.messenger.db.interfaces.IOwnersRepository;
import biz.dealnote.messenger.db.interfaces.IPhotoAlbumsRepository;
import biz.dealnote.messenger.db.interfaces.IPhotosRepository;
import biz.dealnote.messenger.db.interfaces.IRelativeshipRepository;
import biz.dealnote.messenger.db.interfaces.IRepositories;
import biz.dealnote.messenger.db.interfaces.IStickersRepository;
import biz.dealnote.messenger.db.interfaces.ITempDataStore;
import biz.dealnote.messenger.db.interfaces.ITopicsRepository;
import biz.dealnote.messenger.db.interfaces.IUploadQueueRepository;
import biz.dealnote.messenger.db.interfaces.IVideoAlbumsRepository;
import biz.dealnote.messenger.db.interfaces.IVideoRepository;
import biz.dealnote.messenger.db.interfaces.IWallStore;

import static biz.dealnote.messenger.util.Objects.isNull;

public class AppRepositories extends ContextWrapper implements IRepositories {

    private static AppRepositories sRepositoriesInstance;

    private AppRepositories(Context base) {
        super(base);
    }

    public static AppRepositories getInstance(Context baseContext){
        if(isNull(sRepositoriesInstance)){
            synchronized (AppRepositories.class){
                if(isNull(sRepositoriesInstance)){
                    sRepositoriesInstance = new AppRepositories(baseContext.getApplicationContext());
                }
            }
        }

        return sRepositoriesInstance;
    }

    private IOwnersRepository mOwnersRepository;
    private IFeedRepository mFeedRepository;
    private IRelativeshipRepository mRelativeshipRepository;
    private IPhotosRepository mPhotosRepository;
    private IFaveRepository mFaveRepositiry;
    private IWallStore mWallRepository;
    private IMessagesStore mMessagesRepository;
    private IDialogsStore mDialogsRepository;
    private INotificationsRepository mNotificationsRepository;
    private ILocalMediaRepository mLocalPhotosRepository;
    private KeysPersistRepository mKeysPersistRepository;
    private KeysRamRepository mKeysRamRepository;
    private IAttachmentsStore mMessageAttachmentsRepository;
    private volatile IVideoRepository mVideoRepository;
    private volatile IVideoAlbumsRepository mVideoAlbumsRepository;
    private volatile ICommentsRepository mCommentsRepository;
    private volatile IPhotoAlbumsRepository mPhotoAlbumsRepository;
    private volatile ITopicsRepository mTopicsRepository;
    private volatile IDocsRepository mDocsRepository;
    private volatile IStickersRepository stickersRepository;
    private volatile IDatabaseStore databaseStore;

    private volatile IBlacklistRepository blacklistRepository;

    @Override
    public ICommentsRepository comments() {
        if(isNull(mCommentsRepository)){
            synchronized (this){
                if(isNull(mCommentsRepository)){
                    mCommentsRepository = new CommentsRepository(this);
                }
            }
        }

        return mCommentsRepository;
    }

    @Override
    public IPhotoAlbumsRepository photoAlbums() {
        if(isNull(mPhotoAlbumsRepository)){
            synchronized (this){
                if(isNull(mPhotoAlbumsRepository)){
                    mPhotoAlbumsRepository = new PhotoAlbumsRepository(this);
                }
            }
        }

        return mPhotoAlbumsRepository;
    }

    @Override
    public ITopicsRepository topics() {
        if(isNull(mTopicsRepository)){
            synchronized (this){
                if(isNull(mTopicsRepository)){
                    mTopicsRepository = new TopicsRepository(this);
                }
            }
        }

        return mTopicsRepository;
    }

    @Override
    public IDocsRepository docs() {
        if(isNull(mDocsRepository)){
            synchronized (this){
                if(isNull(mDocsRepository)){
                    mDocsRepository = new DocsRepository(this);
                }
            }
        }

        return mDocsRepository;
    }

    @Override
    public IStickersRepository stickers() {
        if(isNull(stickersRepository)){
            synchronized (this){
                if(isNull(stickersRepository)){
                    stickersRepository = new StickersRepository(this);
                }
            }
        }

        return stickersRepository;
    }

    private volatile IUploadQueueRepository uploadQueueRepository;

    @Override
    public IUploadQueueRepository uploads() {
        if(isNull(uploadQueueRepository)){
            synchronized (this){
                if(isNull(uploadQueueRepository)){
                    uploadQueueRepository = new UploadQueueRepository(this);
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
    public IBlacklistRepository blacklist() {
        if(isNull(blacklistRepository)){
            synchronized (this){
                if(isNull(blacklistRepository)){
                    blacklistRepository = new BlacklistRepository();
                }
            }
        }
        return blacklistRepository;
    }

    @Override
    public ITempDataStore tempStore() {
        return tempDataStore;
    }

    public IVideoAlbumsRepository videoAlbums(){
        if(isNull(mVideoAlbumsRepository)){
            synchronized (this){
                if(isNull(mVideoAlbumsRepository)){
                    mVideoAlbumsRepository = new VideoAlbumsRepository(this);
                }
            }
        }

        return mVideoAlbumsRepository;
    }

    public IVideoRepository videos(){
        if(isNull(mVideoRepository)){
            synchronized (this){
                if(isNull(mVideoRepository)){
                    mVideoRepository = new VideoRepository(this);
                }
            }
        }

        return mVideoRepository;
    }

    @NonNull
    public synchronized IAttachmentsStore attachments(){
        if(mMessageAttachmentsRepository == null){
            mMessageAttachmentsRepository = new AttachmentsStore(this);
        }

        return mMessageAttachmentsRepository;
    }

    @NonNull
    public synchronized IKeysRepository keys(@KeyLocationPolicy int policy){
        switch (policy){
            case KeyLocationPolicy.PERSIST:
                if(isNull(mKeysPersistRepository)){
                    mKeysPersistRepository = new KeysPersistRepository(this);
                }

                return mKeysPersistRepository;
            case KeyLocationPolicy.RAM:
                if(isNull(mKeysRamRepository)){
                    mKeysRamRepository = new KeysRamRepository();
                }

                return mKeysRamRepository;
            default:
                throw new IllegalArgumentException("Unsupported key location policy");
        }
    }

    @NonNull
    public synchronized ILocalMediaRepository localPhotos(){
        if(mLocalPhotosRepository == null){
            mLocalPhotosRepository = new LocalMediaRepository(this);
        }

        return mLocalPhotosRepository;
    }

    @NonNull
    public synchronized INotificationsRepository notifications(){
        if(mNotificationsRepository == null){
            mNotificationsRepository = new NotificationsRepository(this);
        }

        return mNotificationsRepository;
    }

    @NonNull
    public synchronized IDialogsStore dialogs(){
        if(mDialogsRepository == null){
            mDialogsRepository = new DialogsStore(this);
        }

        return mDialogsRepository;
    }

    @NonNull
    public synchronized IMessagesStore messages(){
        if(mMessagesRepository == null){
            mMessagesRepository = new MessagesStore(this);
        }

        return mMessagesRepository;
    }

    @NonNull
    public synchronized IWallStore wall(){
        if(mWallRepository == null){
            mWallRepository = new WallStore(this);
        }

        return mWallRepository;
    }

    @NonNull
    public synchronized IFaveRepository fave(){
        if(mFaveRepositiry == null){
            mFaveRepositiry = new FaveRepository(this);
        }

        return mFaveRepositiry;
    }

    @NonNull
    public synchronized IPhotosRepository photos(){
        if(mPhotosRepository == null){
            mPhotosRepository = new PhotosRepository(this);
        }

        return mPhotosRepository;
    }

    @NonNull
    public synchronized IRelativeshipRepository relativeship(){
        if(mRelativeshipRepository == null){
            mRelativeshipRepository = new RelativeshipRepository(this);
        }

        return mRelativeshipRepository;
    }

    @NonNull
    public synchronized IFeedRepository feed(){
        if(mFeedRepository == null){
            mFeedRepository = new FeedRepository(this);
        }

        return mFeedRepository;
    }

    @NonNull
    public synchronized IOwnersRepository owners(){
        if(mOwnersRepository == null){
            mOwnersRepository = new OwnersRepositiry(this);
        }

        return mOwnersRepository;
    }
}

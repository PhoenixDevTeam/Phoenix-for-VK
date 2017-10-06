package biz.dealnote.messenger.db.interfaces;

import biz.dealnote.messenger.api.interfaces.IBlacklistRepository;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;

/**
 * Created by ruslan.kolbasa on 25.11.2016.
 * phoenix
 */
public interface IRepositories {

    IBlacklistRepository blacklist();

    ITempDataStore tempStore();

    IAudioCoversRepository audioCovers();

    IVideoAlbumsRepository videoAlbums();

    IVideoRepository videos();

    IAttachmentsStore attachments();

    IKeysRepository keys(@KeyLocationPolicy int policy);

    ILocalMediaRepository localPhotos();

    INotificationsRepository notifications();

    IDialogsStore dialogs();

    IMessagesStore messages();

    IWallStore wall();

    IFaveRepository fave();

    IPhotosRepository photos();

    IRelativeshipRepository relativeship();

    IFeedRepository feed();

    IOwnersRepository owners();

    ICommentsRepository comments();

    IPhotoAlbumsRepository photoAlbums();

    ITopicsRepository topics();

    IDocsRepository docs();

    IStickersRepository stickers();

    IUploadQueueRepository uploads();

    IDatabaseStore database();
}
package biz.dealnote.messenger.domain.impl;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.interfaces.IDocsApi;
import biz.dealnote.messenger.api.interfaces.IMessagesApi;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.api.model.AttachmentsTokenCreator;
import biz.dealnote.messenger.api.model.ChatUserDto;
import biz.dealnote.messenger.api.model.IAttachmentToken;
import biz.dealnote.messenger.api.model.VKApiChat;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VkApiConversation;
import biz.dealnote.messenger.api.model.VkApiDialog;
import biz.dealnote.messenger.api.model.VkApiDoc;
import biz.dealnote.messenger.api.model.response.SearchDialogsResponse;
import biz.dealnote.messenger.crypt.AesKeyPair;
import biz.dealnote.messenger.crypt.CryptHelper;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.crypt.KeyPairDoesNotExistException;
import biz.dealnote.messenger.db.column.UserColumns;
import biz.dealnote.messenger.db.interfaces.IDialogsStorage;
import biz.dealnote.messenger.db.interfaces.IMessagesStorage;
import biz.dealnote.messenger.db.interfaces.IStorages;
import biz.dealnote.messenger.db.model.MessagePatch;
import biz.dealnote.messenger.db.model.PeerPatch;
import biz.dealnote.messenger.db.model.entity.DialogEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.SimpleDialogEntity;
import biz.dealnote.messenger.db.model.entity.StickerEntity;
import biz.dealnote.messenger.domain.IMessagesDecryptor;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.Mode;
import biz.dealnote.messenger.domain.mappers.Dto2Entity;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.domain.mappers.Entity2Dto;
import biz.dealnote.messenger.domain.mappers.Entity2Model;
import biz.dealnote.messenger.domain.mappers.Model2Dto;
import biz.dealnote.messenger.domain.mappers.Model2Entity;
import biz.dealnote.messenger.exception.NotFoundException;
import biz.dealnote.messenger.exception.UploadNotResolvedException;
import biz.dealnote.messenger.longpoll.model.MessagesRead;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AppChatUser;
import biz.dealnote.messenger.model.Conversation;
import biz.dealnote.messenger.model.CryptStatus;
import biz.dealnote.messenger.model.Dialog;
import biz.dealnote.messenger.model.IOwnersBundle;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.MessageStatus;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.model.SentMsg;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.criteria.DialogsCriteria;
import biz.dealnote.messenger.model.criteria.MessagesCriteria;
import biz.dealnote.messenger.upload.IUploadManager;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Unixtime;
import biz.dealnote.messenger.util.VKOwnIds;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.safelyCloseAction;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.listEmptyIfNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;
import static biz.dealnote.messenger.util.Utils.safelyClose;

/**
 * Created by admin on 03.09.2017.
 * В этом классе сосредоточена вся бизнес-логика для работы с сообщениями
 */
public class MessagesInteractor implements IMessagesInteractor {

    private static final SingleTransformer<List<VKApiMessage>, List<MessageEntity>> DTO_TO_DBO = single -> single
            .map(dtos -> {
                List<MessageEntity> dbos = new ArrayList<>(dtos.size());

                for (VKApiMessage dto : dtos) {
                    dbos.add(Dto2Entity.buildMessageDbo(dto));
                }

                return dbos;
            });

    private final IOwnersInteractor ownersInteractor;
    private final IStorages storages;
    private final INetworker networker;
    private final IMessagesDecryptor decryptor;
    private final IUploadManager uploadManager;

    public MessagesInteractor(INetworker networker, IOwnersInteractor ownersInteractor, IStorages storages, IUploadManager uploadManager) {
        this.ownersInteractor = ownersInteractor;
        this.networker = networker;
        this.storages = storages;
        this.decryptor = new MessagesDecryptor(storages);
        this.uploadManager = uploadManager;
    }

    @Override
    public Completable handleMessagesRead(int accountId, @NonNull List<MessagesRead> reads) {
        List<PeerPatch> patches = new ArrayList<>(reads.size());

        for(MessagesRead read : reads){
            PeerPatch patch = new PeerPatch(read.getPeerId());

            if(read.isOut()){
                patch.withOutRead(read.getToMessageId(), read.getUnreadCount());
            } else {
                patch.withInRead(read.getToMessageId(), read.getUnreadCount());
            }
        }

        return storages.dialogs().applyPatches(accountId, patches);
    }

    private static Conversation entity2Model(int accountId, SimpleDialogEntity entity, IOwnersBundle owners) {
        return new Conversation(entity.getPeerId())
                .setInRead(entity.getInRead())
                .setOutRead(entity.getOutRead())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setPhoto200(entity.getPhoto200())
                .setUnreadCount(entity.getUnreadCount())
                .setTitle(entity.getTitle())
                .setInterlocutor(Peer.isGroup(entity.getPeerId()) || Peer.isUser(entity.getPeerId()) ? owners.getById(entity.getPeerId()) : null)
                .setPinned(isNull(entity.getPinned()) ? null : Entity2Model.message(accountId, entity.getPinned(), owners));
    }

    @Override
    public Flowable<Conversation> getConversation(int accountId, int peerId, @NonNull Mode mode) {
        Single<Optional<Conversation>> cached = storages.dialogs()
                .findSimple(accountId, peerId)
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Single.just(Optional.<Conversation>empty());
                    }

                    return Single.just(optional.get())
                            .compose(simpleEntity2Conversation(accountId, Collections.emptyList()))
                            .map(Optional::wrap);
                });

        Single<Conversation> actual = networker.vkDefault(accountId)
                .messages()
                .getConversations(Collections.singletonList(peerId), true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (isEmpty(response.items)) {
                        return Single.error(new NotFoundException());
                    }

                    VkApiConversation dto = response.items.get(0);
                    SimpleDialogEntity entity = Dto2Entity.dto2Entity(dto);

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.buildOwnerDbos(response.profiles, response.groups);

                    return ownersInteractor.insertOwners(accountId, ownerEntities)
                            .andThen(storages.dialogs().saveSimple(accountId, entity))
                            .andThen(Single.just(entity))
                            .compose(simpleEntity2Conversation(accountId, existsOwners));
                });

        switch (mode) {
            case ANY:
                return cached
                        .flatMap(optional -> optional.isEmpty() ? actual : Single.just(optional.get()))
                        .toFlowable();
            case NET:
                return actual.toFlowable();
            case CACHE:
                return cached
                        .flatMap(optional -> optional.isEmpty() ? Single.error(new NotFoundException()) : Single.just(optional.get()))
                        .toFlowable();
            case CACHE_THEN_ACTUAL:
                Flowable<Conversation> cachedFlowable = cached.toFlowable()
                        .filter(Optional::nonEmpty)
                        .map(Optional::get);

                return Flowable.concat(cachedFlowable, actual.toFlowable());
        }

        throw new IllegalArgumentException("Unsupported mode: " + mode);
    }

    private SingleTransformer<SimpleDialogEntity, Conversation> simpleEntity2Conversation(int accountId, Collection<Owner> existingOwners) {
        return single -> single
                .flatMap(entity -> {
                    VKOwnIds owners = new VKOwnIds();
                    if(Peer.isGroup(entity.getPeerId()) || Peer.isUser(entity.getPeerId())){
                        owners.append(entity.getPeerId());
                    }

                    if(nonNull(entity.getPinned())){
                        Entity2Model.fillOwnerIds(owners, Collections.singletonList(entity.getPinned()));
                    }

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, owners.getAll(), IOwnersInteractor.MODE_ANY, existingOwners)
                            .map(bundle -> entity2Model(accountId, entity, bundle));
                });
    }

    @Override
    public Completable edit(int accountId, @NonNull Message message, String body, @NonNull List<AbsModel> attachments, boolean keepForwardMessages) {
        List<IAttachmentToken> attachmentTokens = Model2Dto.createTokens(attachments);
        return networker.vkDefault(accountId)
                .messages()
                .edit(message.getPeerId(), message.getId(), body, attachmentTokens, keepForwardMessages, null);
    }

    @Override
    public Single<List<Message>> getCachedPeerMessages(int accountId, int peerId) {
        final MessagesCriteria criteria = new MessagesCriteria(accountId, peerId);
        return storages.messages()
                .getByCriteria(criteria, true, true)
                .compose(entities2Models(accountId))
                .compose(decryptor.withMessagesDecryption(accountId));
    }

    @Override
    public Single<List<Dialog>> getCachedDialogs(int accountId) {
        DialogsCriteria criteria = new DialogsCriteria(accountId);

        return storages.dialogs()
                .getDialogs(criteria)
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();

                    for (DialogEntity dbo : dbos) {
                        switch (Peer.getType(dbo.getPeerId())) {
                            case Peer.GROUP:
                            case Peer.USER:
                                ownIds.append(dbo.getPeerId());
                                break;

                            case Peer.CHAT:
                                ownIds.append(dbo.getMessage().getFromId());
                                break;
                        }
                    }

                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY)
                            .flatMap(owners -> {
                                final List<Message> messages = new ArrayList<>(0);
                                final List<Dialog> dialogs = new ArrayList<>(dbos.size());

                                for (DialogEntity dbo : dbos) {
                                    Dialog dialog = Entity2Model.buildDialogFromDbo(accountId, dbo, owners);
                                    dialogs.add(dialog);

                                    if (dbo.getMessage().isEncrypted()) {
                                        messages.add(dialog.getMessage());
                                    }
                                }

                                if (nonEmpty(messages)) {
                                    return Single.just(messages)
                                            .compose(decryptor.withMessagesDecryption(accountId))
                                            .map(ignored -> dialogs);
                                }

                                return Single.just(dialogs);
                            });
                });
    }

    private SingleTransformer<List<MessageEntity>, List<Message>> entities2Models(int accountId) {
        return single -> single
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();
                    Entity2Model.fillOwnerIds(ownIds, dbos);

                    return this.ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersInteractor.MODE_ANY)
                            .map(owners -> {
                                final List<Message> messages = new ArrayList<>(dbos.size());

                                for (MessageEntity dbo : dbos) {
                                    messages.add(Entity2Model.message(accountId, dbo, owners));
                                }

                                return messages;
                            });
                });
    }

    private Completable insertPeerMessages(int accountId, int peerId, List<VKApiMessage> messages, boolean clearBefore) {
        return Single.just(messages)
                .compose(DTO_TO_DBO)
                .flatMapCompletable(dbos -> storages.messages().insertPeerDbos(accountId, peerId, dbos, clearBefore));
    }

    @Override
    public Completable insertMessages(int accountId, List<VKApiMessage> messages) {
        return Single.just(messages)
                .compose(DTO_TO_DBO)
                .flatMap(dbos -> storages.messages().insertDbos(accountId, dbos))
                .ignoreElement();
    }

    @Override
    public Single<List<Message>> getPeerMessages(int accountId, int peerId, int count, Integer offset,
                                                 Integer startMessageId, boolean cacheData) {
        return networker.vkDefault(accountId)
                .messages()
                .getHistory(offset, count, peerId, startMessageId, false)
                .flatMap(response -> {
                    final List<VKApiMessage> dtos = listEmptyIfNull(response.messages);

                    if (nonNull(startMessageId) && nonEmpty(dtos) && startMessageId == dtos.get(0).id) {
                        dtos.remove(0);
                    }

                    final Completable completable;

                    if (cacheData) {
                        completable = this.insertPeerMessages(accountId, peerId, dtos, Objects.isNull(startMessageId))
                                .andThen(this.fixDialogs(accountId, peerId, response.unread));
                    } else {
                        completable = Completable.complete();
                    }

                    VKOwnIds ownerIds = new VKOwnIds();
                    ownerIds.append(dtos);

                    return completable
                            .andThen(ownersInteractor
                                    .findBaseOwnersDataAsBundle(accountId, ownerIds.getAll(), IOwnersInteractor.MODE_ANY)
                                    .flatMap(owners -> {
                                        if (isNull(startMessageId) && cacheData) {
                                            // Это важно !!!
                                            // Если мы получаем сообщения сначала и кэшируем их в базу,
                                            // то нельзя отдать этот список в ответ (как сделано чуть ниже)
                                            // Так как мы теряем сообщения со статусами, отличными от SENT
                                            return this.getCachedPeerMessages(accountId, peerId);
                                        }

                                        List<Message> messages = new ArrayList<>(response.messages.size());
                                        for (VKApiMessage dto : dtos) {
                                            messages.add(Dto2Model.transform(accountId, dto, owners));
                                        }

                                        return Single.just(messages)
                                                .compose(decryptor.withMessagesDecryption(accountId));
                                    }));
                });
    }

    @Override
    public Single<List<Dialog>> getDialogs(int accountId, int count, Integer startMessageId) {
        final boolean clear = isNull(startMessageId);
        final IDialogsStorage dialogsStore = this.storages.dialogs();

        return networker.vkDefault(accountId)
                .messages()
                .getDialogs(null, count, startMessageId, true, Constants.MAIN_OWNER_FIELDS)
                .map(response -> {
                    if (nonNull(startMessageId) && safeCountOf(response.dialogs) > 0) {
                        // remove first item, because we will have duplicate with previous response
                        response.dialogs.remove(0);
                    }
                    return response;
                })
                .flatMap(response -> {
                    List<VkApiDialog> apiDialogs = listEmptyIfNull(response.dialogs);

                    final Collection<Integer> ownerIds;

                    if (nonEmpty(apiDialogs)) {
                        VKOwnIds vkOwnIds = new VKOwnIds();
                        vkOwnIds.append(accountId); // добавляем свой профайл на всякий случай

                        for (VkApiDialog dialog : apiDialogs) {
                            vkOwnIds.append(dialog);
                        }

                        ownerIds = vkOwnIds.getAll();
                    } else {
                        ownerIds = Collections.emptyList();
                    }

                    List<Owner> existsOwners = Dto2Model.transformOwners(response.profiles, response.groups);
                    OwnerEntities ownerEntities = Dto2Entity.buildOwnerDbos(response.profiles, response.groups);

                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ownerIds, IOwnersInteractor.MODE_ANY, existsOwners)
                            .flatMap(owners -> {
                                final List<DialogEntity> entities = new ArrayList<>(apiDialogs.size());
                                final List<Dialog> dialogs = new ArrayList<>(apiDialogs.size());
                                final List<Message> encryptedMessages = new ArrayList<>(0);

                                for (VkApiDialog dto : apiDialogs) {
                                    DialogEntity entity = Dto2Entity.dialog(dto);
                                    entities.add(entity);

                                    Dialog dialog = Dto2Model.transform(accountId, dto, owners);
                                    dialogs.add(dialog);

                                    if (entity.getMessage().isEncrypted()) {
                                        encryptedMessages.add(dialog.getMessage());
                                    }
                                }

                                final Completable insertCompletable = dialogsStore
                                        .insertDialogs(accountId, entities, clear)
                                        .andThen(ownersInteractor.insertOwners(accountId, ownerEntities))
                                        .doOnComplete(() -> dialogsStore.setUnreadDialogsCount(accountId, response.unreadCount));

                                if (nonEmpty(encryptedMessages)) {
                                    return insertCompletable.andThen(Single.just(encryptedMessages)
                                            .compose(decryptor.withMessagesDecryption(accountId))
                                            .map(ignored -> dialogs));
                                }

                                return insertCompletable.andThen(Single.just(dialogs));
                            });
                });
    }

    @Override
    public Single<List<Message>> findCachedMessages(int accountId, List<Integer> ids) {
        return storages.messages()
                .findMessagesByIds(accountId, ids, true, true)
                .compose(entities2Models(accountId))
                .compose(decryptor.withMessagesDecryption(accountId));
    }

    @Override
    public Completable fixDialogs(int accountId, int peerId) {
        return storages.messages()
                .findLastSentMessageIdForPeer(accountId, peerId)
                .flatMapCompletable(id -> {
                    if (id.isEmpty()) {
                        return storages.dialogs().removePeerWithId(accountId, peerId);
                    }

                    return storages.messages()
                            .calculateUnreadCount(accountId, peerId)
                            .flatMapCompletable(count -> storages.dialogs().updatePeerWithId(accountId, peerId, id.get(), count));
                });
    }

    @Override
    public Completable fixDialogs(int accountId, int peerId, int unreadCount) {
        return storages.messages()
                .findLastSentMessageIdForPeer(accountId, peerId)
                .flatMapCompletable(id -> {
                    if (id.isEmpty()) {
                        return storages.dialogs().removePeerWithId(accountId, peerId);
                    }

                    return storages.dialogs().updatePeerWithId(accountId, peerId, id.get(), unreadCount);
                });
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public Single<Message> put(SaveMessageBuilder builder) {
        final int accountId = builder.getAccountId();
        final Integer draftMessageId = builder.getDraftMessageId();
        final int peerId = builder.getPeerId();

        return this.getTargetMessageStatus(builder)
                .flatMap(status -> {
                    final MessagePatch patch = new MessagePatch(status, accountId);

                    patch.setEncrypted(builder.isRequireEncryption());
                    patch.setDate(Unixtime.now());
                    patch.setRead(false);
                    patch.setOut(true);
                    patch.setDeleted(false);
                    patch.setImportant(false);

                    File voice = builder.getVoiceMessageFile();

                    if (nonNull(voice)) {
                        Map<Integer, String> extras = new HashMap<>(1);
                        extras.put(Message.Extra.VOICE_RECORD, voice.getAbsolutePath());
                        patch.setExtras(extras);
                    }

                    if (nonEmpty(builder.getAttachments())) {
                        patch.setAttachments(Model2Entity.buildDboAttachments(builder.getAttachments()));
                    }

                    final List<Message> fwds = builder.getForwardMessages();
                    if (nonEmpty(fwds)) {
                        List<MessageEntity> fwddbos = new ArrayList<>(fwds.size());

                        for (Message message : fwds) {
                            MessageEntity fwddbo = Model2Entity.buildMessageEntity(message);
                            fwddbo.setOriginalId(message.getId()); // сохранить original_id необходимо, так как при вставке в таблицу _ID потеряется

                            // fixes
                            if (fwddbo.isOut()) {
                                fwddbo.setFromId(accountId);
                            }

                            fwddbos.add(fwddbo);
                        }

                        patch.setForward(fwddbos);
                    } else {
                        patch.setForward(Collections.emptyList());
                    }

                    return getFinalMessagesBody(builder)
                            .flatMap(body -> {
                                patch.setBody(body.get());

                                Single<Integer> storeSingle;
                                if (nonNull(draftMessageId)) {
                                    storeSingle = storages.messages().applyPatch(accountId, draftMessageId, patch);
                                } else {
                                    storeSingle = storages.messages().insert(accountId, peerId, patch);
                                }

                                return storeSingle
                                        .flatMap(resultMid -> storages.messages()
                                                .findMessagesByIds(accountId, Collections.singletonList(resultMid), true, true)
                                                .compose(entities2Models(accountId))
                                                .map(messages -> {
                                                    if (messages.isEmpty()) {
                                                        throw new NotFoundException();
                                                    }

                                                    Message message = messages.get(0);

                                                    if (builder.isRequireEncryption()) {
                                                        message.setDecryptedBody(builder.getBody());
                                                        message.setCryptStatus(CryptStatus.DECRYPTED);
                                                    }

                                                    return message;
                                                }));
                            });
                });
    }

    @Override
    public Single<Integer> send(int accountId, int dbid) {
        final IMessagesStorage store = this.storages.messages();

        return store
                .findMessagesByIds(accountId, Collections.singletonList(dbid), true, false)
                .flatMap(dbos -> {
                    if (dbos.isEmpty()) {
                        throw new NotFoundException();
                    }

                    final MessageEntity entity = dbos.get(0);
                    return store.changeMessageStatus(accountId, dbid, MessageStatus.SENDING, null)
                            .andThen(internalSend(accountId, entity)
                                    .flatMap(vkid -> store.changeMessageStatus(accountId, dbid, MessageStatus.SENT, vkid)
                                            .andThen(fixDialogs(accountId, entity.getPeerId(), 0))
                                            .andThen(Single.just(vkid)))
                                    .onErrorResumeNext(throwable -> store
                                            .changeMessageStatus(accountId, dbid, MessageStatus.ERROR, null)
                                            .andThen(Single.error(throwable))));
                });
    }

    @Override
    public Single<SentMsg> sendUnsentMessage(Collection<Integer> accountIds) {
        final IMessagesStorage store = this.storages.messages();

        return store
                .findFirstUnsentMessage(accountIds, true, false)
                .flatMap(optional -> {
                    if (optional.isEmpty()) {
                        return Single.error(new NotFoundException());
                    }

                    final MessageEntity entity = optional.get().getSecond();
                    final int accountId = optional.get().getFirst();
                    final int dbid = entity.getId();
                    final int peerId = entity.getPeerId();

                    return store.changeMessageStatus(accountId, dbid, MessageStatus.SENDING, null)
                            .andThen(internalSend(accountId, entity)
                                    .flatMap(vkid -> store.changeMessageStatus(accountId, dbid, MessageStatus.SENT, vkid)
                                            .andThen(fixDialogs(accountId, entity.getPeerId(), 0))
                                            .andThen(Single.just(new SentMsg(dbid, vkid, peerId, accountId))))
                                    .onErrorResumeNext(throwable -> store
                                            .changeMessageStatus(accountId, dbid, MessageStatus.ERROR, null)
                                            .andThen(Single.error(throwable))));
                });
    }

    @Override
    public Single<List<Object>> searchDialogs(int accountId, int count, String q) {
        return networker.vkDefault(accountId)
                .messages()
                .searchDialogs(q, count, Constants.MAIN_OWNER_FIELDS)
                .map(chattables -> {
                    List<Object> models = new ArrayList<>(chattables.size());

                    for (SearchDialogsResponse.AbsChattable chattable : chattables) {
                        if (chattable instanceof SearchDialogsResponse.Chat) {
                            final VKApiChat chat = ((SearchDialogsResponse.Chat) chattable).getChat();
                            models.add(Dto2Model.transform(chat));
                        } else if (chattable instanceof SearchDialogsResponse.User) {
                            final VKApiUser user = ((SearchDialogsResponse.User) chattable).getUser();
                            models.add(Dto2Model.transformUser(user));
                        } else if (chattable instanceof SearchDialogsResponse.Community) {
                            final VKApiCommunity community = ((SearchDialogsResponse.Community) chattable).getCommunity();
                            models.add(Dto2Model.transformCommunity(community));
                        }
                    }

                    // null because load more not supported
                    return models;
                });
    }

    @Override
    public Single<List<Message>> searchMessages(int accountId, Integer peerId, int count, int offset, String q) {
        return networker.vkDefault(accountId)
                .messages()
                .search(q, peerId, null, null, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    VKOwnIds ids = new VKOwnIds().append(dtos);

                    return ownersInteractor
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY)
                            .map(bundle -> {
                                List<Message> data = new ArrayList<>(dtos.size());
                                for (VKApiMessage dto : dtos) {
                                    Message message = Dto2Model.transform(accountId, dto, bundle);
                                    data.add(message);
                                }

                                return data;
                            })
                            .compose(decryptor.withMessagesDecryption(accountId));
                });
    }

    @Override
    public Single<List<AppChatUser>> getChatUsers(int accountId, int chatId) {
        return networker.vkDefault(accountId)
                .messages()
                .getChat(chatId, null, UserColumns.API_FIELDS, null)
                .map(chats -> {
                    if (chats.isEmpty()) {
                        throw new NotFoundException();
                    }

                    return chats.get(0);
                })
                .flatMap(chatDto -> {
                    List<ChatUserDto> dtos = listEmptyIfNull(chatDto.users);

                    VKOwnIds ids = new VKOwnIds();
                    List<Owner> owners = new ArrayList<>(dtos.size());

                    for (ChatUserDto dto : dtos) {
                        ids.append(dto.invited_by);
                        owners.add(Dto2Model.transformUser(dto.user));
                    }

                    final boolean isAdmin = accountId == chatDto.admin_id;

                    return ownersInteractor.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersInteractor.MODE_ANY, owners)
                            .map(ownersBundle -> {
                                List<AppChatUser> models = new ArrayList<>(dtos.size());

                                for (ChatUserDto dto : dtos) {
                                    AppChatUser user = new AppChatUser(Dto2Model.transformUser(dto.user), dto.invited_by, dto.type);
                                    user.setCanRemove(isAdmin || user.getInvitedBy() == accountId);

                                    if (user.getInvitedBy() != 0) {
                                        user.setInvited((User) ownersBundle.getById(user.getInvitedBy()));
                                    }

                                    models.add(user);
                                }

                                return models;
                            });
                });
    }

    @Override
    public Completable removeChatUser(int accountId, int chatId, int userId) {
        return networker.vkDefault(accountId)
                .messages()
                .removeChatUser(chatId, userId)
                .ignoreElement();
    }

    @Override
    public Single<List<AppChatUser>> addChatUsers(int accountId, int chatId, List<User> users) {
        IMessagesApi api = networker.vkDefault(accountId).messages();

        return ownersInteractor.getBaseOwnerInfo(accountId, accountId, IOwnersInteractor.MODE_ANY)
                .flatMap(iam -> {
                    Completable completable = Completable.complete();

                    List<AppChatUser> data = new ArrayList<>();

                    for (User user : users) {
                        completable = completable.andThen(api.addChatUser(chatId, user.getId()).ignoreElement());

                        AppChatUser chatUser = new AppChatUser(user, accountId, "profile")
                                .setCanRemove(true)
                                .setInvited((User) iam);

                        data.add(chatUser);
                    }

                    return completable.andThen(Single.just(data));
                });
    }

    @Override
    public Completable deleteDialog(int accountId, int peedId, int count, int offset) {
        // TODO: 07.10.2017 Fix this
        return networker.vkDefault(accountId)
                .messages()
                .deleteDialog(peedId, offset, count)
                .flatMapCompletable(ignored -> storages.dialogs()
                        .removePeerWithId(accountId, peedId)
                        .andThen(storages.messages()
                                .insertPeerDbos(accountId, peedId, Collections.emptyList(), true)));
    }

    @Override
    public Completable deleteMessages(int accountId, Collection<Integer> ids) {
        // TODO: 07.10.2017 Remove from Cache?
        return networker.vkDefault(accountId)
                .messages()
                .delete(ids, null, null)
                .ignoreElement();
    }

    @Override
    public Completable restoreMessage(int accountId, int messageId) {
        // TODO: 07.10.2017 Restore into Cache?
        return networker.vkDefault(accountId)
                .messages()
                .restore(messageId)
                .ignoreElement();
    }

    @Override
    public Completable changeChatTitle(int accountId, int chatId, String title) {
        return networker.vkDefault(accountId)
                .messages()
                .editChat(chatId, title)
                .flatMapCompletable(ignored -> storages.dialogs()
                        .changeTitle(accountId, Peer.fromChatId(chatId), title));
    }

    @Override
    public Single<Integer> createGroupChat(int accountId, Collection<Integer> users, String title) {
        return networker.vkDefault(accountId)
                .messages()
                .createChat(users, title);
    }

    @Override
    public Completable markAsRead(int accountId, int peerId, int toId) {
        // TODO: 07.10.2017 Dialogs table update?

        PeerPatch patch = new PeerPatch(peerId).withInRead(toId, 0);
        return networker.vkDefault(accountId)
                .messages()
                .markAsRead(peerId, toId)
                .flatMapCompletable(ignored -> storages.dialogs().applyPatches(accountId, Collections.singletonList(patch)));

                //.flatMapCompletable(ignored -> storages.messages().markAsRead(accountId, peerId))
                //.andThen(fixDialogs(accountId, peerId));
    }

    private Single<Integer> internalSend(int accountId, MessageEntity dbo) {
        if (isEmpty(dbo.getExtras()) && isEmpty(dbo.getAttachments()) && dbo.getForwardCount() == 0) {
            return networker.vkDefault(accountId)
                    .messages()
                    .send(dbo.getId(), dbo.getPeerId(), null, dbo.getBody(), null, null, null, null, null);
        }

        final Collection<IAttachmentToken> attachments = new LinkedList<>();

        try {
            if (nonEmpty(dbo.getAttachments())) {
                for (Entity a : dbo.getAttachments()) {
                    if (a instanceof StickerEntity) {
                        final int stickerId = ((StickerEntity) a).getId();

                        return networker.vkDefault(accountId)
                                .messages()
                                .send(dbo.getId(), dbo.getPeerId(), null, null, null, null, null, null, stickerId);
                    }

                    attachments.add(Entity2Dto.createToken(a));
                }
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        return checkVoiceMessage(accountId, dbo)
                .flatMap(optionalToken -> {
                    if (optionalToken.nonEmpty()) {
                        attachments.add(optionalToken.get());
                    }

                    return checkForwardMessages(accountId, dbo)
                            .flatMap(optionalFwd -> networker.vkDefault(accountId)
                                    .messages()
                                    .send(dbo.getId(), dbo.getPeerId(), null, dbo.getBody(), null, null, attachments, optionalFwd.get(), null));
                });
    }

    private Single<Optional<List<Integer>>> checkForwardMessages(int accountId, MessageEntity dbo) {
        if (dbo.getForwardCount() == 0) {
            return Single.just(Optional.empty());
        }

        return storages.messages()
                .getForwardMessageIds(accountId, dbo.getId())
                .map(Optional::wrap);
    }

    private Single<Optional<IAttachmentToken>> checkVoiceMessage(int accountId, MessageEntity dbo) {
        Map<Integer, String> extras = dbo.getExtras();

        if (nonNull(extras) && extras.containsKey(Message.Extra.VOICE_RECORD)) {
            final String filePath = extras.get(Message.Extra.VOICE_RECORD);
            final IDocsApi docsApi = networker.vkDefault(accountId).docs();

            return docsApi.getUploadServer(null, "audio_message")
                    .flatMap(server -> {
                        final File file = new File(filePath);
                        final InputStream[] is = new InputStream[1];

                        try {
                            is[0] = new FileInputStream(file);
                            return networker.uploads()
                                    .uploadDocumentRx(server.getUrl(), file.getName(), is[0], null)
                                    .doFinally(safelyCloseAction(is[0]))
                                    .flatMap(uploadDto -> docsApi
                                            .save(uploadDto.file, null, null)
                                            .map(dtos -> {
                                                if (dtos.isEmpty()) {
                                                    throw new NotFoundException("Unable to save voice message");
                                                }

                                                VkApiDoc dto = dtos.get(0);
                                                IAttachmentToken token = AttachmentsTokenCreator.ofDocument(dto.id, dto.ownerId, dto.accessKey);
                                                return Optional.wrap(token);
                                            }));
                        } catch (FileNotFoundException e) {
                            safelyClose(is[0]);
                            return Single.error(e);
                        }
                    });
        }

        return Single.just(Optional.empty());
    }

    private Single<Optional<String>> getFinalMessagesBody(SaveMessageBuilder builder) {
        if (isEmpty(builder.getBody()) || !builder.isRequireEncryption()) {
            return Single.just(Optional.wrap(builder.getBody()));
        }

        @KeyLocationPolicy
        int policy = builder.getKeyLocationPolicy();

        return storages.keys(policy)
                .findLastKeyPair(builder.getAccountId(), builder.getPeerId())
                .map(key -> {
                    if (key.isEmpty()) {
                        throw new KeyPairDoesNotExistException();
                    }

                    final AesKeyPair pair = key.get();

                    String encrypted = CryptHelper.encryptWithAes(builder.getBody(),
                            pair.getMyAesKey(),
                            builder.getBody(),
                            pair.getSessionId(),
                            builder.getKeyLocationPolicy()
                    );

                    return Optional.wrap(encrypted);
                });
    }

    private Single<Integer> getTargetMessageStatus(SaveMessageBuilder builder) {
        final int accountId = builder.getAccountId();

        if (isNull(builder.getDraftMessageId())) {
            return Single.just(MessageStatus.QUEUE);
        }

        UploadDestination destination = UploadDestination.forMessage(builder.getDraftMessageId());
        return uploadManager.get(accountId, destination)
                .map(uploads -> {
                    if (uploads.isEmpty()) {
                        return MessageStatus.QUEUE;
                    }

                    boolean uploadingNow = false;

                    for (Upload o : uploads) {
                        if (o.getStatus() == Upload.STATUS_CANCELLING) {
                            continue;
                        }

                        if (o.getStatus() == Upload.STATUS_ERROR) {
                            throw new UploadNotResolvedException();
                        }

                        uploadingNow = true;
                    }

                    return uploadingNow ? MessageStatus.WAITING_FOR_UPLOAD : MessageStatus.QUEUE;
                });
    }
}
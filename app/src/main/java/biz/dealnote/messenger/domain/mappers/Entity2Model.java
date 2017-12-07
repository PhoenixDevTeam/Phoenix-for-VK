package biz.dealnote.messenger.domain.mappers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.db.model.entity.AudioEntity;
import biz.dealnote.messenger.db.model.entity.CommentEntity;
import biz.dealnote.messenger.db.model.entity.CommunityEntity;
import biz.dealnote.messenger.db.model.entity.DialogEntity;
import biz.dealnote.messenger.db.model.entity.DocumentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.LinkEntity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.db.model.entity.NewsEntity;
import biz.dealnote.messenger.db.model.entity.PageEntity;
import biz.dealnote.messenger.db.model.entity.PhotoAlbumEntity;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.db.model.entity.PhotoSizeEntity;
import biz.dealnote.messenger.db.model.entity.PollEntity;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.db.model.entity.PrivacyEntity;
import biz.dealnote.messenger.db.model.entity.StickerEntity;
import biz.dealnote.messenger.db.model.entity.TopicEntity;
import biz.dealnote.messenger.db.model.entity.UserDetailsEntity;
import biz.dealnote.messenger.db.model.entity.UserEntity;
import biz.dealnote.messenger.db.model.entity.VideoAlbumEntity;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Attachments;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.Comment;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.CryptStatus;
import biz.dealnote.messenger.model.Dialog;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.IOwnersBundle;
import biz.dealnote.messenger.model.IdPair;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.PhotoSizes;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.PostSource;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.Topic;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserDetails;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VideoAlbum;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.model.WikiPage;
import biz.dealnote.messenger.util.VKOwnIds;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class Entity2Model {

    public static VideoAlbum buildVideoAlbumFromDbo(VideoAlbumEntity dbo){
        return new VideoAlbum(dbo.getId(), dbo.getOwnerId())
                .setTitle(dbo.getTitle())
                .setCount(dbo.getCount())
                .setPrivacy(nonNull(dbo.getPrivacy()) ? buildPrivacyFromDbo(dbo.getPrivacy()) : null)
                .setPhoto160(dbo.getPhoto160())
                .setPhoto320(dbo.getPhoto320())
                .setUpdatedTime(dbo.getUpdateTime());
    }

    public static Topic buildTopicFromDbo(TopicEntity dbo, IOwnersBundle owners){
        Topic topic = new Topic(dbo.getId(), dbo.getOwnerId())
                .setTitle(dbo.getTitle())
                .setCreationTime(dbo.getCreatedTime())
                .setCreatedByOwnerId(dbo.getCreatorId())
                .setLastUpdateTime(dbo.getLastUpdateTime())
                .setUpdatedByOwnerId(dbo.getUpdatedBy())
                .setClosed(dbo.isClosed())
                .setFixed(dbo.isFixed())
                .setCommentsCount(dbo.getCommentsCount())
                .setFirstCommentBody(dbo.getFirstComment())
                .setLastCommentBody(dbo.getLastComment());

        if(dbo.getUpdatedBy() != 0){
            topic.setUpdater(owners.getById(dbo.getUpdatedBy()));
        }

        if(dbo.getCreatorId() != 0){
            topic.setCreator(owners.getById(dbo.getCreatorId()));
        }

        return topic;
    }

    public static List<Community> buildCommunitiesFromDbos(List<CommunityEntity> dbos){
        List<Community> communities = new ArrayList<>(dbos.size());
        for(CommunityEntity dbo : dbos){
            communities.add(buildCommunityFromDbo(dbo));
        }

        return communities;
    }

    public static Community buildCommunityFromDbo(CommunityEntity dbo){
        return new Community(dbo.getId())
                .setName(dbo.getName())
                .setScreenName(dbo.getScreenName())
                .setClosed(dbo.getClosed())
                .setAdmin(dbo.isAdmin())
                .setAdminLevel(dbo.getAdminLevel())
                .setMember(dbo.isMember())
                .setMemberStatus(dbo.getMemberStatus())
                .setType(dbo.getType())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200());
    }

    public static List<User> buildUsersFromDbo(List<UserEntity> dbos){
        List<User> users = new ArrayList<>(dbos.size());
        for(UserEntity dbo : dbos){
            users.add(buildUserFromDbo(dbo));
        }

        return users;
    }

    public static UserDetails buildUserDetailsFromDbo(UserDetailsEntity dbo){
        return new UserDetails()
                .setPhotoId(nonNull(dbo.getPhotoId()) ? new IdPair(dbo.getPhotoId().getId(), dbo.getPhotoId().getOwnerId()) : null)
                .setStatusAudio(nonNull(dbo.getStatusAudio()) ? buildAudioFromDbo(dbo.getStatusAudio()) : null)
                .setFriendsCount(dbo.getFriendsCount())
                .setOnlineFriendsCount(dbo.getOnlineFriendsCount())
                .setMutualFriendsCount(dbo.getMutualFriendsCount())
                .setFollowersCount(dbo.getFollowersCount())
                .setGroupsCount(dbo.getGroupsCount())
                .setPhotosCount(dbo.getPhotosCount())
                .setAudiosCount(dbo.getAudiosCount())
                .setVideosCount(dbo.getVideosCount())
                .setAllWallCount(dbo.getAllWallCount())
                .setOwnWallCount(dbo.getOwnWallCount())
                .setPostponedWallCount(dbo.getPostponedWallCount());
    }

    public static User buildUserFromDbo(UserEntity dbo){
        return new User(dbo.getId())
                .setFirstName(dbo.getFirstName())
                .setLastName(dbo.getLastName())
                .setOnline(dbo.isOnline())
                .setOnlineMobile(dbo.isOnlineMobile())
                .setOnlineApp(dbo.getOnlineApp())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200())
                .setLastSeen(dbo.getLastSeen())
                .setPlatform(dbo.getPlatform())
                .setStatus(dbo.getStatus())
                .setSex(dbo.getSex())
                .setDomain(dbo.getDomain())
                .setFriend(dbo.isFriend())
                .setFriendStatus(dbo.getFriendStatus());
    }

    public static PhotoAlbum buildPhotoAlbumFromDbo(PhotoAlbumEntity dbo){
        return new PhotoAlbum(dbo.getId(), dbo.getOwnerId())
                .setSize(dbo.getSize())
                .setTitle(dbo.getTitle())
                .setDescription(dbo.getDescription())
                .setCanUpload(dbo.isCanUpload())
                .setUpdatedTime(dbo.getUpdatedTime())
                .setCreatedTime(dbo.getCreatedTime())
                .setSizes(nonNull(dbo.getSizes()) ? buildPhotoSizesFromDbo(dbo.getSizes()) : PhotoSizes.empty())
                .setPrivacyView(nonNull(dbo.getPrivacyView()) ? buildPrivacyFromDbo(dbo.getPrivacyView()) : null)
                .setPrivacyComment(nonNull(dbo.getPrivacyComment()) ? buildPrivacyFromDbo(dbo.getPrivacyComment()) : null)
                .setUploadByAdminsOnly(dbo.isUploadByAdminsOnly())
                .setCommentsDisabled(dbo.isCommentsDisabled());
    }

    public static Comment buildCommentFromDbo(CommentEntity dbo, IOwnersBundle owners){
        Attachments attachments = buildAttachmentsFromDbos(dbo.getAttachments(), owners);

        return new Comment(new Commented(dbo.getSourceId(), dbo.getSourceOwnerId(), dbo.getSourceType(), dbo.getSourceAccessKey()))
                .setId(dbo.getId())
                .setFromId(dbo.getFromId())
                .setDate(dbo.getDate())
                .setText(dbo.getText())
                .setReplyToUser(dbo.getReplyToUserId())
                .setReplyToComment(dbo.getReplyToComment())
                .setLikesCount(dbo.getLikesCount())
                .setUserLikes(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanEdit(dbo.isCanEdit())
                .setAttachments(attachments)
                .setAuthor(owners.getById(dbo.getFromId()))
                .setDeleted(dbo.isDeleted());
    }

    public static Dialog buildDialogFromDbo(int accountId, DialogEntity dbo, IOwnersBundle owners) {
        Message message = buildMessageFromDbo(accountId, dbo.getMessage(), owners);

        Dialog dialog = new Dialog()
                .setAdminId(dbo.getAdminId())
                .setLastMessageId(dbo.getLastMessageId())
                .setPeerId(dbo.getPeerId())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200())
                .setTitle(dbo.getTitle())
                .setMessage(message)
                .setUnreadCount(dbo.getUnreadCount());

        switch (Peer.getType(dbo.getPeerId())) {
            case Peer.GROUP:
            case Peer.USER:
                dialog.setInterlocutor(owners.getById(dialog.getPeerId()));
                break;
            case Peer.CHAT:
                dialog.setInterlocutor(owners.getById(message.getSenderId()));
                break;
            default:
                throw new IllegalArgumentException("Invalid peer_id");
        }

        return dialog;
    }

    public static Message buildMessageFromDbo(int accountId, MessageEntity dbo, IOwnersBundle owners) {
        Message message = new Message(dbo.getId())
                .setAccountId(accountId)
                .setBody(dbo.getBody())
                .setTitle(dbo.getTitle())
                .setPeerId(dbo.getPeerId())
                .setSenderId(dbo.getFromId())
                .setRead(dbo.isRead())
                .setOut(dbo.isOut())
                .setStatus(dbo.getStatus())
                .setDate(dbo.getDate())
                .setHasAttachments(dbo.isHasAttachmens())
                .setForwardMessagesCount(dbo.getForwardCount())
                .setDeleted(dbo.isDeleted())
                .setOriginalId(dbo.getOriginalId())
                .setCryptStatus(dbo.isEncrypted() ? CryptStatus.ENCRYPTED : CryptStatus.NO_ENCRYPTION)
                .setImportant(dbo.isImportant())
                .setChatActive(dbo.getChatActive())
                .setUsersCount(dbo.getUsersCount())
                .setAdminId(dbo.getAdminId())
                .setAction(dbo.getAction())
                .setActionMid(dbo.getActionMemberId())
                .setActionEmail(dbo.getActionEmail())
                .setActionText(dbo.getActionText())
                .setPhoto50(dbo.getPhoto50())
                .setPhoto100(dbo.getPhoto100())
                .setPhoto200(dbo.getPhoto200())
                .setSender(owners.getById(dbo.getFromId()))
                .setRandomId(dbo.getRandomId());

        if (dbo.getActionMemberId() != 0) {
            User actionUser = (User) owners.getById(dbo.getActionMemberId());
            message.setActionUser(actionUser);
        }

        if (nonEmpty(dbo.getAttachments())) {
            message.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));
        }

        if (nonEmpty(dbo.getForwardMessages())) {
            for (MessageEntity fwdDbo : dbo.getForwardMessages()) {
                message.prepareFwd(dbo.getForwardMessages().size()).add(buildMessageFromDbo(accountId, fwdDbo, owners));
            }
        }

        return message;
    }

    public static Attachments buildAttachmentsFromDbos(List<Entity> entities, IOwnersBundle owners) {
        Attachments attachments = new Attachments();

        for (Entity entity : entities) {
            attachments.add(buildAttachmentFromDbo(entity, owners));
        }

        return attachments;
    }

    public static AbsModel buildAttachmentFromDbo(Entity entity, IOwnersBundle owners) {
        if (entity instanceof PhotoEntity) {
            return buildPhotoFromDbo((PhotoEntity) entity);
        }

        if (entity instanceof VideoEntity) {
            return buildVideoFromDbo((VideoEntity) entity);
        }

        if (entity instanceof PostEntity) {
            return buildPostFromDbo((PostEntity) entity, owners);
        }

        if (entity instanceof LinkEntity) {
            return buildLinkFromDbo((LinkEntity) entity);
        }

        if (entity instanceof PollEntity) {
            return buildPollFromDbo((PollEntity) entity);
        }

        if (entity instanceof DocumentEntity) {
            return buildDocumentFromDbo((DocumentEntity) entity);
        }

        if (entity instanceof PageEntity) {
            return buildWikiPageFromDbo((PageEntity) entity);
        }

        if (entity instanceof StickerEntity) {
            return buildStickerFromDbo((StickerEntity) entity);
        }

        if(entity instanceof AudioEntity){
            return buildAudioFromDbo((AudioEntity) entity);
        }

        if(entity instanceof TopicEntity){
            return buildTopicFromDbo((TopicEntity) entity, owners);
        }

        throw new UnsupportedOperationException("Unsupported DBO class: " + entity.getClass());
    }

    public static Audio buildAudioFromDbo(AudioEntity dbo){
        return new Audio()
                .setAccessKey(dbo.getAccessKey())
                .setAlbumId(dbo.getAlbumId())
                .setArtist(dbo.getArtist())
                .setDeleted(dbo.isDeleted())
                .setDuration(dbo.getDuration())
                .setUrl(dbo.getUrl())
                .setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setLyricsId(dbo.getLyricsId())
                .setTitle(dbo.getTitle())
                .setGenre(dbo.getGenre());
    }

    public static Sticker buildStickerFromDbo(StickerEntity dbo) {
        return new Sticker(dbo.getId())
                .setHeight(dbo.getHeight())
                .setWidth(dbo.getWidth());
    }

    public static WikiPage buildWikiPageFromDbo(PageEntity dbo) {
        return new WikiPage(dbo.getId(), dbo.getOwnerId())
                .setCreatorId(dbo.getCreatorId())
                .setTitle(dbo.getTitle())
                .setSource(dbo.getSource())
                .setEditionTime(dbo.getEditionTime())
                .setCreationTime(dbo.getCreationTime())
                .setParent(dbo.getParent())
                .setParent2(dbo.getParent2())
                .setViews(dbo.getViews())
                .setViewUrl(dbo.getViewUrl());
    }

    public static Document buildDocumentFromDbo(DocumentEntity dbo) {
        boolean isVoiceMessage = nonNull(dbo.getAudio());

        Document document = isVoiceMessage ? new VoiceMessage(dbo.getId(), dbo.getOwnerId()) : new Document(dbo.getId(), dbo.getOwnerId());

        document.setTitle(dbo.getTitle())
                .setSize(dbo.getSize())
                .setExt(dbo.getExt())
                .setUrl(dbo.getUrl())
                .setAccessKey(dbo.getAccessKey())
                .setDate(dbo.getDate())
                .setType(dbo.getType());

        if (document instanceof VoiceMessage) {
            ((VoiceMessage) document)
                    .setDuration(dbo.getAudio().getDuration())
                    .setWaveform(dbo.getAudio().getWaveform())
                    .setLinkOgg(dbo.getAudio().getLinkOgg())
                    .setLinkMp3(dbo.getAudio().getLinkMp3());
        }

        if (nonNull(dbo.getPhoto())) {
            document.setPhotoPreview(buildPhotoSizesFromDbo(dbo.getPhoto()));
        }

        if (nonNull(dbo.getVideo())) {
            document.setVideoPreview(new Document.VideoPreview()
                    .setWidth(dbo.getVideo().getWidth())
                    .setHeight(dbo.getVideo().getHeight())
                    .setSrc(dbo.getVideo().getSrc()));
        }

        if (nonNull(dbo.getGraffiti())) {
            document.setGraffiti(new Document.Graffiti()
                    .setHeight(dbo.getGraffiti().getHeight())
                    .setWidth(dbo.getGraffiti().getWidth())
                    .setSrc(dbo.getGraffiti().getSrc()));
        }

        return document;
    }

    public static Poll buildPollFromDbo(PollEntity dbo) {
        List<Poll.Answer> answers = new ArrayList<>(safeCountOf(dbo.getAnswers()));
        if (nonNull(dbo.getAnswers())) {
            for (PollEntity.AnswerDbo answer : dbo.getAnswers()) {
                answers.add(new Poll.Answer(answer.getId())
                        .setRate(answer.getRate())
                        .setText(answer.getText())
                        .setVoteCount(answer.getVoteCount()));
            }
        }

        return new Poll(dbo.getId(), dbo.getOwnerId())
                .setAnonymous(dbo.isAnonymous())
                .setAnswers(answers)
                .setBoard(dbo.isBoard())
                .setCreationTime(dbo.getCreationTime())
                .setMyAnswerId(dbo.getMyAnswerId())
                .setQuestion(dbo.getQuestion())
                .setVoteCount(dbo.getVoteCount());
    }

    public static Link buildLinkFromDbo(LinkEntity dbo) {
        return new Link()
                .setUrl(dbo.getUrl())
                .setTitle(dbo.getTitle())
                .setCaption(dbo.getCaption())
                .setDescription(dbo.getDescription())
                .setPhoto(nonNull(dbo.getPhoto()) ? buildPhotoFromDbo(dbo.getPhoto()) : null);
    }

    public static News buildNewsFromDbo(NewsEntity dbo, IOwnersBundle owners){
        News news = new News()
                .setType(dbo.getType())
                .setSourceId(dbo.getSourceId())
                .setSource(owners.getById(dbo.getSourceId()))
                .setPostType(dbo.getPostType())
                .setFinalPost(dbo.isFinalPost())
                .setCopyOwnerId(dbo.getCopyOwnerId())
                .setCopyPostId(dbo.getCopyPostId())
                .setCopyPostDate(dbo.getCopyPostDate())
                .setDate(dbo.getDate())
                .setPostId(dbo.getPostId())
                .setText(dbo.getText())
                .setCanEdit(dbo.isCanEdit())
                .setCanDelete(dbo.isCanDelete())
                .setCommentCount(dbo.getCommentCount())
                .setCommentCanPost(dbo.isCanPostComment())
                .setLikeCount(dbo.getLikesCount())
                .setUserLike(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanPublish(dbo.isCanPublish())
                .setRepostsCount(dbo.getRepostCount())
                .setUserReposted(dbo.isUserReposted())
                .setFriends(dbo.getFriendsTags())
                .setViewCount(dbo.getViews());

        if(nonEmpty(dbo.getAttachments())){
            news.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));
        } else {
            news.setAttachments(new Attachments());
        }

        if(nonEmpty(dbo.getCopyHistory())){
            List<Post> copies = new ArrayList<>(dbo.getCopyHistory().size());
            for(PostEntity copyDbo : dbo.getCopyHistory()){
                copies.add(buildPostFromDbo(copyDbo, owners));
            }

            news.setCopyHistory(copies);
        } else {
            news.setCopyHistory(Collections.emptyList());
        }

        return news;
    }

    public static Post buildPostFromDbo(PostEntity dbo, IOwnersBundle owners) {
        Post post = new Post()
                .setDbid(dbo.getDbid())
                .setVkid(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setAuthorId(dbo.getFromId())
                .setDate(dbo.getDate())
                .setText(dbo.getText())
                .setReplyOwnerId(dbo.getReplyOwnerId())
                .setReplyPostId(dbo.getReplyPostId())
                .setFriendsOnly(dbo.isFriendsOnly())
                .setCommentsCount(dbo.getCommentsCount())
                .setCanPostComment(dbo.isCanPostComment())
                .setLikesCount(dbo.getLikesCount())
                .setUserLikes(dbo.isUserLikes())
                .setCanLike(dbo.isCanLike())
                .setCanRepost(dbo.isCanPublish())
                .setRepostCount(dbo.getRepostCount())
                .setUserReposted(dbo.isUserReposted())
                .setPostType(dbo.getPostType())
                .setSignerId(dbo.getSignedId())
                .setCreatorId(dbo.getCreatedBy())
                .setCanEdit(dbo.isCanEdit())
                .setCanPin(dbo.isCanPin())
                .setPinned(dbo.isPinned())
                .setViewCount(dbo.getViews());

        PostEntity.SourceDbo sourceDbo = dbo.getSource();
        if(nonNull(sourceDbo)){
            post.setSource(new PostSource(sourceDbo.getType(), sourceDbo.getPlatform(), sourceDbo.getData(), sourceDbo.getUrl()));
        }

        post.setAttachments(buildAttachmentsFromDbos(dbo.getAttachments(), owners));

        if (nonEmpty(dbo.getCopyHierarchy())) {
            int copyCount = safeCountOf(dbo.getCopyHierarchy());

            for (PostEntity copyDbo : dbo.getCopyHierarchy()) {
                post.prepareCopyHierarchy(copyCount).add(buildPostFromDbo(copyDbo, owners));
            }
        }

        Dto2Model.fillPostOwners(post, owners);

        if (post.hasCopyHierarchy()) {
            for (Post copy : post.getCopyHierarchy()) {
                Dto2Model.fillPostOwners(copy, owners);
            }
        }

        return post;
    }

    public static SimplePrivacy buildPrivacyFromDbo(PrivacyEntity dbo) {
        ArrayList<SimplePrivacy.Entry> entries = new ArrayList<>(dbo.getEntries().length);

        for (PrivacyEntity.Entry entry : dbo.getEntries()) {
            entries.add(new SimplePrivacy.Entry(entry.getType(), entry.getId(), entry.isAllowed()));
        }

        return new SimplePrivacy(dbo.getType(), entries);
    }

    public static Video buildVideoFromDbo(VideoEntity dbo) {
        return new Video()
                .setId(dbo.getId())
                .setOwnerId(dbo.getOwnerId())
                .setAlbumId(dbo.getAlbumId())
                .setTitle(dbo.getTitle())
                .setDescription(dbo.getDescription())
                .setDuration(dbo.getDuration())
                .setLink(dbo.getLink())
                .setDate(dbo.getDate())
                .setAddingDate(dbo.getAddingDate())
                .setViews(dbo.getViews())
                .setPlayer(dbo.getPlayer())
                .setPhoto130(dbo.getPhoto130())
                .setPhoto320(dbo.getPhoto320())
                .setPhoto800(dbo.getPhoto800())
                .setAccessKey(dbo.getAccessKey())
                .setCommentsCount(dbo.getCommentsCount())
                .setCanComment(dbo.isCanComment())
                .setCanRepost(dbo.isCanRepost())
                .setUserLikes(dbo.isUserLikes())
                .setRepeat(dbo.isRepeat())
                .setLikesCount(dbo.getLikesCount())
                .setPrivacyView(nonNull(dbo.getPrivacyView()) ? buildPrivacyFromDbo(dbo.getPrivacyView()) : null)
                .setPrivacyComment(nonNull(dbo.getPrivacyComment()) ? buildPrivacyFromDbo(dbo.getPrivacyComment()) : null)
                .setMp4link240(dbo.getMp4link240())
                .setMp4link360(dbo.getMp4link360())
                .setMp4link480(dbo.getMp4link480())
                .setMp4link720(dbo.getMp4link720())
                .setMp4link1080(dbo.getMp4link1080())
                .setExternalLink(dbo.getExternalLink())
                .setPlatform(dbo.getPlatform())
                .setCanEdit(dbo.isCanEdit())
                .setCanAdd(dbo.isCanAdd());
    }

    public static Photo buildPhotoFromDbo(PhotoEntity dbo) {
        return new Photo()
                .setId(dbo.getId())
                .setAlbumId(dbo.getAlbumId())
                .setOwnerId(dbo.getOwnerId())
                .setWidth(dbo.getWidth())
                .setHeight(dbo.getHeight())
                .setText(dbo.getText())
                .setDate(dbo.getDate())
                .setUserLikes(dbo.isUserLikes())
                .setCanComment(dbo.isCanComment())
                .setLikesCount(dbo.getLikesCount())
                .setCommentsCount(dbo.getCommentsCount())
                .setTagsCount(dbo.getTagsCount())
                .setAccessKey(dbo.getAccessKey())
                .setDeleted(dbo.isDeleted())
                .setPostId(dbo.getPostId())
                .setSizes(nonNull(dbo.getSizes()) ? buildPhotoSizesFromDbo(dbo.getSizes()) : new PhotoSizes());
    }

    public static PhotoSizes buildPhotoSizesFromDbo(PhotoSizeEntity dbo) {
        return new PhotoSizes()
                .setS(dbo.getS())
                .setM(dbo.getM())
                .setX(dbo.getX())
                .setO(dbo.getO())
                .setP(dbo.getP())
                .setQ(dbo.getQ())
                .setR(dbo.getR())
                .setY(dbo.getY())
                .setZ(dbo.getZ())
                .setW(dbo.getW());
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable List<? extends Entity> dbos) {
        if(nonNull(dbos)){
            for (Entity entity : dbos) {
                fillOwnerIds(ids, entity);
            }
        }
    }

    public static void fillPostOwnerIds(@NonNull VKOwnIds ids, @Nullable PostEntity dbo) {
        if(nonNull(dbo)){
            ids.append(dbo.getFromId());
            ids.append(dbo.getSignedId());
            ids.append(dbo.getCreatedBy());

            fillOwnerIds(ids, dbo.getAttachments());
            fillOwnerIds(ids, dbo.getCopyHierarchy());
        }
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, CommentEntity entity){
        fillCommentOwnerIds(ids, entity);
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable Entity entity) {
        if (entity instanceof MessageEntity) {
            fillMessageOwnerIds(ids, (MessageEntity) entity);
        } else if (entity instanceof PostEntity) {
            fillPostOwnerIds(ids, (PostEntity) entity);
        }
    }

    public static void fillCommentOwnerIds(@NonNull VKOwnIds ids, @Nullable CommentEntity dbo){
        if(nonNull(dbo)){
            ids.append(dbo.getFromId());
            ids.append(dbo.getReplyToUserId());

            if(nonNull(dbo.getAttachments())){
                fillOwnerIds(ids, dbo.getAttachments());
            }
        }
    }

    public static void fillOwnerIds(@NonNull VKOwnIds ids, @Nullable NewsEntity dbo){
        if(nonNull(dbo)){
            ids.append(dbo.getSourceId());

            fillOwnerIds(ids, dbo.getAttachments());
            fillOwnerIds(ids, dbo.getCopyHistory());
        }
    }

    public static void fillMessageOwnerIds(@NonNull VKOwnIds ids, @Nullable MessageEntity dbo) {
        if(isNull(dbo)){
            return;
        }

        ids.append(dbo.getFromId());
        ids.append(dbo.getActionMemberId()); // тут 100% пользователь, нюанс в том, что он может быть < 0, если email

        if (!Peer.isGroupChat(dbo.getPeerId())) {
            ids.append(dbo.getPeerId());
        }

        if (nonEmpty(dbo.getForwardMessages())) {
            for (MessageEntity fwd : dbo.getForwardMessages()) {
                fillMessageOwnerIds(ids, fwd);
            }
        }

        if (nonEmpty(dbo.getAttachments())) {
            for (Entity attachmentEntity : dbo.getAttachments()) {
                fillOwnerIds(ids, attachmentEntity);
            }
        }
    }
}
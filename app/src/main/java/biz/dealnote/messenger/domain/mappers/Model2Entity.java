package biz.dealnote.messenger.domain.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.db.model.entity.AudioEntity;
import biz.dealnote.messenger.db.model.entity.DocumentEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.LinkEntity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;
import biz.dealnote.messenger.db.model.entity.PageEntity;
import biz.dealnote.messenger.db.model.entity.PhotoEntity;
import biz.dealnote.messenger.db.model.entity.PhotoSizeEntity;
import biz.dealnote.messenger.db.model.entity.PollEntity;
import biz.dealnote.messenger.db.model.entity.PostEntity;
import biz.dealnote.messenger.db.model.entity.PrivacyEntity;
import biz.dealnote.messenger.db.model.entity.StickerEntity;
import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Attachments;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.CryptStatus;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSizes;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.PostSource;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.model.Sticker;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.model.WikiPage;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class Model2Entity {

    public static MessageEntity buildMessageDbo(Message message){
        MessageEntity dbo = new MessageEntity(message.getId(), message.getPeerId(), message.getSenderId())
                .setDate(message.getDate())
                .setRead(message.isRead())
                .setOut(message.isOut())
                .setTitle(message.getTitle())
                .setBody(message.getBody())
                .setEncrypted(message.getCryptStatus() != CryptStatus.NO_ENCRYPTION)
                .setImportant(message.isImportant())
                .setDeleted(message.isDeleted())
                .setForwardCount(message.getForwardMessagesCount())
                .setHasAttachmens(message.isHasAttachments())
                .setStatus(message.getStatus())
                .setOriginalId(message.getOriginalId())
                .setChatActive(message.getChatActive())
                .setUsersCount(message.getUsersCount())
                .setAdminId(message.getAdminId())
                .setAction(message.getAction())
                .setActionMemberId(message.getActionMid())
                .setActionEmail(message.getActionEmail())
                .setActionText(message.getActionText())
                .setPhoto50(message.getPhoto50())
                .setPhoto100(message.getPhoto100())
                .setPhoto200(message.getPhoto200())
                .setRandomId(message.getRandomId())
                .setExtras(message.getExtras())
                .setAttachments(nonNull(message.getAttachments()) ? buildDboAttachments(message.getAttachments()) : null);

        if(nonEmpty(message.getFwd())){
            if(message.getFwd().size() == 1){
                dbo.setForwardMessages(Collections.singletonList(buildMessageDbo(message.getFwd().get(0))));
            } else {
                List<MessageEntity> forwardDbos = new ArrayList<>(message.getFwd().size());
                for(Message fwd : message.getFwd()){
                    forwardDbos.add(buildMessageDbo(fwd));
                }

                dbo.setForwardMessages(forwardDbos);
            }
        }

        return dbo;
    }

    public static List<Entity> buildDboAttachments(Attachments attachments){
        List<Entity> entities = new ArrayList<>(attachments.size());

        if(nonEmpty(attachments.getAudios())){
            for(Audio audio : attachments.getAudios()){
                entities.add(buildAudioDbo(audio));
            }
        }

        if(nonEmpty(attachments.getStickers())){
            for(Sticker sticker : attachments.getStickers()){
                entities.add(buildStickerDbo(sticker));
            }
        }

        if(nonEmpty(attachments.getPhotos())){
            for(Photo photo : attachments.getPhotos()){
                entities.add(buildPhotoDbo(photo));
            }
        }

        if(nonEmpty(attachments.getDocs())){
            for(Document document : attachments.getDocs()){
                entities.add(buildDocumentDbo(document));
            }
        }

        if(nonEmpty(attachments.getVoiceMessages())){
            for(VoiceMessage message : attachments.getVoiceMessages()){
                entities.add(buildDocumentDbo(message));
            }
        }

        if(nonEmpty(attachments.getVideos())){
            for(Video video : attachments.getVideos()){
                entities.add(buildVideoDbo(video));
            }
        }

        if(nonEmpty(attachments.getPosts())){
            for(Post post : attachments.getPosts()){
                entities.add(buildPostDbo(post));
            }
        }

        if(nonEmpty(attachments.getLinks())){
            for(Link link : attachments.getLinks()){
                entities.add(buildLinkDbo(link));
            }
        }

        if(nonEmpty(attachments.getPolls())){
            for(Poll poll : attachments.getPolls()){
                entities.add(buildPollDbo(poll));
            }
        }

        if(nonEmpty(attachments.getPages())){
            for(WikiPage page : attachments.getPages()){
                entities.add(buildPageDbo(page));
            }
        }

        return entities;
    }

    public static List<Entity> buildDboAttachments(List<? extends AbsModel> models){
        List<Entity> entities = new ArrayList<>(models.size());

        for(AbsModel model : models){
            if(model instanceof Audio){
                entities.add(buildAudioDbo((Audio) model));
            } else if(model instanceof Sticker){
                entities.add(buildStickerDbo((Sticker) model));
            } else if(model instanceof Photo){
                entities.add(buildPhotoDbo((Photo) model));
            } else if(model instanceof Document){
                entities.add(buildDocumentDbo((Document) model));
            } else if(model instanceof Video){
                entities.add(buildVideoDbo((Video) model));
            } else if(model instanceof Post){
                entities.add(buildPostDbo((Post) model));
            } else if(model instanceof Link){
                entities.add(buildLinkDbo((Link) model));
            } else if(model instanceof Poll){
                entities.add(buildPollDbo((Poll) model));
            } else if(model instanceof WikiPage){
                entities.add(buildPageDbo((WikiPage) model));
            } else {
                throw new UnsupportedOperationException("Unsupported model");
            }
        }

        return entities;
    }

    public static PageEntity buildPageDbo(WikiPage page){
        return new PageEntity(page.getId(), page.getOwnerId())
                .setViewUrl(page.getViewUrl())
                .setViews(page.getViews())
                .setParent2(page.getParent2())
                .setParent(page.getParent())
                .setCreationTime(page.getCreationTime())
                .setEditionTime(page.getEditionTime())
                .setCreatorId(page.getCreatorId())
                .setSource(page.getSource());
    }

    public static PollEntity buildPollDbo(Poll poll){
        List<PollEntity.AnswerDbo> answerDbos = new ArrayList<>(safeCountOf(poll.getAnswers()));

        if(nonEmpty(poll.getAnswers())){
            for(Poll.Answer answer : poll.getAnswers()){
                answerDbos.add(new PollEntity.AnswerDbo(answer.getId(), answer.getText(), answer.getVoteCount(), answer.getRate()));
            }
        }

        return new PollEntity(poll.getId(), poll.getOwnerId())
                .setAnswers(answerDbos)
                .setQuestion(poll.getQuestion())
                .setVoteCount(poll.getVoteCount())
                .setMyAnswerId(poll.getMyAnswerId())
                .setCreationTime(poll.getCreationTime())
                .setAnonymous(poll.isAnonymous())
                .setBoard(poll.isBoard());
    }

    public static LinkEntity buildLinkDbo(Link link){
        return new LinkEntity(link.getUrl())
                .setPhoto(isNull(link.getPhoto()) ? null : buildPhotoDbo(link.getPhoto()))
                .setTitle(link.getTitle())
                .setDescription(link.getDescription())
                .setCaption(link.getCaption());
    }

    public static PostEntity buildPostDbo(Post post){
        PostEntity dbo = new PostEntity(post.getVkid(), post.getOwnerId())
                .setFromId(post.getAuthorId())
                .setDate(post.getDate())
                .setText(post.getText())
                .setReplyOwnerId(post.getReplyOwnerId())
                .setReplyPostId(post.getReplyPostId())
                .setFriendsOnly(post.isFriendsOnly())
                .setCommentsCount(post.getCommentsCount())
                .setCanPostComment(post.isCanPostComment())
                .setLikesCount(post.getLikesCount())
                .setUserLikes(post.isUserLikes())
                .setCanLike(post.isCanLike())
                .setCanEdit(post.isCanEdit())
                .setCanPublish(post.isCanRepost())
                .setRepostCount(post.getRepostCount())
                .setUserReposted(post.isUserReposted())
                .setPostType(post.getPostType())
                .setAttachmentsCount(nonNull(post.getAttachments()) ? post.getAttachments().size() : 0)
                .setSignedId(post.getSignerId())
                .setCreatedBy(post.getCreatorId())
                .setCanPin(post.isCanPin())
                .setPinned(post.isPinned())
                .setDeleted(post.isDeleted())
                .setViews(post.getViewCount())
                .setDbid(post.getDbid());

        PostSource source = post.getSource();
        if(nonNull(source)){
            dbo.setSource(new PostEntity.SourceDbo(source.getType(), source.getPlatform(), source.getData(), source.getUrl()));
        }

        if(nonNull(post.getAttachments())){
            dbo.setAttachments(buildDboAttachments(post.getAttachments()));
        } else {
            dbo.setAttachments(Collections.emptyList());
        }

        List<Post> copies = post.getCopyHierarchy();
        if(nonEmpty(copies)){
            if(copies.size() == 1){
                dbo.setCopyHierarchy(Collections.singletonList(buildPostDbo(copies.get(0))));
            } else {
                List<PostEntity> copyDbos = new ArrayList<>(copies.size());
                for(Post copy : copies){
                    copyDbos.add(buildPostDbo(copy));
                }

                dbo.setCopyHierarchy(copyDbos);
            }
        } else {
            dbo.setCopyHierarchy(Collections.emptyList());
        }

        return dbo;
    }

    public static VideoEntity buildVideoDbo(Video video){
        return new VideoEntity(video.getId(), video.getOwnerId())
                .setAlbumId(video.getAlbumId())
                .setTitle(video.getTitle())
                .setDescription(video.getDescription())
                .setLink(video.getLink())
                .setDate(video.getDate())
                .setAddingDate(video.getAddingDate())
                .setViews(video.getViews())
                .setPlayer(video.getPlayer())
                .setPhoto130(video.getPhoto130())
                .setPhoto320(video.getPhoto320())
                .setPhoto800(video.getPhoto800())
                .setAccessKey(video.getAccessKey())
                .setCommentsCount(video.getCommentsCount())
                .setUserLikes(video.isUserLikes())
                .setLikesCount(video.getLikesCount())
                .setMp4link240(video.getMp4link240())
                .setMp4link360(video.getMp4link360())
                .setMp4link480(video.getMp4link480())
                .setMp4link720(video.getMp4link720())
                .setMp4link1080(video.getMp4link1080())
                .setExternalLink(video.getExternalLink())
                .setPlatform(video.getPlatform())
                .setRepeat(video.isRepeat())
                .setDuration(video.getDuration())
                .setPrivacyView(isNull(video.getPrivacyView()) ? null : buildPrivacyDbo(video.getPrivacyView()))
                .setPrivacyComment(isNull(video.getPrivacyComment()) ? null : buildPrivacyDbo(video.getPrivacyComment()))
                .setCanEdit(video.isCanEdit())
                .setCanAdd(video.isCanAdd())
                .setCanComment(video.isCanComment())
                .setCanRepost(video.isCanRepost());
    }

    public static PrivacyEntity buildPrivacyDbo(SimplePrivacy privacy){
        List<SimplePrivacy.Entry> entries = privacy.getEntries();
        PrivacyEntity.Entry[] entryDbos = new PrivacyEntity.Entry[(safeCountOf(entries))];

        if(nonNull(entries)){
            for(int i = 0; i < entries.size(); i++){
                SimplePrivacy.Entry entry = entries.get(i);
                entryDbos[i] = new PrivacyEntity.Entry(entry.getType(), entry.getId(), entry.isAllowed());
            }
        }

        return new PrivacyEntity(privacy.getType(), entryDbos);
    }

    public static DocumentEntity buildDocumentDbo(Document document){
        DocumentEntity dbo = new DocumentEntity(document.getId(), document.getOwnerId())
                .setTitle(document.getTitle())
                .setSize(document.getSize())
                .setExt(document.getExt())
                .setUrl(document.getUrl())
                .setDate(document.getDate())
                .setType(document.getType())
                .setAccessKey(document.getAccessKey());

        if(document instanceof VoiceMessage){
            VoiceMessage message = (VoiceMessage) document;
            dbo.setAudio(new DocumentEntity.AudioMessageDbo(message.getDuration(), message.getWaveform(), message.getLinkOgg(), message.getLinkMp3()));
        }

        if(nonNull(document.getGraffiti())){
            Document.Graffiti graffiti = document.getGraffiti();
            dbo.setGraffiti(new DocumentEntity.GraffitiDbo(graffiti.getSrc(), graffiti.getWidth(), graffiti.getHeight()));
        }

        if(nonNull(document.getVideoPreview())){
            Document.VideoPreview video = document.getVideoPreview();
            dbo.setVideo(new DocumentEntity.VideoPreviewDbo(video.getSrc(), video.getWidth(), video.getHeight(), video.getFileSize()));
        }

        return dbo;
    }

    public static StickerEntity buildStickerDbo(Sticker sticker){
        return new StickerEntity(sticker.getId())
                .setWidth(sticker.getWidth())
                .setHeight(sticker.getHeight());
    }

    public static AudioEntity buildAudioDbo(Audio audio){
        return new AudioEntity(audio.getId(), audio.getOwnerId())
                .setArtist(audio.getArtist())
                .setTitle(audio.getTitle())
                .setDuration(audio.getDuration())
                .setUrl(audio.getUrl())
                .setLyricsId(audio.getLyricsId())
                .setAlbumId(audio.getAlbumId())
                .setGenre(audio.getGenre())
                .setAccessKey(audio.getAccessKey())
                .setDeleted(audio.isDeleted());
    }

    public static PhotoEntity buildPhotoDbo(Photo photo) {
        return new PhotoEntity(photo.getId(), photo.getOwnerId())
                .setAlbumId(photo.getAlbumId())
                .setWidth(photo.getWidth())
                .setHeight(photo.getHeight())
                .setText(photo.getText())
                .setDate(photo.getDate())
                .setUserLikes(photo.isUserLikes())
                .setCanComment(photo.isCanComment())
                .setLikesCount(photo.getLikesCount())
                .setCommentsCount(photo.getCommentsCount())
                .setTagsCount(photo.getTagsCount())
                .setAccessKey(photo.getAccessKey())
                .setPostId(photo.getPostId())
                .setDeleted(photo.isDeleted())
                .setSizes(isNull(photo.getSizes()) ? null : buildPhotoSizeDbo(photo.getSizes()));
    }

    public static PhotoSizeEntity buildPhotoSizeDbo(PhotoSizes sizes){
        return new PhotoSizeEntity()
                .setS(sizes.getS())
                .setM(sizes.getM())
                .setX(sizes.getX())
                .setO(sizes.getO())
                .setP(sizes.getP())
                .setQ(sizes.getQ())
                .setR(sizes.getR())
                .setY(sizes.getY())
                .setZ(sizes.getZ())
                .setW(sizes.getW());
    }
}
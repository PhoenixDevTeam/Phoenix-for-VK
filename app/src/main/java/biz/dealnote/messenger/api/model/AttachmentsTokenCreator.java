package biz.dealnote.messenger.api.model;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class AttachmentsTokenCreator {

    public static IAttachmentToken ofDocument(int id, int ownerId, String accessKey){
        return new AttachmentToken("doc", id, ownerId, accessKey);
    }

    public static IAttachmentToken ofAudio(int id, int ownerId, String accessKey){
        return new AttachmentToken("audio", id, ownerId, accessKey);
    }

    public static IAttachmentToken ofLink(String url){
        return new LinkAttachmentToken(url);
    }

    public static IAttachmentToken ofPhoto(int id, int ownerId, String accessKey){
        return new AttachmentToken("photo", id, ownerId, accessKey);
    }

    public static IAttachmentToken ofPoll(int id, int ownerId){
        return new AttachmentToken("poll", id, ownerId);
    }

    public static IAttachmentToken ofPost(int id, int ownerId){
        return new AttachmentToken("wall", id, ownerId);
    }

    public static IAttachmentToken ofVideo(int id, int ownerId, String accessKey){
        return new AttachmentToken("video", id, ownerId, accessKey);
    }
}
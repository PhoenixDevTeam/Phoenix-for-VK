package biz.dealnote.messenger.adapter;

import android.view.View;
import android.view.ViewGroup;

import biz.dealnote.messenger.R;

public class AttachmentsHolder {

    private ViewGroup vgAudios;
    private ViewGroup mVoiceMessageRoot;
    private ViewGroup vgDocs;
    private ViewGroup vgStickers;
    private ViewGroup vgPhotos;
    private ViewGroup vgPosts;
    private ViewGroup vgFriends;

    public static AttachmentsHolder forCopyPost(ViewGroup container){
        AttachmentsHolder containers = new AttachmentsHolder();
        containers.setVgStickers(container.findViewById(R.id.copy_history_stickers_attachments)).
                setVgPhotos(container.findViewById(R.id.copy_history_photo_attachments)).
                setVgAudios(container.findViewById(R.id.copy_history_audio_attachments)).
                setVgDocs(container.findViewById(R.id.copy_history_docs_attachments));
        return containers;
    }

    public static AttachmentsHolder forPost(ViewGroup container){
        AttachmentsHolder containers = new AttachmentsHolder();
        containers.setVgStickers(container.findViewById(R.id.post_stickers_attachments)).
                setVgPosts(container.findViewById(R.id.post_posts_attachments)).
                setVgPhotos(container.findViewById(R.id.post_photo_attachments)).
                setVgAudios(container.findViewById(R.id.post_audio_attachments)).
                setVgDocs(container.findViewById(R.id.post_docs_attachments)).
                setVgFriends(container.findViewById(R.id.post_friends_attachments));
        return containers;
    }

    public static AttachmentsHolder forComment(ViewGroup container){
        AttachmentsHolder containers = new AttachmentsHolder();
        containers.setVgStickers(container.findViewById(R.id.comments_stickers_attachments)).
                setVgPhotos(container.findViewById(R.id.comments_photo_attachments)).
                setVgAudios(container.findViewById(R.id.comments_audio_attachments)).
                setVgDocs(container.findViewById(R.id.comments_docs_attachments));
        return containers;
    }

    public static AttachmentsHolder forFeedback(View container){
        AttachmentsHolder containers = new AttachmentsHolder();
        containers.setVgStickers(container.findViewById(R.id.feedback_stickers_attachments)).
                setVgPhotos(container.findViewById(R.id.feedback_photo_attachments)).
                setVgAudios(container.findViewById(R.id.feedback_audio_attachments)).
                setVgDocs(container.findViewById(R.id.feedback_docs_attachments));
        return containers;
    }

    public ViewGroup getVgAudios() {
        return vgAudios;
    }

    public AttachmentsHolder setVgAudios(ViewGroup vgAudios) {
        this.vgAudios = vgAudios;
        return this;
    }

    public ViewGroup getVgDocs() {
        return vgDocs;
    }

    public AttachmentsHolder setVgDocs(ViewGroup vgDocs) {
        this.vgDocs = vgDocs;
        return this;
    }

    public ViewGroup getVgStickers() {
        return vgStickers;
    }

    public AttachmentsHolder setVgStickers(ViewGroup vgStickers) {
        this.vgStickers = vgStickers;
        return this;
    }

    public ViewGroup getVgPhotos() {
        return vgPhotos;
    }

    public AttachmentsHolder setVgPhotos(ViewGroup vgPhotos) {
        this.vgPhotos = vgPhotos;
        return this;
    }

    public ViewGroup getVgPosts() {
        return vgPosts;
    }

    public AttachmentsHolder setVgPosts(ViewGroup vgPosts) {
        this.vgPosts = vgPosts;
        return this;
    }

    public AttachmentsHolder setVoiceMessageRoot(ViewGroup voiceMessageRoot) {
        this.mVoiceMessageRoot = voiceMessageRoot;
        return this;
    }

    public ViewGroup getVoiceMessageRoot() {
        return mVoiceMessageRoot;
    }

    public ViewGroup getVgFriends(){
        return vgFriends;
    }

    public AttachmentsHolder setVgFriends(ViewGroup vgFriends){
        this.vgFriends = vgFriends;
        return this;
    }
}
package biz.dealnote.messenger.model;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.crypt.KeyLocationPolicy;

/**
 * Created by ruslan.kolbasa on 06.10.2016.
 * phoenix
 */
public class SaveMessageBuilder {

    private int accountId;
    private int peerId;

    private List<AbsModel> attachments;
    private List<Message> forwardMessages;
    private String body;
    private File voiceMessageFile;
    private boolean requireEncryption;

    private Integer draftMessageId;

    @KeyLocationPolicy
    private int keyLocationPolicy;

    public SaveMessageBuilder(int accountId, int peerId) {
        this.accountId = accountId;
        this.peerId = peerId;
        this.keyLocationPolicy = KeyLocationPolicy.PERSIST;
    }

    public SaveMessageBuilder setDraftMessageId(Integer draftMessageId) {
        this.draftMessageId = draftMessageId;
        return this;
    }

    public Integer getDraftMessageId() {
        return draftMessageId;
    }

    public SaveMessageBuilder attach(List<AbsModel> attachments) {
        if (attachments != null) {
            prepareAttachments(attachments.size()).addAll(attachments);
        }

        return this;
    }

    private List<AbsModel> prepareAttachments(int initialSize){
        if(attachments == null){
            attachments = new ArrayList<>(initialSize);
        }

        return attachments;
    }

    public SaveMessageBuilder setForwardMessages(List<Message> forwardMessages) {
        this.forwardMessages = forwardMessages;
        return this;
    }

    public SaveMessageBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public SaveMessageBuilder attach(@NonNull AbsModel attachment) {
        prepareAttachments(1).add(attachment);
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getPeerId() {
        return peerId;
    }

    public List<AbsModel> getAttachments() {
        return attachments;
    }

    public List<Message> getForwardMessages() {
        return forwardMessages;
    }

    public String getBody() {
        return body;
    }

    public File getVoiceMessageFile() {
        return voiceMessageFile;
    }

    public SaveMessageBuilder setVoiceMessageFile(File voiceMessageFile) {
        this.voiceMessageFile = voiceMessageFile;
        return this;
    }

    public boolean isRequireEncryption() {
        return requireEncryption;
    }

    public SaveMessageBuilder setRequireEncryption(boolean requireEncryption) {
        this.requireEncryption = requireEncryption;
        return this;
    }

    @KeyLocationPolicy
    public int getKeyLocationPolicy() {
        return keyLocationPolicy;
    }

    public SaveMessageBuilder setKeyLocationPolicy(int keyLocationPolicy) {
        this.keyLocationPolicy = keyLocationPolicy;
        return this;
    }
}

package biz.dealnote.messenger.db.model;

import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.MessageEntity;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class MessagePatch {

    private boolean encrypted;

    private long date;

    private boolean out;

    private boolean deleted;

    private boolean important;

    private List<MessageEntity> forward;

    private List<Entity> attachments;

    private final int status;

    private final int senderId;

    private boolean read;

    private String body;

    private Map<Integer, String> extras;

    private String title;

    public MessagePatch(int status, int senderId) {
        this.status = status;
        this.senderId = senderId;
    }

    public MessagePatch setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MessagePatch setRead(boolean read) {
        this.read = read;
        return this;
    }

    public MessagePatch setBody(String body) {
        this.body = body;
        return this;
    }

    public String getBody() {
        return body;
    }

    public MessagePatch setExtras(Map<Integer, String> extras) {
        this.extras = extras;
        return this;
    }

    public Map<Integer, String> getExtras() {
        return extras;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public MessagePatch setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    public long getDate() {
        return date;
    }

    public MessagePatch setDate(long date) {
        this.date = date;
        return this;
    }

    public boolean isOut() {
        return out;
    }

    public MessagePatch setOut(boolean out) {
        this.out = out;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public MessagePatch setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isImportant() {
        return important;
    }

    public MessagePatch setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public List<MessageEntity> getForward() {
        return forward;
    }

    public MessagePatch setForward(List<MessageEntity> forward) {
        this.forward = forward;
        return this;
    }

    public List<Entity> getAttachments() {
        return attachments;
    }

    public MessagePatch setAttachments(List<Entity> attachments) {
        this.attachments = attachments;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public int getSenderId() {
        return senderId;
    }
}
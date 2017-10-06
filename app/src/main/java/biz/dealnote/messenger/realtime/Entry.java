package biz.dealnote.messenger.realtime;

import biz.dealnote.messenger.api.model.longpoll.AddMessageUpdate;
import biz.dealnote.messenger.longpoll.FullAndNonFullUpdates;

import static biz.dealnote.messenger.util.Utils.safeCountOf;

/**
 * Created by admin on 11.04.2017.
 * phoenix
 */
public final class Entry {

    private final int id;
    private final int accountId;
    private final boolean ignoreIfExists;
    private final FullAndNonFullUpdates updates;

    public Entry(int accountId, int id, boolean ignoreIfExists) {
        this.id = id;
        this.accountId = accountId;
        this.ignoreIfExists = ignoreIfExists;
        this.updates = new FullAndNonFullUpdates();
    }

    public boolean has(int id){
        if(updates.hasNonFullMessages()){
            for(Integer nonFullId : updates.getNonFull()){
                if(id == nonFullId){
                    return true;
                }
            }
        }

        if(updates.hasFullMessages()){
            for(AddMessageUpdate update : updates.getFullMessages()){
                if(update.getMessageId() == id){
                    return true;
                }
            }
        }

        return false;
    }

    public int count(){
        return safeCountOf(updates.getFullMessages()) + safeCountOf(updates.getNonFull());
    }

    public boolean isIgnoreIfExists() {
        return ignoreIfExists;
    }

    public void append(AddMessageUpdate update){
        if(update.isFull()){
            updates.prepareFull().add(update);
        } else {
            updates.prepareNonFull().add(update.getMessageId());
        }
    }

    public void append(int messageId){
        updates.prepareNonFull().add(messageId);
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public FullAndNonFullUpdates getUpdates() {
        return updates;
    }
}
package biz.dealnote.messenger.model;

import androidx.annotation.NonNull;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class TopicWithOwner {

    private final Topic topic;

    private final Owner owner;

    public TopicWithOwner(Topic topic, Owner owner) {
        this.topic = topic;
        this.owner = owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Topic getTopic() {
        return topic;
    }
}
package biz.dealnote.messenger.model.feedback;

import java.util.List;

import biz.dealnote.messenger.model.Owner;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for [follow, friend_accepted, likes]
 */
public class UsersFeedback extends Feedback {

    private List<Owner> owners;

    public UsersFeedback(@FeedbackType int type) {
        super(type);
    }

    public UsersFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }

    public List<Owner> getOwners() {
        return owners;
    }
}

package biz.dealnote.messenger.model.feedback;

import java.util.List;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Owner;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [copy_post, copy_photo, copy_video]
 */
public class CopyFeedback extends Feedback {

    private AbsModel what;
    private List<Owner> owners;

    public CopyFeedback(@FeedbackType int type) {
        super(type);
    }

    public AbsModel getWhat() {
        return what;
    }

    public CopyFeedback setWhat(AbsModel what) {
        this.what = what;
        return this;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public CopyFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}
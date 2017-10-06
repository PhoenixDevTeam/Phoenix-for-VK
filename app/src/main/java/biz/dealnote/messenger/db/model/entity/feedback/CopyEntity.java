package biz.dealnote.messenger.db.model.entity.feedback;

import biz.dealnote.messenger.db.model.entity.CopiesEntity;
import biz.dealnote.messenger.db.model.entity.Entity;
import biz.dealnote.messenger.db.model.entity.EntityWrapper;

/**
 * Created by ruslan.kolbasa on 09.12.2016.
 * phoenix
 * base class for types [copy_post, copy_photo, copy_video]
 */
public class CopyEntity extends FeedbackEntity {

    private CopiesEntity copies;
    private EntityWrapper copied = EntityWrapper.empty();

    public CopyEntity(int type) {
        super(type);
    }

    public CopiesEntity getCopies() {
        return copies;
    }

    public Entity getCopied() {
        return copied.get();
    }

    public CopyEntity setCopied(Entity copied) {
        this.copied = new EntityWrapper(copied);
        return this;
    }

    public CopyEntity setCopies(CopiesEntity copies) {
        this.copies = copies;
        return this;
    }
}
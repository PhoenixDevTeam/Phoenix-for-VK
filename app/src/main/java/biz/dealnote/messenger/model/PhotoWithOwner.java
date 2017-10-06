package biz.dealnote.messenger.model;

import android.support.annotation.NonNull;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class PhotoWithOwner {

    private final Photo photo;

    private final Owner owner;

    public PhotoWithOwner(Photo photo, Owner owner) {
        this.photo = photo;
        this.owner = owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Photo getPhoto() {
        return photo;
    }
}

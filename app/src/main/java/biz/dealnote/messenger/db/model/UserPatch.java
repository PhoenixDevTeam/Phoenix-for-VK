package biz.dealnote.messenger.db.model;

/**
 * Created by Ruslan Kolbasa on 18.09.2017.
 * phoenix
 */
public class UserPatch {

    private StatusUpdate statusUpdate;

    public UserPatch setStatusUpdate(StatusUpdate statusUpdate) {
        this.statusUpdate = statusUpdate;
        return this;
    }

    public StatusUpdate getStatusUpdate() {
        return statusUpdate;
    }

    public static final class StatusUpdate {

        private final String status;

        public StatusUpdate(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
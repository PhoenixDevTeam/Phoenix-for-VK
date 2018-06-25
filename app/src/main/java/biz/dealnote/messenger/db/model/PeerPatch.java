package biz.dealnote.messenger.db.model;

public final class PeerPatch {

    private ReadTo inRead;

    private ReadTo outRead;

    private final int id;

    public PeerPatch(int id) {
        this.id = id;
    }

    public PeerPatch withInRead(int id, int unreadCount){
        this.inRead = new ReadTo(id, unreadCount);
        return this;
    }

    public PeerPatch withOutRead(int id, int unreadCount){
        this.outRead = new ReadTo(id, unreadCount);
        return this;
    }

    public int getId() {
        return id;
    }

    public ReadTo getInRead() {
        return inRead;
    }

    public ReadTo getOutRead() {
        return outRead;
    }

    public static final class ReadTo {

        private final int id;

        private final int unreadCount;

        private ReadTo(int id, int unreadCount) {
            this.id = id;
            this.unreadCount = unreadCount;
        }

        public int getId() {
            return id;
        }

        public int getUnreadCount() {
            return unreadCount;
        }
    }
}
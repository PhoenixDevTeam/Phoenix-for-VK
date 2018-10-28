package biz.dealnote.messenger.db.model.entity;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class PrivacyEntity {

    private final String type;

    private final Entry[] entries;

    public PrivacyEntity(String type, Entry[] entries) {
        this.type = type;
        this.entries = entries;
    }

    public static final class Entry {

        private final int type;

        private final int id;

        private final boolean allowed;

        public Entry(int type, int id, boolean allowed) {
            this.type = type;
            this.id = id;
            this.allowed = allowed;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public boolean isAllowed() {
            return allowed;
        }
    }

    public String getType() {
        return type;
    }

    public Entry[] getEntries() {
        return entries;
    }
}
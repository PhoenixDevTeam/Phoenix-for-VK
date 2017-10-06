package biz.dealnote.messenger.api.model;

import java.util.ArrayList;

import biz.dealnote.messenger.api.util.VKStringUtils;

public class VkApiPrivacy {

    public int type;
    public ArrayList<Entry> entries;

    public VkApiPrivacy() {
        this.type = Type.UNDEFINED;
        this.entries = new ArrayList<>();
    }

    public static class Type {
        public static final int ALL = 1;
        public static final int FRIENDS = 2;
        public static final int FRIENDS_OF_FRIENDS = 3;
        public static final int ONLY_ME = 4;
        public static final int UNDEFINED = 0;
    }

    private static Entry parseFriendsList(String str) {
        boolean exclude = str.startsWith("-");
        int idStart = str.indexOf("list") + 4;
        int id = Integer.parseInt(str.substring(idStart));
        return exclude ? Entry.excludedFriendsList(id) : Entry.includedFriendsList(id);
    }

    public void appendEntry(Entry entry){
        putIfNotExist(entry);
    }

    private void putIfNotExist(Entry entry) {
        if (!this.entries.contains(entry)) {
            this.entries.add(entry);
        }
    }

    public VkApiPrivacy parse(String text) {
        String[] jsonArray = text.split(", ");
        for (String value : jsonArray) {
            try {
                int tryUserId = Integer.parseInt(value);
                boolean exclude = tryUserId < 0;
                int uid = Math.abs(tryUserId);
                putIfNotExist(exclude ? Entry.excludedUser(uid) : Entry.includedUser(uid));
            } catch (NumberFormatException e) {
                if ("all".equals(value)) {
                    this.type = Type.ALL;
                } else if ("friends".equals(value)) {
                    this.type = Type.FRIENDS;
                } else if ("friends_of_friends".equals(value) || "friends_of_friends_only".equals(value)) {
                    this.type = Type.FRIENDS_OF_FRIENDS;
                } else if ("nobody".equals(value) || "only_me".equals(value)) {
                    this.type = Type.ONLY_ME;
                } else if (value.contains("list")) {
                    putIfNotExist(parseFriendsList(value));
                }
            }
        }

        // если нет типа, значит никому (пример [only_me, 32271297, 216143660] = [32271297, 216143660])
        if(this.type == Type.UNDEFINED){
            this.type = Type.ONLY_ME;
        }

        return this;
    }

    public String buildJsonArray() {
        String typeValue = getTypeValue(type);
        String entriesLine = VKStringUtils.join(", ", entries);

        ArrayList<String> option = new ArrayList<>();

        if (typeValue != null && !typeValue.isEmpty()) {
            option.add(typeValue);
        }

        if (!entriesLine.isEmpty()) {
            option.add(entriesLine);
        }

        return VKStringUtils.join(", ", option);
    }

    private static String getTypeValue(int type) {
        switch (type) {
            case Type.ALL:
                return "all";
            case Type.FRIENDS:
                return "friends";
            case Type.FRIENDS_OF_FRIENDS:
                return "friends_of_friends";
            case Type.ONLY_ME:
                return "only_me";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return buildJsonArray();
    }

    public VkApiPrivacy includeUser(int id) {
        putIfNotExist(Entry.includedUser(id));
        return this;
    }

    public VkApiPrivacy excludeUser(int id) {
        putIfNotExist(Entry.excludedUser(id));
        return this;
    }

    public VkApiPrivacy includeFriendsList(int id) {
        putIfNotExist(Entry.includedFriendsList(id));
        return this;
    }

    public VkApiPrivacy excludeFriendsList(int id) {
        putIfNotExist(Entry.excludedFriendsList(id));
        return this;
    }

    public VkApiPrivacy setType(int type) {
        this.type = type;
        return this;
    }

    public static class Entry {

        public static final int TYPE_USER = 1;
        public static final int TYPE_FRIENDS_LIST = 2;

        public int type;
        public int id;
        public boolean allowed;

        public static Entry excludedUser(int id) {
            return new Entry(TYPE_USER, id, false);
        }

        public static Entry includedUser(int id) {
            return new Entry(TYPE_USER, id, true);
        }

        public static Entry includedFriendsList(int id) {
            return new Entry(TYPE_FRIENDS_LIST, id, true);
        }

        public static Entry excludedFriendsList(int id) {
            return new Entry(TYPE_FRIENDS_LIST, id, false);
        }

        public Entry(int type, int id, boolean allowed) {
            this.type = type;
            this.id = id;
            this.allowed = allowed;
        }

        @Override
        public String toString() {
            switch (type) {
                case TYPE_FRIENDS_LIST:
                    return allowed ? "list" + id : "-list" + id;
                case TYPE_USER:
                    return allowed ? String.valueOf(id) : String.valueOf(-id);
                default:
                    throw new IllegalStateException("Unknown type");
            }
        }
    }
}
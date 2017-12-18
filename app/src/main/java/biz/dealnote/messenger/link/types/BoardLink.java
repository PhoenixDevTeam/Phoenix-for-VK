package biz.dealnote.messenger.link.types;

/**
 * Created by r.kolbasa on 18.12.2017.
 * Phoenix-for-VK
 */
public class BoardLink extends AbsLink {

    private final int groupId;

    public BoardLink(int groupId) {
        super(BOARD);
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }
}
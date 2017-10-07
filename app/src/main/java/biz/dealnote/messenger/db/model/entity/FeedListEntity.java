package biz.dealnote.messenger.db.model.entity;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class FeedListEntity {

    private final int id;

    private String title;

    private boolean noReposts;

    private int[] sourceIds;

    public FeedListEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public FeedListEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isNoReposts() {
        return noReposts;
    }

    public FeedListEntity setNoReposts(boolean noReposts) {
        this.noReposts = noReposts;
        return this;
    }

    public int[] getSourceIds() {
        return sourceIds;
    }

    public FeedListEntity setSourceIds(int[] sourceIds) {
        this.sourceIds = sourceIds;
        return this;
    }
}
package biz.dealnote.messenger.model;

import biz.dealnote.messenger.api.model.Identificable;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class FeedList implements Identificable {

    private final int id;

    private final String title;

    public FeedList(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
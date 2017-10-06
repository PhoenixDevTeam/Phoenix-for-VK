package biz.dealnote.messenger.api.model;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public class LinkAttachmentToken implements IAttachmentToken {

    public final String url;

    public LinkAttachmentToken(String url) {
        this.url = url;
    }

    @Override
    public String format() {
        return url;
    }
}

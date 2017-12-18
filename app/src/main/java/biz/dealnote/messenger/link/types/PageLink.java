package biz.dealnote.messenger.link.types;

public class PageLink extends AbsLink {

    private final String pageLink;

    public PageLink(String pageLink) {
        super(PAGE);
        this.pageLink = pageLink;
    }

    public String getLink() {
        return pageLink;
    }
}
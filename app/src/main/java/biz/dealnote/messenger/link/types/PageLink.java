package biz.dealnote.messenger.link.types;

public class PageLink extends AbsLink {

    public String pageLink;

    public PageLink(String pageLink) {
        super(PAGE);
        this.pageLink = pageLink;
    }

    @Override
    public String toString() {
        return "PageLink{" +
                "pageLink='" + pageLink + '\'' +
                '}';
    }
}

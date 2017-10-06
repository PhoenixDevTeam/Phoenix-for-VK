package biz.dealnote.messenger.link.types;

public class AwayLink extends AbsLink {

    public String link;

    public AwayLink(String link) {
        super(EXTERNAL_LINK);
        this.link = link;
    }

    @Override
    public String toString() {
        return "AwayLink{" +
                "link='" + link + '\'' +
                '}';
    }
}

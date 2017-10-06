package biz.dealnote.messenger.link.types;

public class DomainLink extends AbsLink {

    public String fullLink;
    public String domain;

    public DomainLink(String fullLink, String domain) {
        super(DOMAIN);
        this.domain = domain;
        this.fullLink = fullLink;
    }

    @Override
    public String toString() {
        return "DomainLink{" +
                "fullLink='" + fullLink + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}

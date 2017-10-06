package biz.dealnote.messenger.link.internal;

public class AbsInternalLink {

    public int start;
    public int end;

    public String targetLine;

    @Override
    public String toString() {
        return "AbsInternalLink{" +
                "start=" + start +
                ", end=" + end +
                ", targetLine='" + targetLine + '\'' +
                '}';
    }
}

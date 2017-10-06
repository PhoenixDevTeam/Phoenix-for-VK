package biz.dealnote.messenger.model;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class LogEventWrapper {

    private final LogEvent event;

    public LogEventWrapper(LogEvent event) {
        this.event = event;
    }

    public LogEvent getEvent() {
        return event;
    }

    private boolean expanded;

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }
}

package biz.dealnote.messenger.listener;

public interface AppStyleable {
    void blockDrawer(boolean block, int gravity);

    void openDrawer(boolean open, int gravity);

    void setStatusbarColored(boolean colored, boolean invertIcons);
}

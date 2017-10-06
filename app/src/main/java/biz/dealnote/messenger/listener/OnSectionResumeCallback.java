package biz.dealnote.messenger.listener;

import biz.dealnote.messenger.model.drawer.SectionDrawerItem;

public interface OnSectionResumeCallback {
    void onSectionResume(SectionDrawerItem sectionDrawerItem);
    void onChatResume(int accountId, int peerId, String title, String imgUrl);
    void onClearSelection();
}

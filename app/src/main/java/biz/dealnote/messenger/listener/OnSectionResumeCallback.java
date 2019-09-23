package biz.dealnote.messenger.listener;

import biz.dealnote.messenger.model.drawer.SectionMenuItem;

public interface OnSectionResumeCallback {
    void onSectionResume(SectionMenuItem sectionDrawerItem);
    void onChatResume(int accountId, int peerId, String title, String imgUrl);
    void onClearSelection();
}

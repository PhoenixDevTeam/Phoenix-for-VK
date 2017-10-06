package biz.dealnote.messenger.mvp.view;

/**
 * Created by admin on 16.05.2017.
 * phoenix
 */
public interface IBasePostEditView extends IBaseAttachmentsEditView, IProgressView, IErrorView {
    void displaySignerInfo(String fullName, String photo);

    void setShowAuthorChecked(boolean checked);

    void setSignerInfoVisible(boolean visible);

    void setAddSignatureOptionVisible(boolean visible);

    void setFromGroupOptionVisible(boolean visible);

    void setFriendsOnlyOptionVisible(boolean visible);

    void setFromGroupChecked(boolean checked);

    void setFriendsOnlyCheched(boolean cheched);
}

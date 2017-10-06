package biz.dealnote.messenger.mvp.view;

/**
 * Created by admin on 27.03.2017.
 * phoenix
 */
public interface ICreateCommentView extends IBaseAttachmentsEditView {
    void returnDataToParent(String textBody);

    void goBack();
}

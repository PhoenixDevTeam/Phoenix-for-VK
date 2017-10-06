package biz.dealnote.messenger.mvp.view;

/**
 * Created by admin on 30.01.2017.
 * phoenix
 */
public interface IPostEditView extends IBasePostEditView, IToastView, IToolbarView {

    void closeAsSuccess();

    void showConfirmExitDialog();


}

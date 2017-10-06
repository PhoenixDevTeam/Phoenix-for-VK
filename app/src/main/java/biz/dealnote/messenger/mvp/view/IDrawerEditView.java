package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.DrawerCategory;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 20.07.2017.
 * phoenix
 */
public interface IDrawerEditView extends IMvpView {
    void displayData(List<DrawerCategory> data);
    void goBackAndApplyChanges();
}
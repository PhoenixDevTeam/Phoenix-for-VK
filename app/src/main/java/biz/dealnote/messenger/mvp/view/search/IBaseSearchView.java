package biz.dealnote.messenger.mvp.view.search;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.fragment.search.options.BaseOption;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 01.05.2017.
 * phoenix
 */
public interface IBaseSearchView<T> extends IMvpView, IErrorView, IAccountDependencyView, IAttachmentsPlacesView {
    void displayData(List<T> data);

    void setEmptyTextVisible(boolean visible);

    void notifyDataSetChanged();

    void notifyItemChanged(int index);

    void notifyDataAdded(int position, int count);

    void showLoading(boolean loading);

    void displaySearchQuery(String query);

    void displayFilter(int accountId, ArrayList<BaseOption> options);
}
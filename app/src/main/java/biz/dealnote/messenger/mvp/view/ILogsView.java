package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.LogEventType;
import biz.dealnote.messenger.model.LogEventWrapper;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public interface ILogsView extends IMvpView, IErrorView {

    void displayTypes(List<LogEventType> types);

    void displayData(List<LogEventWrapper> events);

    void showRefreshing(boolean refreshing);

    void notifyEventDataChanged();

    void notifyTypesDataChanged();

    void setEmptyTextVisible(boolean visible);
}

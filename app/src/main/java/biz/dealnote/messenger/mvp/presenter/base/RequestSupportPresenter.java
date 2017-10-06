package biz.dealnote.messenger.mvp.presenter.base;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.Iterator;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.service.RestRequestManager;
import biz.dealnote.messenger.service.ServiceRequestAdapter;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IMvpView;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public abstract class RequestSupportPresenter<V extends IMvpView> extends RxSupportPresenter<V> {

    private static final String SAVE_CURRENT_REQUESTS = "save_current_requests";

    private ArrayList<Request> mCurrentRequests;
    private ServiceRequestAdapter mRequestAdapter;

    public RequestSupportPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);

        if(savedInstanceState != null){
            mCurrentRequests = savedInstanceState.getParcelableArrayList(SAVE_CURRENT_REQUESTS);
        } else {
            mCurrentRequests = new ArrayList<>();
        }

        mRequestAdapter = new ServiceRequestAdapter(){
            @Override
            public void onRequestError(@NonNull Request request, @NonNull ServiceException e) {
                if(mCurrentRequests.contains(request)){
                    mCurrentRequests.remove(request);
                    RequestSupportPresenter.this.onRequestError(request, e);
                }
            }

            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                if(mCurrentRequests.contains(request)){
                    mCurrentRequests.remove(request);
                    RequestSupportPresenter.this.onRequestFinished(request, resultData);
                }
            }
        };

        tryConnectToCurrentRequests();
    }

    /**
     * Что быдет делать фрагмент, когда запрос выполнился с ошибкой
     *
     * @param request   запрос
     * @param e ошибка
     */
    @CallSuper
    protected void onRequestError(@NonNull Request request, @NonNull ServiceException e){

    }

    /**
     * Что будет делать фрагмент, когда запрос был выполнен успешно и вернул результат
     *
     * @param request    запрос
     * @param resultData результат выполения
     */
    @CallSuper
    protected void onRequestFinished(@NonNull Request request, @NonNull Bundle resultData) {

    }

    /**
     * Попытка переподключиться к коллекции запросов
     * (например, при перевороте экрана)
     */
    private void tryConnectToCurrentRequests() {
        RestRequestManager manager = RestRequestManager.from(App.getInstance());

        Iterator<Request> iterator = mCurrentRequests.iterator();
        while (iterator.hasNext()){
            Request request = iterator.next();
            if (manager.isRequestInProgress(request)) {
                manager.execute(request, mRequestAdapter);
                onRestoreConnectionToRequest(request);
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * Что будет делать фрагмент при восстановлении подключения к запросу (например, при перевороте экрана)
     *
     * @param request запрос, к которому восстановлено подключение
     */
    @CallSuper
    protected void onRestoreConnectionToRequest(Request request) {

    }

    /**
     * Выполнить запрос
     *
     * @param request обьект запроса
     */
    protected void executeRequest(@NonNull Request request) {
        if (!mCurrentRequests.contains(request)) {
            mCurrentRequests.add(request);
        }

        RestRequestManager.from(App.getInstance()).execute(request, mRequestAdapter);
    }

    /**
     * Игнорировать результаты всех запросов
     */
    protected void ignoreAll(){
        mCurrentRequests.clear();
    }

    /**
     * Игнорировать результат выполения запроса
     *
     * @param requestTypes типа запросов, которые необходимо игнорировать
     * @return количество "проигноренных" запросов
     */
    protected int ignoreRequestResult(int... requestTypes) {
        int removed = 0;
        Iterator<Request> iterator = mCurrentRequests.iterator();
        while (iterator.hasNext()) {
            Request request = iterator.next();

            for (int requestType : requestTypes) {
                if (request.getRequestType() == requestType) {
                    iterator.remove();
                    removed++;
                    break;
                }
            }
        }

        return removed;
    }

    /**
     * Присутсвуют ли запросы в списке ожидания
     *
     * @param requestTypes типы запросов
     * @return присутсвуют ли
     */
    protected boolean hasRequest(int... requestTypes) {
        for (Request request : mCurrentRequests) {
            for (int requestType : requestTypes) {
                if (request.getRequestType() == requestType) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Найти запрос по типу
     *
     * @param requestType тип искомого запроса
     * @return первый найденный запрос
     */
    protected Request findRequest(int requestType) {
        for (Request request : mCurrentRequests) {
            if (request.getRequestType() == requestType) {
                return request;
            }
        }

        return null;
    }

    protected Request getFirstRequestInQueue(){
        return Utils.safeIsEmpty(mCurrentRequests) ? null : mCurrentRequests.get(0);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelableArrayList(SAVE_CURRENT_REQUESTS, mCurrentRequests);
    }

    @Override
    public void onDestroyed() {
        RestRequestManager.from(App.getInstance()).removeRequestListener(mRequestAdapter);
        super.onDestroyed();
    }
}

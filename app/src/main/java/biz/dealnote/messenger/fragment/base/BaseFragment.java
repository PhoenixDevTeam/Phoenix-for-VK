package biz.dealnote.messenger.fragment.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.Iterator;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.service.RestRequestManager;
import biz.dealnote.messenger.service.ServiceRequestAdapter;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    private ArrayList<Request> mCurrentRequests;

    private ServiceRequestAdapter mRequestAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
            mCurrentRequests = savedInstanceState.getParcelableArrayList(SAVE_CURRENT_REQUESTS);
        } else {
            mCurrentRequests = new ArrayList<>();
        }

        mRequestAdapter = new ServiceRequestAdapter(){
            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                if(!mCurrentRequests.contains(request)) return;

                mCurrentRequests.remove(request);
                BaseFragment.this.onRequestFinished(request, resultData);
            }

            @Override
            public void onRequestError(@NonNull Request request, @NonNull ServiceException exception) {
                if (mCurrentRequests.contains(request)) {
                    mCurrentRequests.remove(request);
                    BaseFragment.this.onRequestError(request, exception);
                    Logger.d(TAG, "onApiError, " + (mCurrentRequests == null ? "null" : mCurrentRequests.size()));
                } else {
                    Logger.d(TAG, "onRequestApiError, request error was ignored");
                }
            }
        };

        tryConnectToCurrentRequests();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //RequestCodesScope.requireRegistered(requestCode);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        for(Request request : mCurrentRequests){
            onRestoreConnectionToRequest(request);
        }
    }

    /**
     * Выполнить запрос
     *
     * @param request обьект запроса
     */
    protected void executeRequest(@NonNull Request request) {
        Logger.d(TAG, "executeRequest, request: " + request);
        if (!mCurrentRequests.contains(request)) {
            mCurrentRequests.add(request);
        }

        RestRequestManager.from(getContext()).execute(request, mRequestAdapter);
    }

    /**
     * Попытка переподключиться к коллекции запросов
     * (например, при перевороте экрана)
     */
    private void tryConnectToCurrentRequests() {
        RestRequestManager manager = RestRequestManager.from(getContext());

        Iterator<Request> iterator = mCurrentRequests.iterator();
        while (iterator.hasNext()){
            Request request = iterator.next();
            if (manager.isRequestInProgress(request)) {
                manager.execute(request, mRequestAdapter);
            } else {
                iterator.remove();
            }
        }
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

    /**
     * Что будет делать фрагмент при восстановлении подключения к запросу (например, при перевороте экрана)
     *
     * @param request запрос, к которому восстановлено подключение
     */
    protected void onRestoreConnectionToRequest(Request request){

    }

    /**
     * Что будет делать фрагмент, когда запрос был выполнен успешно и вернул результат
     *
     * @param request    запрос
     * @param resultData результат выполения
     */
    @CallSuper
    protected void onRequestFinished(Request request, Bundle resultData){

    }

    /**
     * Что быдет делать фрагмент, когда запрос выполнился с ошибкой
     *
     * @param request   запрос
     * @param throwable     обьект ошибки
     */
    @CallSuper
    protected void onRequestError(Request request, ServiceException throwable){

    }

    private static final String SAVE_CURRENT_REQUESTS = "save_current_requests";

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_CURRENT_REQUESTS, mCurrentRequests);
        Logger.d(TAG, "onSaveInstanceState, put current requests to outState, mCurrentRequests: " + (mCurrentRequests == null ? "null" : mCurrentRequests.size()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RestRequestManager.from(getActivity()).removeRequestListener(mRequestAdapter);
    }

    public App getApplicationContext(){
        return (App) getActivity().getApplicationContext();
    }
}
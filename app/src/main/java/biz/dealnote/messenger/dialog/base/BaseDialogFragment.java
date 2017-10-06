package biz.dealnote.messenger.dialog.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.foxykeep.datadroid.requestmanager.Request;

import java.util.ArrayList;
import java.util.Iterator;

import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.service.RestRequestManager;
import biz.dealnote.messenger.service.ServiceRequestAdapter;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;

public abstract class BaseDialogFragment extends DialogFragment {

    private static final String TAG = BaseDialogFragment.class.getSimpleName();

    private ArrayList<Request> mCurrentRequests;
    private ServiceRequestAdapter mRequestAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestAdapter = new ServiceRequestAdapter(){
            @Override
            public void onRequestError(@NonNull Request request, @NonNull ServiceException exception) {
                if (mCurrentRequests.contains(request)) {
                    mCurrentRequests.remove(request);
                    BaseDialogFragment.this.onRequestError(request, exception);
                }
            }

            @Override
            public void onRequestFinished(Request request, Bundle resultData) {
                if (mCurrentRequests.contains(request)) {
                    mCurrentRequests.remove(request);
                    BaseDialogFragment.this.onRequestFinished(request, resultData);
                }
            }
        };

        if (savedInstanceState != null) {
            restoreCurrentRequests(savedInstanceState);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tryConnectToCurrentRequests();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //RequestCodesScope.requireRegistered(requestCode);
    }

    /**
     * Выполнить запрос
     *
     * @param request обьект запроса
     */
    protected void executeRequest(@NonNull Request request) {
        Logger.d(TAG, "executeRequest, request: " + request);
        if (mCurrentRequests == null) {
            mCurrentRequests = new ArrayList<>();
        }

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
        Logger.d(TAG, "tryConnectToCurrentRequests, exist requests: " + (mCurrentRequests == null ? "null" : mCurrentRequests.size()));
        if (mCurrentRequests == null) {
            return;
        }

        RestRequestManager manager = RestRequestManager.from(getContext());

        Iterator<Request> iterator = mCurrentRequests.iterator();
        while (iterator.hasNext()){
            Request request = iterator.next();
            if (manager.isRequestInProgress(request)) {
                manager.addRequestListener(mRequestAdapter, request);
                onRestoreConnectionToRequest(request);
            } else {
                iterator.remove();
            }
        }
    }

    /**
     * Игнорировать результаты всех запросов
     */
    protected void ignoreAll(){
        if(mCurrentRequests != null){
            mCurrentRequests.clear();
        }
    }

    /**
     * Игнорировать результат выполения запроса
     *
     * @param requestTypes типа запросов, которые необходимо игнорировать
     * @return количество "проигноренных" запросов
     */
    protected int ignoreRequestResult(int... requestTypes) {
        if (mCurrentRequests == null) {
            return 0;
        }

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
        if (mCurrentRequests == null) {
            return false;
        }

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
        if (mCurrentRequests == null) {
            return null;
        }

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
    protected void onRequestFinished(@NonNull Request request, Bundle resultData){

    }

    /**
     * Что быдет делать фрагмент, когда запрос выполнился с ошибкой
     *
     * @param request   запрос
     * @param throwable     обьект ошибки
     */
    @CallSuper
    protected void onRequestError(@NonNull Request request, ServiceException throwable){

    }

    private static final String SAVE_CURRENT_REQUESTS = "save_current_requests";

    private void restoreCurrentRequests(Bundle savedInstanceState) {
        mCurrentRequests = savedInstanceState.getParcelableArrayList(SAVE_CURRENT_REQUESTS);
        Logger.d(TAG, "restoreCurrentRequests, mCurrentRequests: " + (mCurrentRequests == null ? "null" : mCurrentRequests.size()));
    }

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
        ViewUtils.keyboardHide(getActivity());
    }

}

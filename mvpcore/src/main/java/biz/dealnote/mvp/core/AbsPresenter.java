package biz.dealnote.mvp.core;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import biz.dealnote.mvp.Logger;
import biz.dealnote.mvp.reflect.AnnotatedHandlerFinder;
import biz.dealnote.mvp.reflect.EventHandler;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public abstract class AbsPresenter<V extends IMvpView> implements IPresenter<V> {

    private static final String TAG = AbsPresenter.class.getSimpleName();

    /**
     * V (View Host - это не значит, что существуют реальные вьюхи,
     * с которыми можно работать и менять их состояние. Это значит,
     * что существует, к примеру, фрагмент, но нет гарантии, кто метод onCreateView был выполнен.
     * К примеру, фрагмент находится в стэке, он существует, может хранить какие-то данные,
     * но при перевороте его вьюв был уничтожен, но не был заново создан, ибо фрагмент не в топе
     * контейнера и не added)
     */
    private WeakReference<V> mViewHost;
    private boolean mIsGuiReady;

    private static final AtomicInteger IDGEN = new AtomicInteger();

    private int mPresenterId;

    public AbsPresenter(@Nullable Bundle savedInstanceState){
        if(savedInstanceState != null){
            mPresenterId = savedInstanceState.getInt(SAVE_ID);
            if(mPresenterId >= IDGEN.get()){
                IDGEN.set(mPresenterId + 1);
            }
        } else {
            mPresenterId = IDGEN.incrementAndGet();
        }
    }

    public int getPresenterId(){
        return mPresenterId;
    }

    private static final String SAVE_ID = "save_presenter_id";

    @Override
    public void onViewHostAttached(@NonNull V viewHost) {
        Logger.d(TAG, "onViewHostAttached, tag: " + tag());
        this.mViewHost = new WeakReference<>(viewHost);
    }

    @Override
    public void onViewHostDetached() {
        Logger.d(TAG, "onViewHostDetached, tag: " + tag());
        this.mViewHost = null;
    }

    @CallSuper
    @Override
    public void onGuiCreated(@NonNull V viewHost) {
        Logger.d(TAG, "onGuiCreated, tag: " + tag());

        mIsGuiReady = true;
        executeAllResolveViewMethods();
    }

    private void executeAllResolveViewMethods(){
        Set<EventHandler> resolveMethodHandlers =
                AnnotatedHandlerFinder.findAllOnGuiCreatedHandlers(this, AbsPresenter.class);

        for(EventHandler handler : resolveMethodHandlers){
            if(!handler.isValid()) continue;

            try {
                handler.handle();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onGuiDestroyed() {
        Logger.d(TAG, "onGuiDestroyed, tag: " + tag());
        mIsGuiReady = false;
    }

    private boolean mDestroyed;

    private boolean mResumed;

    @Override
    public boolean isGuiResumed() {
        return mResumed;
    }

    @Override
    public void onGuiResumed() {
        mResumed = true;
        Logger.d(TAG, "onGuiResumed, tag: " + tag());
    }

    @Override
    public void onGuiPaused() {
        mResumed = false;
        Logger.d(TAG, "onGuiPaused, tag: " + tag());
    }

    @Override
    public void onDestroyed() {
        Logger.d(TAG, "onDestroyed, tag: " + tag());
        mDestroyed = true;
    }

    public boolean isDestroyed() {
        return mDestroyed;
    }

    public void saveState(@NonNull Bundle outState) {
        outState.putInt(SAVE_ID, mPresenterId);
        Logger.d(TAG, "saveState, tag: " + tag());
    }

    @Override
    public boolean isGuiReady() {
        return mIsGuiReady;
    }

    public V getView() {
        return mViewHost == null ? null : mViewHost.get();
    }

    @Override
    public boolean isViewHostAttached() {
        return getView() != null;
    }

    protected void callView(ViewAction<V> action){
        if(isGuiReady()){
            action.call(getView());
        }
    }

    protected abstract String tag();
}

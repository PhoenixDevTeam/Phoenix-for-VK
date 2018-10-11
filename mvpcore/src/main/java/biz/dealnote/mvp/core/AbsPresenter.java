package biz.dealnote.mvp.core;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import biz.dealnote.mvp.reflect.AnnotatedHandlerFinder;
import biz.dealnote.mvp.reflect.EventHandler;

/**
 * Created by admin on 11.07.2016.
 * mvpcore
 */
public abstract class AbsPresenter<V extends IMvpView> implements IPresenter<V> {

    /**
     * V (View Host - это не значит, что существуют реальные вьюхи,
     * с которыми можно работать и менять их состояние. Это значит,
     * что существует, к примеру, фрагмент, но нет гарантии, кто метод onCreateView был выполнен.
     * К примеру, фрагмент находится в стэке, он существует, может хранить какие-то данные,
     * но при перевороте его вьюв был уничтожен, но не был заново создан, ибо фрагмент не в топе
     * контейнера и не added)
     */
    private WeakReference<V> viewReference;
    private boolean guiCreated;

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

    private static final String SAVE_ID = "save_presenter_id";

    public int getPresenterId(){
        return mPresenterId;
    }

    @CallSuper
    protected void onViewHostAttached(@NonNull V view) {

    }

    @CallSuper
    protected void onViewHostDetached() {

    }

    @CallSuper
    protected void onGuiCreated(@NonNull V viewHost) {

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

    @CallSuper
    protected void onGuiDestroyed() {

    }

    private boolean destroyed;

    private boolean guiResumed;

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isGuiResumed() {
        return guiResumed;
    }

    @CallSuper
    protected void onGuiResumed() {

    }

    @CallSuper
    protected void onGuiPaused() {

    }

    @Override
    public final void destroy() {
        destroyed = true;
        onDestroyed();
    }

    @Override
    public final void resumeView() {
        guiResumed = true;
        onGuiResumed();
    }

    public boolean isViewHostAttached(){
        return viewReference.get() != null;
    }

    @Override
    public final void pauseView() {
        guiResumed = false;
        onGuiPaused();
    }

    @Override
    public final void attachViewHost(@NonNull V view) {
        viewReference = new WeakReference<>(view);
        onViewHostAttached(view);
    }

    @Override
    public final void detachViewHost() {
        viewReference = new WeakReference<>(null);
        onViewHostDetached();
    }

    @Override
    public final void createView(@NonNull V view) {
        guiCreated = true;
        executeAllResolveViewMethods();
        onGuiCreated(view);
    }

    @Override
    public final void destroyView() {
        guiCreated = false;
        onGuiDestroyed();
    }

    @CallSuper
    public void onDestroyed() {

    }

    @Override
    @CallSuper
    public void saveState(@NonNull Bundle outState) {
        outState.putInt(SAVE_ID, mPresenterId);
    }

    public final boolean isGuiReady() {
        return guiCreated;
    }

    //@Nullable
    public V getView() {
        return viewReference == null ? null : viewReference.get();
    }

    protected void callView(ViewAction<V> action){
        if(isGuiReady()){
            action.call(getView());
        }
    }
}
package biz.dealnote.messenger.util;

import androidx.annotation.Nullable;

import java.io.Closeable;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.Injection;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.FlowableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by admin on 19.03.2017.
 * phoenix
 */
public class RxUtils {

    private static final Action DUMMMY_ACTION_0 = () -> {
    };

    public static Action dummy() {
        return DUMMMY_ACTION_0;
    }

    public static <T> Consumer<T> ignore(){
        return t -> {
            if(t instanceof Throwable && BuildConfig.DEBUG){
                ((Throwable) t).printStackTrace();
            }
        };
    }

    public static Action safelyCloseAction(@Nullable Closeable closeable){
        return () -> Utils.safelyClose(closeable);
    }

    public static <T> Disposable subscribeOnIOAndIgnore(Single<T> single) {
        return single.subscribeOn(Schedulers.io())
                .subscribe(ignore(), ignore());
    }

    public static Disposable subscribeOnIOAndIgnore(Completable completable) {
        return completable.subscribeOn(Schedulers.io())
                .subscribe(dummy(), ignore());
    }

    public static <T> MaybeTransformer<T, T> applyMaybeIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> SingleTransformer<T, T> applySingleIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> SingleTransformer<T, T> applySingleComputationToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> ObservableTransformer<T, T> applyObservableIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }

    public static <T> FlowableTransformer<T, T> applyFlowableIOToMainSchedulers() {
        return upstream -> upstream
                .subscribeOn(Schedulers.computation())
                .observeOn(Injection.provideMainThreadScheduler());
    }


    public static CompletableTransformer applyCompletableIOToMainSchedulers() {
        return completable -> completable.subscribeOn(Schedulers.io())
                .observeOn(Injection.provideMainThreadScheduler());
    }
}
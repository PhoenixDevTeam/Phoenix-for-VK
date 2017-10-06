package biz.dealnote.messenger.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.interfaces.ILogsStore;
import biz.dealnote.messenger.model.LogEvent;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static biz.dealnote.messenger.util.Utils.safelyClose;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public class PersistentLogger {

    public static void logThrowable(String tag, Throwable throwable){
        ILogsStore store = Injection.provideLogsStore();
        Throwable cause = Utils.getCauseIfRuntime(throwable);

        getStackTrace(cause)
                .flatMapCompletable(s -> store.add(LogEvent.Type.ERROR, tag, s)
                        .toCompletable())
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, ignore -> {});
    }

    private static Single<String> getStackTrace(final Throwable throwable){
        return Single.fromCallable(() -> {
            StringWriter sw = null;
            PrintWriter pw = null;
            try {
                sw = new StringWriter();
                pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                return sw.toString();
            } finally {
                safelyClose(pw);
                safelyClose(sw);
            }
        });
    }
}
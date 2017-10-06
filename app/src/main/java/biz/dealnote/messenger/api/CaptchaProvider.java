package biz.dealnote.messenger.api;

import android.content.Context;
import android.content.Intent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import biz.dealnote.messenger.activity.CaptchaActivity;
import biz.dealnote.messenger.api.model.Captcha;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Ruslan Kolbasa on 06.06.2017.
 * phoenix
 */
public class CaptchaProvider implements ICaptchaProvider {

    private final Context app;

    private final Map<String, Entry> entryMap;

    private final Scheduler uiScheduler;

    public CaptchaProvider(Context app, Scheduler uiScheduler) {
        this.app = app;
        this.uiScheduler = uiScheduler;
        this.entryMap = Collections.synchronizedMap(new HashMap<>());
        this.waitingNotifier = PublishSubject.create();
        this.cancelingNotifier = PublishSubject.create();
    }

    @Override
    public void requestCaptha(String sid, Captcha captcha) {
        Entry entry = new Entry(sid, captcha);
        entryMap.put(sid, entry);

        startCapthaActivity(app, sid, captcha);
    }

    private void startCapthaActivity(final Context context, String sid, Captcha captcha){
        Completable.complete()
                .observeOn(uiScheduler)
                .subscribe(() -> {
                    Intent intent = CaptchaActivity.createIntent(context, sid, captcha.getImg());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
    }

    @Override
    public void cancel(String sid) {
        entryMap.remove(sid);
        cancelingNotifier.onNext(sid);
    }

    @Override
    public Observable<String> observeCanceling() {
        return cancelingNotifier;
    }

    @Override
    public String lookupCode(String sid) throws OutOfDateException {
        Iterator<Map.Entry<String, Entry>> iterator = entryMap.entrySet().iterator();

        while (iterator.hasNext()){
            Map.Entry<String, Entry> next = iterator.next();
            String lookupsid = next.getKey();
            Entry lookupEntry = next.getValue();

            if(System.currentTimeMillis() - lookupEntry.lastActivityTime > MAX_WAIT_DELAY){
                iterator.remove();
            } else {
                waitingNotifier.onNext(lookupsid);
            }
        }

        Entry entry = entryMap.get(sid);

        if(Objects.isNull(entry)){
            throw new OutOfDateException();
        }

        return entry.getCode();
    }

    private static final int MAX_WAIT_DELAY = 15 * 60 * 1000;

    private final PublishSubject<String> cancelingNotifier;
    private final PublishSubject<String> waitingNotifier;

    @Override
    public Observable<String> observeWaiting() {
        return waitingNotifier;
    }

    @Override
    public void notifyThatCaptchaEntryActive(String sid) {
        Entry entry = entryMap.get(sid);
        if(Objects.nonNull(entry)){
            entry.lastActivityTime = System.currentTimeMillis();
        }
    }

    @Override
    public void enterCode(String sid, String code) {
        Entry entry = entryMap.get(sid);
        if(Objects.nonNull(entry)){
            entry.code = code;
        }
    }

    private static class Entry {

        final String sid;

        final Captcha captcha;

        String code;

        long lastActivityTime;

        Entry(String sid, Captcha captcha) {
            this.sid = sid;
            this.captcha = captcha;
            this.lastActivityTime = System.currentTimeMillis();
        }

        public String getCode() {
            return code;
        }
    }
}
/**
 * 2011 Foxykeep (http://datadroid.foxykeep.com)
 * <p>
 * Licensed under the Beerware License : <br />
 * As long as you retain this notice you can do whatever you want with this stuff. If we meet some
 * day, and you think this stuff is worth it, you can buy me a beer in return
 */

package com.foxykeep.datadroid.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import biz.dealnote.messenger.BuildConfig;
import biz.dealnote.messenger.R;

/**
 * MultiThreadIntentService is a base class for {@link android.app.Service}s that handle asynchronous requests
 * (expressed as {@link android.content.Intent}s) on demand. Clients send requests through
 * {@link android.content.Context#startService(android.content.Intent)} calls; the service is started as needed,
 * handles each Intent in turn using a worker thread, and stops itself when it runs out of work.
 * <p>
 * This "work queue processor" pattern is commonly used to offload tasks from an application's main
 * thread. The MultiThreadedIntentService class exists to simplify this pattern and take care of the
 * mechanics. To use it, extend MultiThreadedIntentService and implement
 * {@link #onHandleIntent(android.content.Intent)}. MultiThreadedIntentService will receive the Intents, launch a
 * worker thread, and stop the service as appropriate.
 * <p>
 * All requests are handled on multiple worker threads -- they may take as long as necessary (and
 * will not block the application's main loop). By default only one concurrent worker thread is
 * used. You can modify the number of current worker threads by overriding
 * {@link #getMaximumNumberOfThreads()}.
 * <p>
 * For obvious efficiency reasons, MultiThreadedIntentService won't stop itself as soon as all tasks
 * has been processed. It will only stop itself after a certain delay (about 30s). This optimization
 * prevents the system from creating new instances over and over again when tasks are sent.
 *
 * @author Foxykeep
 */
public abstract class MultiThreadedIntentService extends Service {
    
    private static final long STOP_SELF_DELAY = 3 * 1000; // 3s

    private ExecutorService mThreadPool;
    private boolean mRedelivery;

    private ArrayList<Future<?>> mFutureList;

    private Handler mHandler;
    
    private final Runnable mStopSelfRunnable = () -> {
        if(BuildConfig.DEBUG){
            stopForeground(true);
        }

        stopSelf();
    };

    private final Runnable mWorkDoneRunnable = new Runnable() {
        @Override
        public void run() {
            if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
                throw new IllegalStateException(
                        "This runnable can only be called in the Main thread!");
            }

            final ArrayList<Future<?>> futureList = mFutureList;
            Iterator<Future<?>> iterator = futureList.iterator();
            while (iterator.hasNext()){
                if(iterator.next().isDone()){
                    iterator.remove();
                }
            }

            //for (int i = 0; i < futureList.size(); i++) {
            //    if (futureList.get(i).isDone()) {
            //        futureList.remove(i);
            //        i--;
            //    }
            //}

            if (futureList.isEmpty()) {
                mHandler.postDelayed(mStopSelfRunnable, STOP_SELF_DELAY);
            }
        }
    };

    /**
     * Sets intent redelivery preferences. Usually called from the constructor with your preferred
     * semantics.
     * <p>
     * If enabled is true, {@link #onStartCommand(android.content.Intent, int, int)} will return
     * {@link android.app.Service#START_REDELIVER_INTENT}, so if this process dies before
     * {@link #onHandleIntent(android.content.Intent)} returns, the process will be restarted and the intent
     * redelivered. If multiple Intents have been sent, only the most recent one is guaranteed to be
     * redelivered.
     * <p>
     * If enabled is false (the default), {@link #onStartCommand(android.content.Intent, int, int)} will return
     * {@link android.app.Service#START_NOT_STICKY}, and if the process dies, the Intent dies along with it.
     */
    @SuppressWarnings("unused")
    protected void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int maximumNumberOfThreads = getMaximumNumberOfThreads();
        if (maximumNumberOfThreads <= 0) {
            throw new IllegalArgumentException("Maximum number of threads must be " +
                    "strictly positive");
        }

        mThreadPool = Executors.newFixedThreadPool(maximumNumberOfThreads);
        mHandler = new Handler();
        mFutureList = new ArrayList<>();

        if(BuildConfig.DEBUG){
            //startWithNotification();
        }
    }

    private void startWithNotification(){
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("REST service is active")
                .setSmallIcon(R.drawable.ic_statusbar_rest_active)
                //.setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(REST_SERVICE_NOTIFICATION_ID, notification);
    }

    private static final int REST_SERVICE_NOTIFICATION_ID = 90;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeCallbacks(mStopSelfRunnable);

        IntentRunnable intentRunnable = new IntentRunnable(intent);
        Future<?> future = mThreadPool.submit(intentRunnable);
        mFutureList.add(future);

        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(BuildConfig.DEBUG){
            ((NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE))
                    .cancel(REST_SERVICE_NOTIFICATION_ID);
        }

        mThreadPool.shutdown();
        super.onDestroy();
    }

    /**
     * Unless you provide binding for your service, you don't need to implement this method, because
     * the default implementation returns null.
     *
     * @see android.app.Service#onBind
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Define the maximum number of concurrent worker threads used to execute the incoming Intents.
     * <p>
     * By default only one concurrent worker thread is used at the same time. Overrides this method
     * in subclasses to change this number.
     * <p>
     * This method is called once in the {@link #onCreate()}. Modifying the value returned after the
     * {@link #onCreate()} is called will have no effect.
     *
     * @return The maximum number of concurrent worker threads
     */
    protected int getMaximumNumberOfThreads() {
        return 3;
    }

    private class IntentRunnable implements Runnable {

        private final Intent mIntent;

        IntentRunnable(Intent intent) {
            mIntent = intent;
        }

        @Override
        public void run() {
            onHandleIntent(mIntent);
            mHandler.removeCallbacks(mWorkDoneRunnable);
            mHandler.post(mWorkDoneRunnable);
        }
    }

    /**
     * This method is invoked on the worker thread with a request to process. The processing happens
     * on a worker thread that runs independently from other application logic. When all requests
     * have been handled, the IntentService stops itself, so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link android.content.Context#startService(android.content.Intent)}.
     */
    abstract protected void onHandleIntent(Intent intent);
}

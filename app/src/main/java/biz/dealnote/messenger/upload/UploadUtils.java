package biz.dealnote.messenger.upload;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Logger;
import io.reactivex.schedulers.Schedulers;

public class UploadUtils {

    private static final String TAG = UploadUtils.class.getSimpleName();
    private static IUploadService mService = null;
    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;

    static {
        mConnectionMap = new WeakHashMap<>();
    }

    private UploadUtils() {}

    public static void upload(@NonNull Context context, List<UploadIntent> intents){
        final Context app = context.getApplicationContext();

        Stores.getInstance()
                .uploads()
                .put(intents)
                .subscribeOn(Schedulers.io())
                .subscribe(objects -> uploadFirst(app), Analytics::logUnexpectedError);
    }

    private static void uploadFirst(Context context){
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_UPLOAD_FIRST);
        context.startService(intent);
    }

    public static void cancelByDestination(@NonNull Context context, @NonNull UploadDestination destination){
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_CANCEL_BY_DESTINATION);
        intent.putExtra(Extra.DESTINATION, destination);
        context.startService(intent);
    }

    /**
     * @param context  The {@link Context} to use
     * @param callback The {@link ServiceConnection} to use
     * @return The new instance of {@link ServiceToken}
     */
    public static ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        ServiceBinder binder = new ServiceBinder(callback);
        Intent intent = new Intent().setClass(contextWrapper, UploadService.class);

        if (contextWrapper.bindService(intent, binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }

        Logger.d(TAG, "bindToService, count: " + mConnectionMap.size());
        return null;
    }

    public static UploadObject getCurrent(){
        if(mService != null){
            try {
                return mService.getCurrent();
            } catch (RemoteException ignored) {}
        }

        return null;
    }

    public static List<UploadIntent> createIntents(int accountId, UploadDestination destination, List<LocalPhoto> photos, int size,
                                                   boolean autoCommit){
        List<UploadIntent> intents = new ArrayList<>(photos.size());
        for (LocalPhoto photo : photos) {
            intents.add(new UploadIntent(accountId, destination)
                    .setSize(size)
                    .setAutoCommit(autoCommit)
                    .setFileId(photo.getImageId())
                    .setFileUri(photo.getFullImageUri()));
        }
        return intents;
    }

    public static void cancelById(Context context, int id){
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(UploadService.ACTION_CANCEL_BY_ID);
        intent.putExtra(Extra.ID, id);
        context.startService(intent);
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }

        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }

        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {

        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IUploadService.Stub.asInterface(service);

            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }

            mService = null;
        }
    }

    public static final class ServiceToken {

        public ContextWrapper mWrappedContext;

        /**
         * Constructor of <code>ServiceToken</code>
         *
         * @param context The {@link ContextWrapper} to use
         */
        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }
}

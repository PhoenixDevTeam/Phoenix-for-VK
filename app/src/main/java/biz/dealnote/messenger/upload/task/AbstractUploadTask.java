package biz.dealnote.messenger.upload.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import biz.dealnote.messenger.api.PercentageListener;
import biz.dealnote.messenger.api.model.server.UploadServer;
import biz.dealnote.messenger.upload.BaseUploadResponse;
import biz.dealnote.messenger.upload.UploadCallback;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.IOUtils;
import retrofit2.Call;

public abstract class AbstractUploadTask<R extends BaseUploadResponse> extends AsyncTask<Object, Integer, R> implements PercentageListener {

    protected static final String TAG = AbstractUploadTask.class.getSimpleName();

    private Context context;
    protected UploadCallback callback;
    protected UploadObject uploadObject;
    protected UploadServer server;

    public AbstractUploadTask(Context cntx, @NonNull UploadCallback callback, @NonNull UploadObject uploadObject, @Nullable UploadServer server) {
        this.context = cntx.getApplicationContext();
        this.callback = callback;
        this.uploadObject = uploadObject;
        this.server = server;
    }

    @Override
    public void onProgressChanged(int percentage) {
        super.publishProgress(percentage);
    }

    private final Object callLocker = new Object();

    private Set<Call<?>> activeCalls = Collections.synchronizedSet(new HashSet<>(1));

    protected void registerCall(@NonNull Call<?> call){
        synchronized (callLocker){
            this.activeCalls.add(call);
        }
    }

    protected void unregisterCall(@NonNull Call<?> call){
        synchronized (callLocker){
            activeCalls.remove(call);
        }
    }

    public final void cancelUploading() {
        synchronized (callLocker){
            for(Call<?> call : activeCalls){
                try {
                    call.cancel();
                } catch (Exception ignored){}
            }
        }

        super.cancel(false);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (isCancelled()) return;

        uploadObject.setProgress(values[0]);
        callback.onProgressUpdate(uploadObject, values[0]);
    }

    @Override
    protected void onPreExecute() {
        callback.onPrepareToUpload(uploadObject);
    }

    public void startAsync(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, server, uploadObject);
    }

    @Override
    protected final R doInBackground(Object... params) {
        R response = null;

        try {
            response = doUpload((UploadServer) params[0], (UploadObject) params[1]);
        } catch (CancelException ignored) {
        }

        return response;
    }

    protected abstract R doUpload(@Nullable UploadServer server, @NonNull UploadObject uploadObject) throws CancelException;

    @Override
    protected void onPostExecute(R response) {
        if (!response.isSuccess()) {
            callback.onError(uploadObject, response.getThrowable());
        } else {
            callback.onSuccess(uploadObject, response);
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected void onCancelled() {
        callback.onCanceled(uploadObject);
    }

    public static InputStream openStream(Context context, Uri uri, int size) throws IOException {
        InputStream originalStream;

        File filef = new File(uri.getPath());
        if (filef.isFile()) {
            originalStream = new FileInputStream(filef);
        } else {
            originalStream = context.getContentResolver().openInputStream(uri);
        }

        if (size == UploadObject.IMAGE_SIZE_FULL) {
            return originalStream;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(originalStream);
        File tempFile = new File(context.getExternalCacheDir() + File.separator + "scale.jpg");

        FileOutputStream ostream = null;
        Bitmap target = null;

        try {
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    throw new IOException("Unable to delete old image file");
                }
            }

            if (!tempFile.createNewFile()) {
                throw new IOException("Unable to create new file");
            }

            ostream = new FileOutputStream(tempFile);
            target = scaleDown(bitmap, size, true);
            target.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            return new FileInputStream(tempFile);
        } finally {
            IOUtils.recycleBitmapQuietly(bitmap);
            IOUtils.recycleBitmapQuietly(target);
            IOUtils.closeStreamQuietly(ostream);
            IOUtils.closeStreamQuietly(originalStream);
        }
    }

    /*protected File getImageFileForUpload(UploadObject item) throws IOException {
        if (item.getSize() == UploadObject.IMAGE_SIZE_FULL) {
            return new File(item.getFileUri().getPath());
        } else {
            File originalFile = new File(item.getFileUri().getPath());
            if (!originalFile.isFile()) {
                throw new FileNotFoundException(item.getFileUri() + " is not valid file");
            }

            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            Bitmap target = null;
            File file = new File(context.getExternalCacheDir() + File.separator + "scale.jpg");

            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("Unable to delete old image file");
                }
            }

            FileOutputStream ostream = null;
            try {
                if (!file.createNewFile()) {
                    throw new IOException("Unable to create new file");
                }

                ostream = new FileOutputStream(file);
                target = scaleDown(bitmap, item.getSize(), true);
                target.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            } finally {
                bitmap.recycle();
                if (target != null) {
                    target.recycle();
                }

                if (ostream != null) {
                    ostream.close();
                }
            }

            return file;
        }
    }*/

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        if (realImage.getHeight() < maxImageSize && realImage.getWidth() < maxImageSize) {
            return realImage;
        }

        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    protected static void assertCancel(AsyncTask task) throws CancelException {
        if (task.isCancelled()) {
            throw new CancelException();
        }
    }

    protected static class CancelException extends Exception {
    }

    public UploadObject getUploadObject() {
        return uploadObject;
    }
}
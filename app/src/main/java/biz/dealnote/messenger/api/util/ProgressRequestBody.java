package biz.dealnote.messenger.api.util;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by ruslan.kolbasa on 26.12.2016.
 * phoenix
 */
public class ProgressRequestBody extends RequestBody {

    private InputStream stream;
    private UploadCallbacks listener;
    private MediaType mediaType;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
    }

    public ProgressRequestBody(final InputStream file, final UploadCallbacks listener, MediaType mediaType) {
        this.stream = file;
        this.listener = listener;
        this.mediaType = mediaType;
    }

    @Override
    public MediaType contentType() {
        return mediaType;
    }

    @Override
    public long contentLength() throws IOException {
        return stream.available();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = stream.available();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long uploaded = 0;

        try {
            int read;
            //Handler handler = new Handler(Looper.getMainLooper());

            while ((read = stream.read(buffer)) != -1) {

                // update progress on UI thread
                //handler.post(new ProgressUpdater(uploaded, fileLength));

                if(listener != null){
                    listener.onProgressUpdate((int) (100 * uploaded / fileLength));
                }

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            stream.close();
        }
    }

    /*private class ProgressUpdater implements Runnable {

        private long mUploaded;
        private long mTotal;

        ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            listener.onProgressUpdate((int)(100 * mUploaded / mTotal));
        }
    }*/
}
package biz.dealnote.messenger.task;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private String photourl;
    private String file;

    public DownloadImageTask(Context context, String url, String file) {
        this.context = context.getApplicationContext();
        this.file = file;
        this.photourl = url;
    }

    @Override
    protected String doInBackground(String... params) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wl.acquire();

        try {
            URL url = new URL(photourl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(60000);
            urlConnection.setReadTimeout(60000);

            try {
                urlConnection.connect();
            } catch (ConnectException ex) {
                throw new Exception(ex);
            }

            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Server return " + urlConnection.getResponseCode() +
                        " " + urlConnection.getResponseMessage());
            }

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            double downloadedSize = 0.0;
            byte[] buffer = new byte[5000];
            int bufferLength;

            int totalSize = urlConnection.getContentLength();
            while (true) {
                bufferLength = inputStream.read(buffer);
                if (bufferLength <= 0) break;
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                publishProgress((int) ((downloadedSize / totalSize) * 100));
            }

            fileOutput.flush();
            fileOutput.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(file))));
        } catch (Exception e) {
            return e.toString();
        } finally {
            wl.release();
        }

        return null;
    }

    public void doDownload(){
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}

package biz.dealnote.messenger.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import biz.dealnote.messenger.Constants;

/**
 * Created by admin on 17.04.2017.
 * phoenix
 */
public class FileUtil {

    private static final DateFormat PHOTO_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    public static Uri getExportedUriForFile(Context context, File file){
        return FileProvider.getUriForFile(context, Constants.FILE_PROVIDER_AUTHORITY, file);
    }

    public static File createImageFile() throws IOException {
        String timeStamp = PHOTO_DATE_FORMAT.format(new Date());
        String externalStorageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File directory = new File(externalStorageDir + "/" + Constants.PHOTOS_PATH);

        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Unable to create directory");
        }

        File targetFile = null;
        Random random = new Random();

        boolean noExist = false;
        while (!noExist) {
            int randomInt = random.nextInt(1000000);
            String fileName = "JPEG_" + timeStamp + "_" + randomInt + ".jpg";
            File file = new File(directory, fileName);

            if (!file.exists()) {
                targetFile = file;
                noExist = true;
            }
        }

        return targetFile;
    }
}

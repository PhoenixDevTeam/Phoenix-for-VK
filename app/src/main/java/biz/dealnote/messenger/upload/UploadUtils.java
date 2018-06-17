package biz.dealnote.messenger.upload;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.util.IOUtils;

public final class UploadUtils {

    private UploadUtils(){

    }

    public static InputStream openStream(Context context, Uri uri, int size) throws IOException {
        InputStream originalStream;

        File filef = new File(uri.getPath());
        if (filef.isFile()) {
            originalStream = new FileInputStream(filef);
        } else {
            originalStream = context.getContentResolver().openInputStream(uri);
        }

        if (size == Upload.IMAGE_SIZE_FULL) {
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

    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        if (realImage.getHeight() < maxImageSize && realImage.getWidth() < maxImageSize) {
            return realImage;
        }

        float ratio = Math.min(maxImageSize / realImage.getWidth(), maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }
}
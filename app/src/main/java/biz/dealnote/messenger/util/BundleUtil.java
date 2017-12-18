package biz.dealnote.messenger.util;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by r.kolbasa on 18.12.2017.
 * Phoenix-for-VK
 * https://gist.github.com/aprock/2037883
 */
public class BundleUtil {

    public static String serializeBundle(final Bundle bundle) {
        String base64;
        final Parcel parcel = Parcel.obtain();
        try {
            parcel.writeBundle(bundle);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(bos));
            zos.write(parcel.marshall());
            zos.close();
            base64 = Base64.encodeToString(bos.toByteArray(), 0);
        } catch (IOException e) {
            e.printStackTrace();
            base64 = null;
        } finally {
            parcel.recycle();
        }
        return base64;
    }

    public static Bundle deserializeBundle(final String base64) {
        Bundle bundle;

        final Parcel parcel = Parcel.obtain();
        try {
            final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            final GZIPInputStream zis = new GZIPInputStream(new ByteArrayInputStream(Base64.decode(base64, 0)));
            int len;
            while ((len = zis.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            zis.close();
            parcel.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
            parcel.setDataPosition(0);
            bundle = parcel.readBundle();
        } catch (IOException e) {
            e.printStackTrace();
            bundle = null;
        } finally {
            parcel.recycle();
        }

        return bundle;
    }
}
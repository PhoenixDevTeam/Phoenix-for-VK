package biz.dealnote.messenger.push;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import biz.dealnote.messenger.Constants;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class GcmTokenProvider implements IGcmTokenProvider {

    private final Context app;

    public GcmTokenProvider(Context context) {
        this.app = context.getApplicationContext();
    }

    @Override
    public String getToken() throws IOException {
        InstanceID instanceID = InstanceID.getInstance(app);
        return instanceID.getToken(Constants.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }
}
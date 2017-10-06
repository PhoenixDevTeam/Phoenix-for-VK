package biz.dealnote.messenger.service.operations.account;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.exception.ApiServiceException;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.service.ApiErrorCodes;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.settings.VkPushRegistration;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.service.operations.AbsApiOperation.prepareException;

public class PushResolveOperation implements RequestService.Operation {

    private static final String TAG = PushResolveOperation.class.getSimpleName();

    @Override
    public Bundle execute(Context context, Request request) throws DataException, CustomRequestException {
        long start = System.currentTimeMillis();

        int currentAccountId = Settings.get()
                .accounts()
                .getCurrent();

        if (!MainActivity.checkPlayServices(context)) {
            Logger.d(TAG, "No GCM support");
            return null;
        }

        List<VkPushRegistration> registrations = Settings.get()
                .pushSettings()
                .getRegistrations();

        String deviceId = Utils.getDiviceId(context);

        String gcmToken;

        try {
            gcmToken = getGcmToken(context);
        } catch (IOException e) {
            Logger.d(TAG, "Unable to get GCM token, error: " + e.getMessage());
            return null;
        }

        boolean hasCurrentAccount = false;
        for (VkPushRegistration registration : registrations) {
            Reason reason = resolveRegistration(registration, currentAccountId, deviceId, gcmToken);

            switch (reason) {
                case REMOVE:
                    remove(registration.getUserId());
                    break;
                case UNREGISTER_AND_REMOVE:
                    boolean unregistered = unreg(context, registration);
                    Logger.d(TAG, "Try to unregister, registration: " + registration + ", result: " + unregistered);
                    if (unregistered) {
                        remove(registration.getUserId());
                    }
                    break;
                case OK:
                    hasCurrentAccount = true;
                    break;
            }
        }

        if (!hasCurrentAccount && currentAccountId != ISettings.IAccountsSettings.INVALID_ID) {
            VkPushRegistration registration = register(currentAccountId, deviceId, gcmToken);

            Logger.d(TAG, "Try to register current account, result: " + registration);
        }

        boolean validNow = validate(context);

        Logger.d(TAG, "validNow: " + validNow + ", time: " + (System.currentTimeMillis() - start) + " ms");

        return null;
    }

    public static boolean validate(Context context) {
        int currentAccountId = Settings.get()
                .accounts()
                .getCurrent();

        List<VkPushRegistration> registrations = Settings.get()
                .pushSettings()
                .getRegistrations();

        try {
            return validate(registrations, currentAccountId, Utils.getDiviceId(context), getGcmToken(context));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean validate(@NonNull List<VkPushRegistration> registrations, int currentAccountId, @NonNull String deviceId, @NonNull String gcmToken) {
        boolean hasCurrentAccount = false;
        for (VkPushRegistration registration : registrations) {
            Reason reason = resolveRegistration(registration, currentAccountId, deviceId, gcmToken);
            switch (reason) {
                case UNREGISTER_AND_REMOVE:
                    return false;
                case OK:
                    hasCurrentAccount = true;
                    break;
            }
        }

        return hasCurrentAccount || currentAccountId == ISettings.IAccountsSettings.INVALID_ID;
    }

    private static void remove(int userId) {
        Logger.d(TAG, "Remove registration record from prefs, user_id: " + userId);

        List<VkPushRegistration> registrations = Settings.get()
                .pushSettings()
                .getRegistrations();

        Iterator<VkPushRegistration> iterator = registrations.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUserId() == userId) {
                iterator.remove();
            }
        }

        Settings.get()
                .pushSettings()
                .savePushRegistations(registrations);
    }

    private static void put(@NonNull VkPushRegistration registration) {
        Logger.d(TAG, "Add registration record, registration: " + registration);

        List<VkPushRegistration> registrations = Settings.get()
                .pushSettings()
                .getRegistrations();

        Iterator<VkPushRegistration> iterator = registrations.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getUserId() == registration.getUserId()) {
                iterator.remove();
            }
        }

        registrations.add(registration);

        Settings.get()
                .pushSettings()
                .savePushRegistations(registrations);
    }

    private static Reason resolveRegistration(@NonNull VkPushRegistration registration,
                                              int currentAccountId, @NonNull String deviceId, @NonNull String gcmToken) {
        if (!deviceId.equals(registration.getDeviceId())) {
            return Reason.REMOVE;
        }

        if (registration.getUserId() != currentAccountId) {
            return Reason.UNREGISTER_AND_REMOVE;
        }

        if (!gcmToken.equals(registration.getGmcToken())) {
            return Reason.REMOVE;
        }

        String currentVkToken = Settings.get()
                .accounts()
                .getAccessToken(currentAccountId);

        if (!registration.getVkToken().equals(currentVkToken)) {
            return Reason.REMOVE;
        }

        return Reason.OK;
    }

    private enum Reason {
        OK, REMOVE, UNREGISTER_AND_REMOVE
    }

    private static String getGcmToken(Context context) throws IOException {
        InstanceID instanceID = InstanceID.getInstance(context);
        return instanceID.getToken(Constants.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
    }

    private VkPushRegistration register(int accountId, String deviceId, String token) {
        try {
            JSONArray fr_of_fr = new JSONArray();
            fr_of_fr.put("fr_of_fr");

            JSONObject json = new JSONObject();
            json.put("msg", "on"); // личные сообщения +
            json.put("chat", "on"); // групповые чаты +
            json.put("wall_post", "on"); // новая запись на стене пользователя +
            json.put("comment", "on"); // комментарии +
            json.put("reply", "on"); // ответы +
            json.put("wall_publish", "on"); // размещение предложенной новости +
            json.put("friend", "on");  // запрос на добавления в друзья +
            json.put("friend_accepted", "on"); // подтверждение заявки в друзья +
            json.put("group_invite", "on"); // приглашение в сообщество +
            json.put("birthday", "on"); // уведомления о днях рождениях на текущую дату

            //(хер приходят)
            json.put("like", "on"); // отметки "Мне нравится"
            json.put("group_accepted", fr_of_fr); // подтверждение заявки на вступление в группу - (хер приходят) 09.01.2016
            json.put("mention", fr_of_fr); // упоминания - (хер приходят) 09.01.2016
            json.put("repost", fr_of_fr); // действия "Рассказать друзьям" - (хер приходят) 09.01.2016

            json.put("new_post", "on"); //записи выбранных людей и сообществ;

            String targetSettingsStr = json.toString();
            Logger.d(TAG, "targetSettingsStr: " + targetSettingsStr);

            String vktoken = Settings.get()
                    .accounts()
                    .getAccessToken(accountId);

            String deviceModel = Utils.getDeviceName();
            String osVersion = Utils.getAndroidVersion();

            boolean success = Apis.get()
                    .vkManual(accountId, vktoken)
                    .account()
                    .registerDevice(token, deviceModel, null, deviceId, osVersion, targetSettingsStr)
                    .blockingGet();

            if (success) {
                VkPushRegistration registration = new VkPushRegistration(accountId, deviceId, vktoken, token);
                put(registration);
                return registration;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean unreg(Context context, VkPushRegistration registration) {
        String deviceId = Utils.getDiviceId(context);

        try {
            return Apis.get()
                    .vkManual(registration.getUserId(), registration.getVkToken())
                    .account()
                    .unregisterDevice(deviceId)
                    .blockingGet();
        } catch (Exception e) {
            ServiceException exception = prepareException(context, e);
            exception.printStackTrace();

            if(exception instanceof ApiServiceException &&
                    ((ApiServiceException) exception).getCode() == ApiErrorCodes.USER_AUTHORIZATION_FAILED){
                // потому что токен уже неактуален и может быть удален
                return true;
            }


            return false;
        }
    }
}

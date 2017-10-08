package biz.dealnote.messenger.push;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biz.dealnote.messenger.api.ApiException;
import biz.dealnote.messenger.api.interfaces.INetworker;
import biz.dealnote.messenger.service.ApiErrorCodes;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.VkPushRegistration;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class PushRegistrationResolver implements IPushRegistrationResolver {

    private static final String TAG = PushRegistrationResolver.class.getSimpleName();

    private final IGcmTokenProvider gcmTokenProvider;
    private final IDevideIdProvider devideIdProvider;
    private final ISettings settings;
    private final INetworker networker;

    public PushRegistrationResolver(IGcmTokenProvider gcmTokenProvider, IDevideIdProvider devideIdProvider, ISettings settings, INetworker networker) {
        this.gcmTokenProvider = gcmTokenProvider;
        this.devideIdProvider = devideIdProvider;
        this.settings = settings;
        this.networker = networker;
    }

    @Override
    public boolean canReceivePushNotification() {
        int accountId = settings.accounts().getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return false;
        }

        final List<VkPushRegistration> available = settings.pushSettings().getRegistrations();
        boolean can = available.size() == 1 && available.get(0).getUserId() == accountId;

        Logger.d(TAG, "canReceivePushNotification, reason: " + String.valueOf(can).toUpperCase());
        return can;
    }

    @Override
    public Completable resolvePushRegistration() {
        return getInfo()
                .flatMapCompletable(data -> {
                    final List<VkPushRegistration> available = settings.pushSettings().getRegistrations();

                    int accountId = settings.accounts().getCurrent();
                    boolean hasAuth = accountId != ISettings.IAccountsSettings.INVALID_ID;

                    if (!hasAuth && available.isEmpty()) {
                        Logger.d(TAG, "No auth, no regsitrations, OK");
                        return Completable.complete();
                    }

                    Set<VkPushRegistration> needUnregister = new HashSet<>(0);

                    Optional<Integer> optionalAccountId = hasAuth ? Optional.wrap(accountId) : Optional.empty();

                    boolean hasOk = false;
                    boolean hasRemove = false;

                    for (VkPushRegistration registered : available) {
                        Reason reason = analizeRegistration(registered, data, optionalAccountId);

                        Logger.d(TAG, "Reason: " + reason);

                        switch (reason) {
                            case UNREGISTER_AND_REMOVE:
                                needUnregister.add(registered);
                                break;
                            case REMOVE:
                                hasRemove = true;
                                break;
                            case OK:
                                hasOk = true;
                                break;
                        }
                    }

                    if (hasAuth && hasOk && !hasRemove && needUnregister.isEmpty()) {
                        Logger.d(TAG, "Has auth, valid registration, OK");
                        return Completable.complete();
                    }

                    Completable completable = Completable.complete();

                    for (VkPushRegistration unreg : needUnregister) {
                        completable = completable.andThen(unregister(unreg));
                    }

                    final List<VkPushRegistration> target = new ArrayList<>();

                    if (!hasOk && hasAuth) {
                        final String vkToken = settings.accounts().getAccessToken(accountId);
                        final VkPushRegistration current = new VkPushRegistration(accountId, data.deviceId, vkToken, data.gcmToken);
                        target.add(current);

                        completable = completable.andThen(register(current));
                    }

                    return completable
                            .doOnComplete(() -> settings.pushSettings().savePushRegistations(target))
                            .doOnComplete(() -> Logger.d(TAG, "Register success"))
                            .doOnError(throwable -> Logger.d(TAG, "Register error, t: " + throwable));
                });
    }

    private Completable register(VkPushRegistration registration) {
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

            final String targetSettingsStr = json.toString();
            final String deviceModel = Utils.getDeviceName();
            final String osVersion = Utils.getAndroidVersion();

            return networker.vkManual(registration.getUserId(), registration.getVkToken())
                    .account()
                    .registerDevice(registration.getGmcToken(), deviceModel, null, registration.getDeviceId(), osVersion, targetSettingsStr)
                    .toCompletable();
        } catch (JSONException e) {
            return Completable.error(e);
        }
    }

    private Completable unregister(VkPushRegistration registration) {
        return networker.vkManual(registration.getUserId(), registration.getVkToken())
                .account()
                .unregisterDevice(registration.getDeviceId())
                .toCompletable()
                .onErrorResumeNext(t -> {
                    Throwable cause = getCauseIfRuntime(t);

                    if (cause instanceof ApiException && ((ApiException) cause).getError().errorCode == ApiErrorCodes.USER_AUTHORIZATION_FAILED) {
                        return Completable.complete();
                    }

                    return Completable.error(t);
                });
    }

    private static final class Data {

        final String gcmToken;

        final String deviceId;

        Data(String gcmToken, String deviceId) {
            this.gcmToken = gcmToken;
            this.deviceId = deviceId;
        }
    }

    private Reason analizeRegistration(VkPushRegistration available, Data data, Optional<Integer> optionAccountId) {
        if (!data.deviceId.equals(available.getDeviceId())) {
            return Reason.REMOVE;
        }

        if (!data.gcmToken.equals(available.getGmcToken())) {
            return Reason.REMOVE;
        }

        if (optionAccountId.isEmpty()) {
            return Reason.UNREGISTER_AND_REMOVE;
        }

        int currentAccountId = optionAccountId.get();

        if (available.getUserId() != currentAccountId) {
            return Reason.UNREGISTER_AND_REMOVE;
        }

        String currentVkToken = settings.accounts().getAccessToken(currentAccountId);

        if (!available.getVkToken().equals(currentVkToken)) {
            return Reason.REMOVE;
        }

        return Reason.OK;
    }

    private enum Reason {
        OK, REMOVE, UNREGISTER_AND_REMOVE
    }

    private Single<Data> getInfo() {
        return Single.fromCallable(() -> {
            String deviceId = devideIdProvider.getDeviceId();
            String gcmToken = gcmTokenProvider.getToken();
            return new Data(gcmToken, deviceId);
        });
    }

    private VkPushRegistration createCurrent() throws IOException {
        int accountId = settings.accounts().getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return null;
        }

        String deviceId = devideIdProvider.getDeviceId();
        String gcmToken = gcmTokenProvider.getToken();
        String vkToken = settings.accounts().getAccessToken(accountId);
        return new VkPushRegistration(accountId, deviceId, vkToken, gcmToken);
    }
}
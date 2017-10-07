package biz.dealnote.messenger.settings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.push.IPushRegistrationResolver;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.Observable;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

/**
 * Created by ruslan.kolbasa on 02.12.2016.
 * phoenix
 */
class AccountsSettings implements ISettings.IAccountsSettings {

    private static final String WHAT_ACCOUNT_CHANGE = "biz.dealnote.messenger.WHAT_ACCOUNT_CHANGE";
    private static final String KEY_ACCOUNT_UIDS = "account_uids";
    private static final String KEY_CURRENT = "current_account_id";

    private final Context app;

    private SharedPreferences preferences;
    private Map<Integer, String> tokens;

    @SuppressLint("UseSparseArrays")
    AccountsSettings(Context context) {
        this.app = context.getApplicationContext();
        this.tokens = Collections.synchronizedMap(new HashMap<>(1));
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Collection<Integer> aids = getRegistered();
        for (Integer aid : aids) {
            String token = preferences.getString(tokenKeyFor(aid), null);

            if (nonEmpty(token)) {
                tokens.put(aid, token);
            }
        }
    }

    private static String tokenKeyFor(int uid) {
        return "token" + uid;
    }

    @Override
    public Observable<Integer> observeChanges() {
        return Observable.create(e -> {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (nonNull(intent) && WHAT_ACCOUNT_CHANGE.equals(intent.getAction())) {
                        e.onNext(intent.getExtras().getInt(Extra.ACCOUNT_ID));
                    }
                }
            };

            e.setCancellable(() -> LocalBroadcastManager.getInstance(app).unregisterReceiver(receiver));
            if (!e.isDisposed()) {
                LocalBroadcastManager.getInstance(app).registerReceiver(receiver, new IntentFilter(WHAT_ACCOUNT_CHANGE));
            }
        });
    }

    @NonNull
    @Override
    public List<Integer> getRegistered() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        Set<String> uids = preferences.getStringSet(KEY_ACCOUNT_UIDS, new HashSet<>(0));

        List<Integer> ids = new ArrayList<>(uids.size());
        for (String stringuid : uids) {
            int uid = Integer.parseInt(stringuid);
            ids.add(uid);
        }

        return ids;
    }

    @Override
    public void setCurrent(int accountId) {
        if (getCurrent() == accountId) return;

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_CURRENT, accountId)
                .apply();
        fireAccountChange();
    }

    private void fireAccountChange() {
        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();
        registrationResolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {}, Throwable::printStackTrace);

        //RestRequestManager.from(app).execute(AccountRequestFactory.getResolvePushRegistrationRequest(), DUMMY_REQUEST_ADAPTER);

        Intent intent = new Intent(WHAT_ACCOUNT_CHANGE);
        intent.putExtra(Extra.ACCOUNT_ID, getCurrent());

        LocalBroadcastManager.getInstance(app).sendBroadcast(intent);
    }

    @Override
    public int getCurrent() {
        return preferences.getInt(KEY_CURRENT, INVALID_ID);
    }

    @NonNull
    private Set<String> copyUidsSet() {
        return new HashSet<>(preferences.getStringSet(KEY_ACCOUNT_UIDS, new HashSet<>(1)));
    }

    @Override
    public void remove(int accountId) {
        int currentAccountId = getCurrent();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        Set<String> uids = copyUidsSet();

        uids.remove(String.valueOf(accountId));
        preferences.edit()
                .putStringSet(KEY_ACCOUNT_UIDS, uids)
                .apply();

        if (accountId == currentAccountId) {
            List<Integer> accountIds = getRegistered();

            Integer fisrtUserAccountId = null;

            // делаем активным первый аккаунт ПОЛЬЗОВАТЕЛЯ
            for (Integer existsId : accountIds) {
                if (existsId > 0) {
                    fisrtUserAccountId = existsId;
                    break;
                }
            }

            if (nonNull(fisrtUserAccountId)) {
                preferences.edit()
                        .putInt(KEY_CURRENT, fisrtUserAccountId)
                        .apply();
            } else {
                preferences.edit()
                        .remove(KEY_CURRENT)
                        .apply();
            }
        }

        fireAccountChange();
    }

    @Override
    public void registerAccountId(int accountId, boolean setCurrent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);

        Set<String> uids = copyUidsSet();
        uids.add(String.valueOf(accountId));

        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(KEY_ACCOUNT_UIDS, uids);

        if (setCurrent) {
            editor.putInt(KEY_CURRENT, accountId);
        }

        editor.apply();

        if (setCurrent) {
            fireAccountChange();
        }
    }

    @Override
    public void storeAccessToken(int accountId, String accessToken) {
        tokens.put(accountId, accessToken);
        preferences.edit()
                .putString(tokenKeyFor(accountId), accessToken)
                .apply();
    }

    @Override
    public String getAccessToken(int accountId) {
        return tokens.get(accountId);
    }

    @Override
    public void removeAccessToken(int accountId) {
        tokens.remove(accountId);
        preferences.edit()
                .remove(tokenKeyFor(accountId))
                .apply();
    }
}
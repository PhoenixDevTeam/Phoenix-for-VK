package biz.dealnote.messenger.longpoll;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.longpoll.model.AbsRealtimeAction;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import io.reactivex.Observable;

public class LongpollUtils {

    public static final String TAG = LongpollUtils.class.getSimpleName();

    public static void register(Context context, int accountId, int peerId, Integer unregAccountId, Integer unregPeerId) {
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) return;

        Intent intent = new Intent(context, LongpollService.class);
        intent.setAction(LongpollService.ACTION_REGISTER);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);

        if (unregAccountId != null && unregPeerId != null) {
            intent.putExtra("unreg_aid", unregAccountId);
            intent.putExtra("unreg_pid", unregPeerId);
        }

        context.startService(intent);
    }

    public static void unregister(Context context, int accountId, int peerId) {
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) return;

        Intent intent = new Intent(context, LongpollService.class);
        intent.setAction(LongpollService.ACTION_UNREGISTER);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        context.startService(intent);
    }

    //public static void fireGcmMessage(Context context, Message message) {
    //    Intent intent = new Intent(context, LongpollService.class);
    //    intent.setAction(LongpollService.ACTION_GCM_MESSAGE);
    //    intent.putExtra(Extra.MESSAGE, message);
    //    context.startService(intent);
    //}

    public static Observable<List<AbsRealtimeAction>> observeUpdates(@NonNull Context context) {
        Context app = context.getApplicationContext();
        return Observable.create(e -> {
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context1, Intent intent) {
                    ArrayList<AbsRealtimeAction> actions = intent
                            .getParcelableArrayListExtra(LongpollService.EXTRA_REALTIME_ACTIONS);
                    AssertUtils.requireNonNull(actions);
                    e.onNext(actions);
                }
            };

            e.setCancellable(() -> {
                LocalBroadcastManager.getInstance(app).unregisterReceiver(receiver);
                Logger.d(TAG, "Dispose");
            });

            if (!e.isDisposed()) {
                LocalBroadcastManager.getInstance(app).registerReceiver(receiver,
                        new IntentFilter(LongpollService.WHAT_REALTIME_ACTIONS));
                Logger.d(TAG, "Register");
            }
        });
    }
}

package biz.dealnote.messenger.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.MainActivity;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.Peer;
import io.reactivex.Completable;
import io.reactivex.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class ShortcutUtils {

    private static final String SHURTCUT_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";

    private static int getLauncherIconSize(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher).getWidth();
    }

    public static void createAccountShurtcut(Context context, int accountId, String title, String url) throws IOException {
        //Bitmap immutableIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        //Bitmap mutableBitmap = immutableIcon.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap avatar = null;

        if (nonEmpty(url)) {
            int size = getLauncherIconSize(context);

            //int size = getLauncherIconSize(mContext);
            //int size = mutableBitmap.getWidth();
            //int avatarSize = (int) (size / 2.6);

            avatar = PicassoInstance.with()
                    .load(url)
                    .transform(new RoundTransformation())
                    .resize(size, size)
                    .get();

            //Canvas canvas = new Canvas(mutableBitmap);
            //Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            //canvas.drawBitmap(avatar, size - avatarSize, size - avatarSize, paint);

            //canvas.drawBitmap(avatar, 0, 0, paint);

           // avatar.recycle();
        }

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SWITH_ACCOUNT);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);

        String id = "phoenix_account_" + accountId;

        sendShortcutBroadcast(context, id, intent, title, avatar);
    }

    private static void sendShortcutBroadcast(Context context, String shortcutId, Intent shortcutIntent, String title, Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Icon icon = Icon.createWithBitmap(bitmap);

            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, shortcutId)
                            .setIcon(icon)
                            .setShortLabel(title)
                            .setIntent(shortcutIntent)
                            .build();

            ShortcutManager manager = context.getSystemService(ShortcutManager.class);
            if (manager != null) {
                manager.requestPinShortcut(shortcutInfo, null);
            }
        } else {
            final Context app = context.getApplicationContext();

            Intent intent = new Intent();
            intent.setAction(SHURTCUT_ACTION);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
            context.sendBroadcast(intent);

            Completable.complete()
                    .observeOn(Injection.provideMainThreadScheduler())
                    .subscribe(() -> Toast.makeText(app, R.string.success, Toast.LENGTH_SHORT).show());
        }
    }

    public static Intent chatOpenIntent(Context context, String url, int accoutnId, int peerId, String title) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_CHAT_FROM_SHORTCUT);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(Extra.IMAGE, url);
        intent.putExtra(Extra.ACCOUNT_ID, accoutnId);
        return intent;
    }

    public static void createChatShortcut(Context context, String url, int accoutnId, int peerId, String title) throws IOException {
        String id = "phoenix_peer_" + peerId + "_aid_" + accoutnId;

        Bitmap bm = createBitmap(context, url);
        Intent intent = chatOpenIntent(context, url, accoutnId, peerId, title);
        sendShortcutBroadcast(context, id, intent, title, bm);
    }

    public static Completable createChatShortcutRx(Context context, String url, int accoutnId, int peerId, String title) {
        return Completable.create(e -> {
            try {
                createChatShortcut(context, url, accoutnId, peerId, title);
            } catch (Exception e1) {
                e.onError(e1);
            }

            e.onComplete();
        });
    }

    private static Single<Bitmap> loadRoundAvatar(Context context, String url) {
        return Single.fromCallable(() -> PicassoInstance.with()
                .load(url)
                .transform(new RoundTransformation())
                .get());
    }

    private static final int MAX_DYNAMIC_COUNT = 5;

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static Completable addDynamicShortcut(Context context, int accountId, Peer peer) {
        final Context app = context.getApplicationContext();

        return loadRoundAvatar(app, peer.getAvaUrl())
                .flatMapCompletable(bitmap -> Completable.fromAction(() -> {
                    ShortcutManager manager = app.getSystemService(ShortcutManager.class);
                    List<ShortcutInfo> infos = new ArrayList<>(manager.getDynamicShortcuts());

                    /*ShortcutInfo createPost = new ShortcutInfo.Builder(app, "create_new_post")
                            .setShortLabel(app.getString(R.string.new_post_title))
                            .setIcon(Icon.createWithResource(app, R.mipmap.ic_home_indigo))
                            .setIntent(new Intent(app, MainActivity.class).setAction(MainActivity.ACTION_CREATE_POST))
                            .setRank(0)
                            .build();

                    infos.set(0, createPost);*/

                    List<String> mustBeRemoved = new ArrayList<>(1);

                    if(infos.size() >= MAX_DYNAMIC_COUNT){
                        Collections.sort(infos, (o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()));

                        ShortcutInfo infoWhichMustBeRemoved = infos.get(infos.size() - 1);
                        mustBeRemoved.add(infoWhichMustBeRemoved.getId());
                    }

                    final String title = peer.getTitle();
                    final String id = "chat" + peer.getId();
                    final String avaurl = peer.getAvaUrl();
                    final Intent intent = chatOpenIntent(app, avaurl, accountId, peer.getId(), title);

                    int rank = 0;

                    ShortcutInfo.Builder builder = new ShortcutInfo.Builder(app, id)
                            .setShortLabel(title)
                            .setIntent(intent)
                            .setRank(rank);

                    if (nonNull(bitmap)) {
                        Icon icon = Icon.createWithBitmap(bitmap);
                        builder.setIcon(icon);
                    }

                    if(!mustBeRemoved.isEmpty()){
                        manager.removeDynamicShortcuts(mustBeRemoved);
                    }

                    manager.addDynamicShortcuts(Collections.singletonList(builder.build()));
                }));
    }

    private static Bitmap createBitmap(Context mContext, String url) throws IOException {
        int appIconSize = getLauncherIconSize(mContext);
        Bitmap bm;

        Transformation transformation = new RoundTransformation();

        if (isEmpty(url)) {
            bm = PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .resize(appIconSize, appIconSize)
                    .get();
        } else {
            bm = PicassoInstance.with()
                    .load(url)
                    .transform(transformation)
                    .resize(appIconSize, appIconSize)
                    .get();
        }

        return bm;
    }
}

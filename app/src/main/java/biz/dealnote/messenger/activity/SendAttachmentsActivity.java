package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.ViewUtils;

/**
 * Тот же MainActivity, предназначенный для шаринга контента
 * Отличие только в том, что этот активити может существовать в нескольких экземплярах
 */
public class SendAttachmentsActivity extends MainActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // потому, что в onBackPressed к этому числу будут прибавлять 2000 !!!! и выход за границы
        super.mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;
    }

    public static void startForSendAttachments(Context context, int accountId, ModelsBundle bundle) {
        Intent intent = new Intent(context, SendAttachmentsActivity.class);
        intent.setAction(ACTION_SEND_ATTACHMENTS);
        intent.putExtra(EXTRA_INPUT_ATTACHMENTS, bundle);
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
        intent.putExtra(Extra.PLACE, PlaceFactory.getDialogsPlace(accountId, accountId,null));
        context.startActivity(intent);
    }

    public static void startForSendAttachments(Context context, int accountId, AbsModel model) {
        startForSendAttachments(context, accountId, new ModelsBundle(1).append(model));
    }

    @Override
    public void onDestroy(){
        ViewUtils.keyboardHide(this);
        super.onDestroy();
    }
}

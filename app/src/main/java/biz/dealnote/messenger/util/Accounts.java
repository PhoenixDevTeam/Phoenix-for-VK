package biz.dealnote.messenger.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;

public class Accounts {

    public static void showAccountSwitchedToast(@NonNull Activity context) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        if (aid == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        View view = View.inflate(context, R.layout.account_change_toast, null);

        ImageView avatar = view.findViewById(R.id.avatar);
        TextView subtitle = view.findViewById(R.id.subtitle);

        User user;

        try {
            user = (User) InteractorFactory.createOwnerInteractor()
                    .getBaseOwnerInfo(aid, aid, IOwnersInteractor.MODE_CACHE)
                    .blockingGet();
        } catch (Exception e){
            // NotFountException
            return;
        }

        PicassoInstance.with()
                .load(user.getMaxSquareAvatar())
                .transform(CurrentTheme.createTransformationForAvatar(context))
                .into(avatar);

        subtitle.setText(user.getFullName());

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
    }

    public static int fromArgs(Bundle bundle) {
        return bundle == null ? ISettings.IAccountsSettings.INVALID_ID : bundle.getInt(Extra.ACCOUNT_ID);
    }
}
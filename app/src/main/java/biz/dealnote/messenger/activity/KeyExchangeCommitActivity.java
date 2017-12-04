package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.crypt.ExchangeMessage;
import biz.dealnote.messenger.crypt.KeyExchangeService;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.ViewUtils;

/**
 * Created by ruslan.kolbasa on 26.10.2016.
 * phoenix
 */
public class KeyExchangeCommitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get()
                .ui()
                .getQuickReplyTheme());

        setContentView(R.layout.activity_key_exchange_commit);

        int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        int peerId = getIntent().getExtras().getInt(Extra.PEER_ID);

        User user = getIntent().getParcelableExtra(Extra.OWNER);

        int messageId = getIntent().getExtras().getInt(Extra.MESSAGE_ID);
        ExchangeMessage message = getIntent().getParcelableExtra(Extra.MESSAGE);

        ImageView avatar = findViewById(R.id.avatar);
        ViewUtils.displayAvatar(avatar, CurrentTheme.createTransformationForAvatar(this), user.getMaxSquareAvatar(), null);

        TextView userName = findViewById(R.id.user_name);
        userName.setText(user.getFullName());

        findViewById(R.id.accept_button).setOnClickListener(v -> {
            startService(KeyExchangeService.createIntentForApply(KeyExchangeCommitActivity.this, message, accountId, peerId, messageId));
            finish();
        });

        findViewById(R.id.decline_button).setOnClickListener(v -> {
            startService(KeyExchangeService.createIntentForDecline(KeyExchangeCommitActivity.this, message, accountId, peerId, messageId));
            finish();
        });
    }

    public static Intent createIntent(@NonNull Context context, int accountId, int peerId, @NonNull User user, int messageId, @NonNull ExchangeMessage message){
        Intent intent = new Intent(context, KeyExchangeCommitActivity.class);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.OWNER, user);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE_ID, messageId);
        intent.putExtra(Extra.MESSAGE, message);
        return intent;
    }
}

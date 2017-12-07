package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Transformation;

import java.util.concurrent.TimeUnit;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.crypt.KeyLocationPolicy;
import biz.dealnote.messenger.domain.IMessagesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.longpoll.NotificationHelper;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.SaveMessageBuilder;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.service.SendService;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.task.TextingNotifier;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.isEmpty;

public class QuickAnswerActivity extends AppCompatActivity {

    public static final String PARAM_BODY = "body";
    public static final String PARAM_MESSAGE_ID = "message_id";
    public static final String PARAM_LAST_SEEN = "last_seen";
    public static final String PARAM_MESSAGE_SENT_TIME = "message_sent_time";

    public static final String EXTRA_FOCUS_TO_FIELD = "focus_to_field";
    public static final String EXTRA_LIVE_DELAY = "live_delay";

    private EditText etText;
    private int peerId;
    private TextingNotifier notifier;
    private int accountId;

    private boolean mMessageIsRead;
    private IMessagesInteractor mMessagesInteractor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mMessagesInteractor = InteractorFactory.createMessagesInteractor();

        boolean focusToField = getIntent().getBooleanExtra(EXTRA_FOCUS_TO_FIELD, true);

        if (!focusToField) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        setTheme(Settings.get().ui().getQuickReplyTheme());

        accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        peerId = getIntent().getExtras().getInt(Extra.PEER_ID);
        notifier = new TextingNotifier(accountId);

        setContentView(R.layout.activity_quick_answer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(CurrentTheme.getDrawableFromAttribute(this, R.attr.toolbarBackIcon));
        }

        setSupportActionBar(toolbar);

        TextView tvMessage = findViewById(R.id.item_message_text);
        TextView tvTime = findViewById(R.id.item_message_time);
        etText = findViewById(R.id.activity_quick_answer_edit_text);

        ImageView ivAvatar = findViewById(R.id.avatar);

        ImageButton btnToDialog = findViewById(R.id.activity_quick_answer_to_dialog);
        ImageButton btnSend = findViewById(R.id.activity_quick_answer_send);

        String messageTime = AppTextUtils.getDateFromUnixTime(this, getIntent().getLongExtra(PARAM_MESSAGE_SENT_TIME, 0));
        String onlineTime = AppTextUtils.getDateFromUnixTime(this, getIntent().getLongExtra(PARAM_LAST_SEEN, 0));
        final String title = getIntent().getStringExtra(Extra.TITLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setSubtitle(onlineTime);
        }

        tvMessage.setText(getIntent().getStringExtra(PARAM_BODY), TextView.BufferType.SPANNABLE);
        tvTime.setText(messageTime);

        Transformation transformation = CurrentTheme.createTransformationForAvatar(this);
        final String imgUrl = getIntent().getStringExtra(Extra.IMAGE);
        if (ivAvatar != null) {
            ViewUtils.displayAvatar(ivAvatar, transformation, imgUrl, null);
        }

        etText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!mMessageIsRead) {
                    setMessageAsRead();
                    mMessageIsRead = true;
                }

                cancelFinishWithDelay();

                if (Objects.nonNull(notifier)) {
                    notifier.notifyAboutTyping(peerId);
                }
            }
        });

        btnSend.setOnClickListener(view -> send());
        btnToDialog.setOnClickListener(v -> {
            Intent intent = new Intent(QuickAnswerActivity.this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_OPEN_PLACE);

            Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, new Peer(peerId).setAvaUrl(imgUrl).setTitle(title));
            intent.putExtra(Extra.PLACE, chatPlace);
            startActivity(intent);
            finish();
        });

        boolean liveDelay = getIntent().getBooleanExtra(EXTRA_LIVE_DELAY, false);
        if (liveDelay) {
            finishWithDelay();
        }
    }

    private void finishWithDelay() {
        mLiveSubscription.add(Observable.just(new Object())
                .delay(1, TimeUnit.MINUTES)
                .subscribe(o -> finish()));
    }

    private CompositeDisposable mLiveSubscription = new CompositeDisposable();

    private void cancelFinishWithDelay() {
        mLiveSubscription.dispose();
    }

    @Override
    protected void onDestroy() {
        mLiveSubscription.dispose();
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public static Intent forStart(Context context, int accountId, int peerId, String body, int mid, long lastSeen, long messageTime, String imgUrl, String title) {
        Intent intent = new Intent(context, QuickAnswerActivity.class);
        intent.putExtra(PARAM_BODY, body);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(PARAM_MESSAGE_ID, mid);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(PARAM_LAST_SEEN, lastSeen);
        intent.putExtra(PARAM_MESSAGE_SENT_TIME, messageTime);
        intent.putExtra(Extra.IMAGE, imgUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * Отправка сообщения
     */
    private void send() {
        String trimmedtext = etText.getText().toString().trim();
        if (isEmpty(trimmedtext)) {
            Toast.makeText(QuickAnswerActivity.this, getString(R.string.text_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean requireEncryption = Settings.get()
                .security()
                .isMessageEncryptionEnabled(accountId, peerId);

        @KeyLocationPolicy
        int policy = KeyLocationPolicy.PERSIST;

        if (requireEncryption) {
            policy = Settings.get()
                    .security()
                    .getEncryptionLocationPolicy(accountId, peerId);
        }

        final SaveMessageBuilder builder = new SaveMessageBuilder(accountId, peerId)
                .setBody(trimmedtext)
                .setRequireEncryption(requireEncryption)
                .setKeyLocationPolicy(policy);

        mCompositeDisposable.add(mMessagesInteractor.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onMessageSaved, this::onSavingError));
    }

    private void onSavingError(Throwable throwable) {
        Utils.showRedTopToast(this, throwable.toString());
    }

    @SuppressWarnings("unused")
    private void onMessageSaved(Message message) {
        NotificationHelper.tryCancelNotificationForPeer(this, accountId, peerId);

        Intent intent = new Intent(this, SendService.class);
        startService(intent);
        finish();
    }

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private void setMessageAsRead() {
        mCompositeDisposable.add(mMessagesInteractor.markAsRead(accountId, peerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {/*ignore*/}, t -> {/*ignore*/}));
    }
}
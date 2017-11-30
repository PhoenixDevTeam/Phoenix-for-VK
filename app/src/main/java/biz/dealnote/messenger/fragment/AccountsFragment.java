package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.LoginActivity;
import biz.dealnote.messenger.activity.ProxyManagerActivity;
import biz.dealnote.messenger.adapter.AccountAdapter;
import biz.dealnote.messenger.api.Auth;
import biz.dealnote.messenger.db.DBHelper;
import biz.dealnote.messenger.dialog.DirectAuthDialog;
import biz.dealnote.messenger.domain.IAccountsInteractor;
import biz.dealnote.messenger.domain.IOwnersInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.longpoll.LongpollService;
import biz.dealnote.messenger.model.Account;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.ShortcutUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

public class AccountsFragment extends BaseFragment implements View.OnClickListener, AccountAdapter.Callback {

    private TextView empty;
    private RecyclerView mRecyclerView;
    private AccountAdapter mAdapter;
    private ArrayList<Account> mData;

    private IOwnersInteractor mOwnersInteractor;
    private IAccountsInteractor accountsInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        this.mOwnersInteractor = InteractorFactory.createOwnerInteractor();
        this.accountsInteractor = InteractorFactory.createAccountInteractor();

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(SAVE_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_accounts, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        empty = root.findViewById(R.id.empty);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        root.findViewById(R.id.fab).setOnClickListener(this);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new AccountAdapter(getActivity(), mData, this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            load();
        }

        resolveEmptyText();
    }

    private void resolveEmptyText() {
        if (!isAdded() || empty == null) return;
        empty.setVisibility(Utils.safeIsEmpty(mData) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setSubtitle(null);
        }
    }

    private static final String SAVE_DATA = "save_data";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, mData);
    }

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    private void load() {
        mCompositeDisposable.add(accountsInteractor
                .getAll()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(appAccounts -> {
                    mData.clear();
                    mData.addAll(appAccounts);

                    if (Objects.nonNull(mAdapter)) {
                        mAdapter.notifyDataSetChanged();
                    }

                    resolveEmptyText();
                }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == Activity.RESULT_OK) {
                    int uid = data.getExtras().getInt(Extra.USER_ID);
                    String token = data.getStringExtra(Extra.TOKEN);
                    processNewAccount(uid, token);
                }

                break;

            case REQEUST_DIRECT_LOGIN:
                if (resultCode == Activity.RESULT_OK) {
                    if (DirectAuthDialog.ACTION_LOGIN_VIA_WEB.equals(data.getAction())) {
                        startLoginViaWeb();
                    } else if (DirectAuthDialog.ACTION_LOGIN_COMPLETE.equals(data.getAction())) {
                        int uid = data.getExtras().getInt(Extra.USER_ID);
                        String token = data.getStringExtra(Extra.TOKEN);
                        processNewAccount(uid, token);
                    }
                }
                break;
        }
    }

    private int indexOf(int uid) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId() == uid) {
                return i;
            }
        }

        return -1;
    }

    private void merge(Account account) {
        int index = indexOf(account.getId());

        if (index != -1) {
            mData.set(index, account);
        } else {
            mData.add(account);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        resolveEmptyText();
    }

    private void processNewAccount(final int uid, final String token) {
        //Accounts account = new Accounts(token, uid);

        // важно!! Если мы получили новый токен, то необходимо удалить запись
        // о регистрации push-уведомлений
        //PushSettings.unregisterFor(getContext(), account);

        Settings.get()
                .accounts()
                .storeAccessToken(uid, token);

        Settings.get()
                .accounts()
                .registerAccountId(uid, true);

        merge(new Account(uid, null));

        mCompositeDisposable.add(mOwnersInteractor.getBaseOwnerInfo(uid, uid, IOwnersInteractor.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owner -> merge(new Account(uid, owner)), t -> {/*ignored*/}));
    }

    private static final int REQUEST_LOGIN = 107;
    private static final int REQEUST_DIRECT_LOGIN = 108;

    private void startLoginViaWeb() {
        Intent intent = LoginActivity.createIntent(getActivity(), String.valueOf(Constants.API_ID), Auth.getScope());
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    private void startDirectLogin() {
        DirectAuthDialog.newInstance()
                .targetTo(this, REQEUST_DIRECT_LOGIN)
                .show(getFragmentManager(), "direct-login");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startDirectLogin();
                break;
        }
    }

    private void delete(Account account) {
        Settings.get()
                .accounts()
                .removeAccessToken(account.getId());

        Settings.get()
                .accounts()
                .remove(account.getId());

        DBHelper.removeDatabaseFor(getContext(), account.getId());

        stopLongpoll();

        mData.remove(account);
        mAdapter.notifyDataSetChanged();
        resolveEmptyText();
    }

    private void stopLongpoll() {
        getContext().stopService(new Intent(getActivity(), LongpollService.class));
    }

    private void setAsActive(Account account) {
        Settings.get()
                .accounts()
                .setCurrent(account.getId());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(final Account account) {
        boolean idCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        String[] items;

        if (account.getId() > 0) {
            if (idCurrent) {
                items = new String[]{getString(R.string.delete), getString(R.string.add_to_home_screen)};
            } else {
                items = new String[]{getString(R.string.delete), getString(R.string.add_to_home_screen), getString(R.string.set_as_active)};
            }
        } else {
            items = new String[]{getString(R.string.delete)};
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(account.getDisplayName())
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            delete(account);
                            break;
                        case 1:
                            createShortcut(account);
                            break;
                        case 2:
                            setAsActive(account);
                            break;
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            getActivity().finish();
            return true;
        }

        if (item.getItemId() == R.id.privacy_policy) {
            showPrivacyPolicy();
            return true;
        }

        if (item.getItemId() == R.id.action_proxy) {
            startProxySettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startProxySettings() {
        startActivity(new Intent(getActivity(), ProxyManagerActivity.class));
    }

    private void showPrivacyPolicy() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.PRIVACY_POLICY_LINK));
        startActivity(browserIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_accounts, menu);
    }

    private void createShortcut(final Account account) {
        if (account.getId() < 0) {
            return; // this is comminity
        }

        User user = (User) account.getOwner();

        final Context app = getActivity().getApplicationContext();
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String avaUrl = user == null ? null : user.getMaxSquareAvatar();
                try {
                    ShortcutUtils.createAccountShurtcut(app, account.getId(), account.getDisplayName(), avaUrl);
                } catch (IOException e) {
                    return e.getMessage();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (isAdded() && !TextUtils.isEmpty(s)) {
                    Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
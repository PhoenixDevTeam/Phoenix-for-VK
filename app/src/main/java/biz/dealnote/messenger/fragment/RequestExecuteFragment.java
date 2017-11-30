package biz.dealnote.messenger.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.mvp.presenter.RequestExecutePresenter;
import biz.dealnote.messenger.mvp.view.IRequestExecuteView;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by Ruslan Kolbasa on 05.07.2017.
 * phoenix
 */
public class RequestExecuteFragment extends BasePresenterFragment<RequestExecutePresenter, IRequestExecuteView> implements IRequestExecuteView {

    private static final int REQUEST_PERMISSION_WRITE = 14;

    public static RequestExecuteFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        RequestExecuteFragment fragment = new RequestExecuteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private EditText mResposeBody;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_executor, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        mResposeBody = (EditText) root.findViewById(R.id.response_body);

        EditText methodEditText = (EditText) root.findViewById(R.id.method);
        methodEditText.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireMethodEdit(s);
            }
        });

        EditText bodyEditText = (EditText) root.findViewById(R.id.body);
        bodyEditText.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireBodyEdit(s);
            }
        });

        root.findViewById(R.id.button_copy).setOnClickListener(v -> getPresenter().fireCopyClick());
        root.findViewById(R.id.button_save).setOnClickListener(v -> getPresenter().fireSaveClick());
        root.findViewById(R.id.button_execute).setOnClickListener(v -> getPresenter().fireExecuteClick());
        return root;
    }

    @Override
    public IPresenterFactory<RequestExecutePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RequestExecutePresenter(getArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof OnSectionResumeCallback){
            ((OnSectionResumeCallback)getActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(actionBar != null){
            actionBar.setTitle(R.string.request_executor_title);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    protected String tag() {
        return RequestExecuteFragment.class.getSimpleName();
    }

    @Override
    public void displayBody(String body) {
        safelySetText(mResposeBody, body);
    }

    @Override
    public void hideKeyboard() {
        ActivityUtils.hideSoftKeyboard(getActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_WRITE){
            getPresenter().fireWritePermissionResolved();
        }
    }

    @Override
    public void requestWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
    }
}
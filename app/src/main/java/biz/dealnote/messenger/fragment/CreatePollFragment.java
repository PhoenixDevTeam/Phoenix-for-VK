package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.CreatePollPresenter;
import biz.dealnote.messenger.mvp.view.ICreatePollView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 20.12.2016.
 * phoenix
 */
public class CreatePollFragment extends BasePresenterFragment<CreatePollPresenter, ICreatePollView> implements ICreatePollView {

    public static CreatePollFragment newInstance(Bundle args) {
        CreatePollFragment fragment = new CreatePollFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        return args;
    }

    private EditText mQuestion;
    private CheckBox mAnonymous;
    private ViewGroup mOptionsViewGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_poll, container, false);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        mQuestion = (EditText) root.findViewById(R.id.dialog_poll_create_question);
        mAnonymous = (CheckBox) root.findViewById(R.id.dialog_poll_create_anonymous);
        mOptionsViewGroup = (ViewGroup) root.findViewById(R.id.dialog_poll_create_options);

        for (int i = 0; i < mOptionsViewGroup.getChildCount(); i++) {
            EditText editText = (EditText) mOptionsViewGroup.getChildAt(i);
            final int finalI = i;

            editText.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    getPresenter().fireOptionEdited(finalI, s);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s) || finalI == mOptionsViewGroup.getChildCount() - 1) {
                        return;
                    }

                    EditText next = (EditText) mOptionsViewGroup.getChildAt(finalI + 1);
                    if (next.getVisibility() == View.GONE) {
                        next.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        mQuestion.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireQuestionEdited(s);
            }
        });

        mAnonymous.setOnCheckedChangeListener((compoundButton, b) -> getPresenter().fireAnonyamousChecked(b));
        return root;
    }

    @Override
    public IPresenterFactory<CreatePollPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CreatePollPresenter(getArguments().getInt(Extra.ACCOUNT_ID), getArguments().getInt(Extra.OWNER_ID), saveInstanceState);
    }

    @Override
    protected String tag() {
        return CreatePollFragment.class.getSimpleName();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);

        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.new_poll);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(true)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void displayQuestion(String question) {
        if (nonNull(mQuestion)) {
            mQuestion.setText(question);
        }
    }

    @Override
    public void setAnonymous(boolean anomymous) {
        if (nonNull(mAnonymous)) {
            mAnonymous.setChecked(anomymous);
        }
    }

    @Override
    public void displayOptions(String[] options) {
        if (nonNull(mOptionsViewGroup)) {
            for (int i = 0; i < mOptionsViewGroup.getChildCount(); i++) {
                EditText editText = (EditText) mOptionsViewGroup.getChildAt(i);
                editText.setVisibility(View.VISIBLE);
                editText.setText(options[i]);
            }

            for (int u = mOptionsViewGroup.getChildCount() - 2; u >= 0; u--) {
                if (u == 1) {
                    break;
                }

                if (TextUtils.isEmpty(options[u])) {
                    mOptionsViewGroup.getChildAt(u + 1).setVisibility(View.GONE);
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.add_menu):
                getPresenter().fireDoneClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_menu, menu);
    }

    @Override
    public void showQuestionError(@StringRes int message) {
        if (nonNull(mQuestion)) {
            mQuestion.setError(getString(message));
            mQuestion.requestFocus();
        }
    }

    @Override
    public void showOptionError(int index, @StringRes int message) {
        if (nonNull(mOptionsViewGroup)) {
            ((EditText) mOptionsViewGroup.getChildAt(index)).setError(getString(message));
            mOptionsViewGroup.getChildAt(index).requestFocus();
        }
    }

    @Override
    public void sendResultAndGoBack(@NonNull Poll poll) {
        if(nonNull(getTargetFragment())){
            Intent intent = new Intent();
            intent.putExtra(Extra.POLL, poll);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }

        getActivity().onBackPressed();
    }
}

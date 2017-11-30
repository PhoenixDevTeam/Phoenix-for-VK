package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.PollAnswersAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.model.Poll;
import biz.dealnote.messenger.mvp.presenter.PollPresenter;
import biz.dealnote.messenger.mvp.view.IPollView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.view.ProgressButton;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Created by admin on 19.12.2016.
 * phoenix
 */
public class PollFragment extends BasePresenterFragment<PollPresenter, IPollView>
        implements IPollView, PollAnswersAdapter.OnAnswerChangedCallback {

    public static Bundle buildArgs(int aid, Poll poll){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.POLL, poll);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static PollFragment newInstance(Bundle bundle) {
        PollFragment fragment = new PollFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private TextView mQuestion;
    private TextView mVotesCount;
    private PollAnswersAdapter mAnswersAdapter;
    private ProgressButton mButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_poll, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAnswersAdapter = new PollAnswersAdapter(getActivity(), Collections.emptyList());
        mAnswersAdapter.setListener(this);

        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header_poll, recyclerView, false);
        mAnswersAdapter.addHeader(header);

        mQuestion = header.findViewById(R.id.title);
        mVotesCount = header.findViewById(R.id.votes_count);

        mButton = root.findViewById(R.id.button);
        mButton.setOnClickListener(view -> getPresenter().fireButtonClick());

        recyclerView.setAdapter(mAnswersAdapter);
        return root;
    }

    @Override
    public void displayQuestion(String title) {
        if(nonNull(mQuestion)){
            mQuestion.setText(title);
        }
    }

    @Override
    public void displayType(boolean anonymous) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            actionBar.setTitle(anonymous ? R.string.anonymous_poll : R.string.open_poll);
        }
    }

    @Override
    public void displayCreationTime(long unixtime) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            String formattedDate = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    .format(new Date(unixtime * 1000));
            actionBar.setSubtitle(formattedDate);
        }
    }

    @Override
    public void displayVoteCount(int count) {
        if(nonNull(mVotesCount)){
            mVotesCount.setText(getString(R.string.votes_count, count));
        }
    }

    @Override
    public void displayVotesList(List<Poll.Answer> answers, boolean canCheck, Integer myVoteId) {
        if(nonNull(mAnswersAdapter)){
            mAnswersAdapter.setCheckable(canCheck);
            mAnswersAdapter.setChecked(isNull(myVoteId) ? 0 : myVoteId);
            mAnswersAdapter.setItems(answers);
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if(nonNull(mButton)){
            mButton.changeState(loading);
        }
    }

    @Override
    public void setupButton(boolean voted) {
        if(nonNull(mButton)){
            mButton.setText(getString(voted ? R.string.remove_vote : R.string.add_vote));
        }
    }

    @Override
    public void sendDataToParent(@NonNull Poll poll) {
        if (getTargetFragment() != null) {
            Intent intent = new Intent();
            intent.putExtra(Extra.POLL, poll);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        }
    }

    @Override
    public IPresenterFactory<PollPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = getArguments().getInt(Extra.ACCOUNT_ID);
            Poll poll = getArguments().getParcelable(Extra.POLL);
            AssertUtils.requireNonNull(poll);
            return new PollPresenter(aid, poll, saveInstanceState);
        };
    }

    @Override
    protected String tag() {
        return PollFragment.class.getSimpleName();
    }

    @Override
    public void onAnswerChanged(int newid) {
        getPresenter().fireVoteChecked(newid);
    }
}

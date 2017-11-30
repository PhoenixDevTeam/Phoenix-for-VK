package biz.dealnote.messenger.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.vkdatabase.ChairsAdapter;
import biz.dealnote.messenger.dialog.base.AccountDependencyDialogFragment;
import biz.dealnote.messenger.domain.IDatabaseInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.database.Chair;
import biz.dealnote.messenger.util.RxUtils;

public class SelectChairsDialog extends AccountDependencyDialogFragment implements ChairsAdapter.Listener {

    private static final int COUNT_PER_REQUEST = 1000;

    public static SelectChairsDialog newInstance(int aid, int facultyId, Bundle additional){
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.FACULTY_ID, facultyId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectChairsDialog selectCityDialog = new SelectChairsDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int mAccountId;
    private int facultyId;

    private ArrayList<Chair> mData;
    private RecyclerView mRecyclerView;
    private ChairsAdapter mAdapter;
    private IDatabaseInteractor mDatabaseInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
        this.facultyId = getArguments().getInt(Extra.FACULTY_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View root = inflater.inflate(R.layout.dialog_simple_recycler_view, container, false);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        boolean firstRun = false;
        if(mData == null){
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new ChairsAdapter(getActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if(firstRun){
            request(0);
        }
    }

    private void request(int offset){
        appendDisposable(mDatabaseInteractor.getChairs(mAccountId, facultyId, COUNT_PER_REQUEST, offset)
        .compose(RxUtils.applySingleIOToMainSchedulers())
        .subscribe(chairs -> onDataReceived(offset, chairs), throwable -> {}));
    }

    private void onDataReceived(int offset, List<Chair> chairs){
        if(offset == 0){
            mData.clear();
        }

        mData.addAll(chairs);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Chair chair) {
        Intent intent = new Intent();
        intent.putExtra(Extra.CHAIR, chair);
        intent.putExtra(Extra.ID, chair.getId());
        intent.putExtra(Extra.TITLE, chair.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
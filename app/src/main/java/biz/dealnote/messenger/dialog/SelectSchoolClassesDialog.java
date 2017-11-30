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
import biz.dealnote.messenger.adapter.vkdatabase.SchoolClassesAdapter;
import biz.dealnote.messenger.dialog.base.AccountDependencyDialogFragment;
import biz.dealnote.messenger.domain.IDatabaseInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.database.SchoolClazz;
import biz.dealnote.messenger.util.RxUtils;

public class SelectSchoolClassesDialog extends AccountDependencyDialogFragment implements SchoolClassesAdapter.Listener {

    public static SelectSchoolClassesDialog newInstance(int aid, int countryId, Bundle additional){
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectSchoolClassesDialog selectCityDialog = new SelectSchoolClassesDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    private int mAccountId;
    private int countryId;
    private IDatabaseInteractor mDatabaseInteractor;

    private ArrayList<SchoolClazz> mData;
    private RecyclerView mRecyclerView;
    private SchoolClassesAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        this.countryId = getArguments().getInt(Extra.COUNTRY_ID);
        this.mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
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

        mAdapter = new SchoolClassesAdapter(getActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if(firstRun){
            request();
        }
    }

    private void request(){
        appendDisposable(mDatabaseInteractor.getSchoolClasses(mAccountId, countryId)
        .compose(RxUtils.applySingleIOToMainSchedulers())
        .subscribe(this::onDataReceived, t -> {/*todo*/}));
    }

    private void onDataReceived(List<SchoolClazz> clazzes){
        mData.clear();
        mData.addAll(clazzes);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(SchoolClazz schoolClazz) {
        Intent intent = new Intent();
        intent.putExtra(Extra.SCHOOL_CLASS, schoolClazz);
        intent.putExtra(Extra.ID, schoolClazz.getId());
        intent.putExtra(Extra.TITLE, schoolClazz.getTitle());

        if (getArguments() != null) {
            intent.putExtras(getArguments());
        }

        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        dismiss();
    }
}
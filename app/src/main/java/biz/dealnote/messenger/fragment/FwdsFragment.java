package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.MessagesAdapter;
import biz.dealnote.messenger.fragment.base.AccountDependencyFragment;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Message;

public class FwdsFragment extends AccountDependencyFragment implements MessagesAdapter.OnMessageActionListener {

    public static Bundle buildArgs(int accountId, ArrayList<Message> messages){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.MESSAGES, messages);
        return args;
    }

    public static FwdsFragment newInstance(Bundle args){
        FwdsFragment fwdsFragment = new FwdsFragment();
        fwdsFragment.setArguments(args);
        return fwdsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fwds, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));

        ArrayList<Message> items = getArguments().getParcelableArrayList(Extra.MESSAGES);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        MessagesAdapter adapter = new MessagesAdapter(getActivity(), items, this);
        adapter.setOnMessageActionListener(this);
        recyclerView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() instanceof OnSectionResumeCallback){
            ((OnSectionResumeCallback)getActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(actionBar != null){
            actionBar.setSubtitle(null);
            actionBar.setTitle(R.string.title_mssages);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        super.onOpenOwner(userId);
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {

    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        return false;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {

    }
}

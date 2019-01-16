package biz.dealnote.messenger.fragment.attachments;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.WallEditorAttrs;
import biz.dealnote.messenger.mvp.presenter.PostEditPresenter;
import biz.dealnote.messenger.mvp.view.IPostEditView;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 21.01.2017.
 * phoenix
 */
public class PostEditFragment extends AbsPostEditFragment<PostEditPresenter, IPostEditView>
        implements IPostEditView {

    public static PostEditFragment newInstance(Bundle args){
        PostEditFragment fragment = new PostEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static Bundle buildArgs(int accountId, @NonNull Post post, @NonNull WallEditorAttrs attrs) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.POST, post);
        args.putParcelable(Extra.ATTRS, attrs);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    @Override
    public IPresenterFactory<PostEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            Post post = requireArguments().getParcelable(Extra.POST);
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            WallEditorAttrs attrs = requireArguments().getParcelable(Extra.ATTRS);

            AssertUtils.requireNonNull(post);
            AssertUtils.requireNonNull(attrs);
            return new PostEditPresenter(accountId, post, attrs, saveInstanceState);
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_attchments, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ready:
                getPresenter().fireReadyClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeAsSuccess() {
        requireActivity().onBackPressed();
    }

    @Override
    public void showConfirmExitDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.save_changes_question)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> getPresenter().fireExitWithSavingConfirmed())
                .setNegativeButton(R.string.button_no, (dialog, which) -> getPresenter().fireExitWithoutSavingClick())
                .setNeutralButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public boolean onBackPressed() {
        return getPresenter().onBackPressed();
    }
}
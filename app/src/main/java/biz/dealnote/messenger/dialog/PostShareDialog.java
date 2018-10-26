package biz.dealnote.messenger.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.MenuAdapter;
import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Text;
import biz.dealnote.messenger.model.menu.Item;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.ignore;

/**
 * Created by admin on 3/26/2018.
 * Phoenix-for-VK
 */
public class PostShareDialog extends DialogFragment {

    public static PostShareDialog newInstance(int accountId, @NonNull Post post) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.POST, post);
        PostShareDialog fragment = new PostShareDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public PostShareDialog targetTo(Fragment fragment, int requestCode) {
        setTargetFragment(fragment, requestCode);
        return this;
    }

    private int mAccountId;
    private Post mPost;

    public static final class Methods {
        public static final int SHARE_LINK = 1;
        public static final int SEND_MESSAGE = 2;
        public static final int REPOST_YOURSELF = 3;
        public static final int REPOST_GROUP = 4;
    }

    public static int extractMethod(@NonNull Intent data) {
        AssertUtils.requireNonNull(data.getExtras());
        return data.getExtras().getInt(EXTRA_METHOD);
    }

    public static Post extractPost(@NonNull Intent data) {
        return data.getParcelableExtra(Extra.POST);
    }

    public static int extractAccountId(@NonNull Intent data) {
        AssertUtils.requireNonNull(data.getExtras());
        return data.getExtras().getInt(Extra.ACCOUNT_ID);
    }

    public static int extractOwnerId(@NonNull Intent data) {
        AssertUtils.requireNonNull(data.getExtras());
        return data.getExtras().getInt(EXTRA_OWNER_ID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AssertUtils.requireNonNull(getArguments());
        mAccountId = getArguments().getInt(Extra.ACCOUNT_ID);
        mPost = getArguments().getParcelable(Extra.POST);
    }

    private MenuAdapter mAdapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private static final String EXTRA_METHOD = "share-method";
    private static final String EXTRA_OWNER_ID = "share-owner-id";

    private void onItemClick(Item item) {
        if (nonNull(getTargetFragment())) {
            Intent data = new Intent();

            int method = item.getKey();
            data.putExtra(Extra.ACCOUNT_ID, mAccountId);
            data.putExtra(EXTRA_METHOD, method);
            data.putExtra(Extra.POST, mPost);

            if (method == Methods.REPOST_GROUP) {
                data.putExtra(EXTRA_OWNER_ID, item.getExtra());
            }

            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
            dismissAllowingStateLoss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final IOwnersRepository interactor = Repository.INSTANCE.getOwners();

        final List<Item> items = new ArrayList<>();

        items.add(new Item(Methods.SHARE_LINK, new Text(R.string.share_link)).setIcon(R.drawable.web));
        items.add(new Item(Methods.SEND_MESSAGE, new Text(R.string.repost_send_message)).setIcon(R.drawable.share));

        boolean canRepostYourself = mPost.getOwnerId() != mAccountId && !mPost.isFriendsOnly() && mPost.getAuthorId() != mAccountId;

        if (canRepostYourself) {
            items.add(new Item(Methods.REPOST_YOURSELF, new Text(R.string.repost_to_wall)).setIcon(R.drawable.share_variant));
        }

        mAdapter = new MenuAdapter(requireActivity(), items);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.repost_title)
                .setAdapter(mAdapter, (dialog, which) -> onItemClick(items.get(which)))
                .setNegativeButton(R.string.button_cancel, null);


        boolean iAmOwnerAndAuthor = mPost.getOwnerId() == mAccountId && mPost.getAuthorId() == mAccountId;

        // Аккуратно, сложная логика!!!
        boolean canShareToGroups = mPost.isCanRepost() || (iAmOwnerAndAuthor && !mPost.isFriendsOnly());

        if (canShareToGroups) {
            compositeDisposable.add(interactor
                    .getCommunitiesWhereAdmin(mAccountId, true, true, false)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(owners -> {
                        for (Owner owner : owners) {
                            if (owner.getOwnerId() == mPost.getOwnerId()) {
                                continue;
                            }

                            items.add(new Item(Methods.REPOST_GROUP, new Text(owner.getFullName()))
                                    .setIcon(owner.get100photoOrSmaller())
                                    .setExtra(owner.getOwnerId()));
                        }

                        mAdapter.notifyDataSetChanged();
                    }, ignore()));
        }

        return builder.create();
    }
}
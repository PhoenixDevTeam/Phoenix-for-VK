package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.adapter.WallAdapter;
import biz.dealnote.messenger.fragment.base.PlaceSupportPresenterFragment;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.WallSearchCriteria;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.AbsWallPresenter;
import biz.dealnote.messenger.mvp.view.IWallView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.place.PlaceUtil;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.LoadMoreFooterHelper;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isLandscape;

/**
 * Created by ruslan.kolbasa on 23.01.2017.
 * phoenix
 */
public abstract class AbsWallFragment<V extends IWallView, P extends AbsWallPresenter<V>>
        extends PlaceSupportPresenterFragment<P, V> implements IWallView, WallAdapter.ClickListener, WallAdapter.NonPublishedPostActionListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WallAdapter mWallAdapter;
    private LoadMoreFooterHelper mLoadMoreFooterHelper;

    public static Bundle buildArgs(int accoutnId, int ownerId, @Nullable Owner owner) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accoutnId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putParcelable(Extra.OWNER, new ParcelableOwnerWrapper(owner));
        return args;
    }

    public static Fragment newInstance(Bundle args) {
        Fragment fragment;
        if (args.getInt(Extra.OWNER_ID) > 0) {
            fragment = new UserWallFragment();
        } else {
            fragment = new GroupWallFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    protected static void setupCounter(TextView view, int count) {
        view.setText((count > 0 ? (AppTextUtils.getCounterWithK(count)) : "-"));
        view.setEnabled(count > 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wall, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(getActivity(), mSwipeRefreshLayout);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(getActivity())) {
            boolean land = isLandscape(getActivity());
            manager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(getActivity());
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        View headerView = inflater.inflate(headerLayout(), recyclerView, false);
        onHeaderInflated(headerView);

        View footerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false);
        mLoadMoreFooterHelper = LoadMoreFooterHelper.createFrom(footerView, () -> getPresenter().fireLoadMoreClick());
        mLoadMoreFooterHelper.setEndOfListText("• • • • • • • •");

        FloatingActionButton fabCreate = root.findViewById(R.id.fragment_user_profile_fab);
        fabCreate.setOnClickListener(v -> getPresenter().fireCreateClick());

        mWallAdapter = new WallAdapter(getActivity(), Collections.emptyList(), this, this);
        mWallAdapter.addHeader(headerView);
        mWallAdapter.addFooter(footerView);
        mWallAdapter.setNonPublishedPostActionListener(this);

        recyclerView.setAdapter(mWallAdapter);
        return root;
    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    public void showSnackbar(int res, boolean isLong) {
        if(nonNull(getView())){
            Snackbar.make(getView(), res, isLong ? Snackbar.LENGTH_LONG : Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void openPhotoAlbum(int accountId, int ownerId, int albumId, @Nullable Integer focusPhotoId) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, focusPhotoId)
                .tryOpenWith(getActivity());
    }

    @Override
    public void goToWallSearch(int accountId, int ownerId) {
        WallSearchCriteria criteria = new WallSearchCriteria("", ownerId);
        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.WALL, criteria).tryOpenWith(getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                getPresenter().fireRefresh();
                return true;
            case R.id.action_copy_url:
                getPresenter().fireCopyUrlClick();
                return true;
            case R.id.action_search:
                getPresenter().fireSearchClick();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_wall, menu);
    }

    @Override
    public void copyToClipboard(String label, String body){
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, body);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getActivity(), R.string.copied, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void goToPostCreation(int accountId, int ownerId, @EditingPostType int postType) {
        PlaceUtil.goToPostCreation(getActivity(), accountId, ownerId, postType, null);
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @LayoutRes
    protected abstract int headerLayout();

    protected abstract void onHeaderInflated(View headerRootView);

    @Override
    public void displayWallData(List<Post> data) {
        if(nonNull(mWallAdapter)){
            mWallAdapter.setItems(data);
        }
    }

    @Override
    public void notifyWallDataSetChanged() {
        if(nonNull(mWallAdapter)){
            mWallAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyWallItemChanged(int position) {
        if(nonNull(mWallAdapter)){
            mWallAdapter.notifyItemChanged(position + mWallAdapter.getHeadersCount());
        }
    }

    @Override
    public void notifyWallDataAdded(int position, int count) {
        if(nonNull(mWallAdapter)){
            mWallAdapter.notifyItemRangeInserted(position + mWallAdapter.getHeadersCount(), count);
        }
    }

    @Override
    public void notifyWallItemRemoved(int index) {
        if(nonNull(mWallAdapter)){
            mWallAdapter.notifyItemRemoved(index + mWallAdapter.getHeadersCount());
        }
    }

    public static void repost(final Activity activity, final int accountId, final Post post) {
        String[] items = post.getOwnerId() == accountId ?
                new String[]{activity.getString(R.string.share_link), activity.getString(R.string.repost_send_message)} :
                new String[]{activity.getString(R.string.share_link), activity.getString(R.string.repost_send_message), activity.getString(R.string.repost_to_wall)};

        new AlertDialog.Builder(activity).setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    Utils.shareLink(activity, post.generateVkPostLink(), post.getText());
                    break;
                case 1:
                    SendAttachmentsActivity.startForSendAttachments(activity, accountId, post);
                    break;
                case 2:
                    PlaceFactory.getRepostPlace(accountId, null, post).tryOpenWith(activity);
                    break;
            }
        }).setCancelable(true).setTitle(R.string.repost_title).show();
    }

    @Override
    public void onOwnerClick(int ownerId) {
        super.onOpenOwner(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        getPresenter().fireShareClick(post);
    }

    @Override
    public void onPostClick(Post post) {
        getPresenter().firePostBodyClick(post);
    }

    @Override
    public void onRestoreClick(Post post) {
        getPresenter().firePostRestoreClick(post);
    }

    @Override
    public void onCommentsClick(Post post) {
        getPresenter().fireCommentsClick(post);
    }

    @Override
    public void onLikeLongClick(Post post) {
        getPresenter().fireLikeLongClick(post);
    }

    @Override
    public void onShareLongClick(Post post) {
        getPresenter().fireShareLongClick(post);
    }

    @Override
    public void onLikeClick(Post post) {
        getPresenter().fireLikeClick(post);
    }

    @Override
    public void openPostEditor(int accountId, Post post) {
        PlaceUtil.goToPostEditor(getActivity(), accountId, post);
        //PlaceFactory.getEditPostPlace(accountId, post, owner).tryOpenWith(getActivity());
    }

    @Override
    public void setupLoadMoreFooter(@LoadMoreState int state) {
        if(nonNull(mLoadMoreFooterHelper)){
            mLoadMoreFooterHelper.switchToState(state);
        }
    }

    @Override
    public void openPhotoAlbums(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getVKPhotoAlbumsPlace(accountId, ownerId, VKPhotosFragment.ACTION_SHOW_PHOTOS, ParcelableOwnerWrapper.wrap(owner))
                .tryOpenWith(getActivity());
    }

    @Override
    public void openVideosLibrary(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getVideosPlace(accountId, ownerId, VideosFragment.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(getActivity());
    }

    @Override
    public void openAudios(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getAudiosPlace(accountId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    @Override
    public void onButtonRemoveClick(Post post) {
        getPresenter().fireButtonRemoveClick(post);
    }
}

package biz.dealnote.messenger.fragment;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeStandalonePlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.adapter.MenuAdapter;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.link.internal.LinkActionAdapter;
import biz.dealnote.messenger.link.internal.OwnerLinkSpanFactory;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.EditingPostType;
import biz.dealnote.messenger.model.InternalVideoSize;
import biz.dealnote.messenger.model.Text;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.menu.Item;
import biz.dealnote.messenger.model.menu.Section;
import biz.dealnote.messenger.mvp.presenter.VideoPreviewPresenter;
import biz.dealnote.messenger.mvp.view.IVideoPreviewView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.place.PlaceUtil;
import biz.dealnote.messenger.settings.AppPrefs;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.YoutubeDeveloperKey;
import biz.dealnote.messenger.view.CircleCounterButton;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class VideoPreviewFragment extends BasePresenterFragment<VideoPreviewPresenter, IVideoPreviewView> implements View.OnClickListener, IVideoPreviewView {

    private static final String EXTRA_VIDEO_ID = "video_id";

    private View mRootView;
    private CircleCounterButton likeButton;
    private CircleCounterButton commentsButton;

    private TextView mTitleText;
    private TextView mSubtitleText;
    private ImageView mPreviewImage;

    public static Bundle buildArgs(int accountId, int ownerId, int videoId, @Nullable Video video) {
        Bundle bundle = new Bundle();

        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.OWNER_ID, ownerId);
        bundle.putInt(EXTRA_VIDEO_ID, videoId);

        if (nonNull(video)) {
            bundle.putParcelable(Extra.VIDEO, video);
        }

        return bundle;
    }

    public static VideoPreviewFragment newInstance(int accountId, int ownerId, int videoId, @Nullable Video video) {
        return newInstance(buildArgs(accountId, ownerId, videoId, video));
    }

    public static VideoPreviewFragment newInstance(Bundle args) {
        VideoPreviewFragment fragment = new VideoPreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_video_preview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(android.view.Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionView view = new OptionView();
        getPresenter().fireOptionViewCreated(view);

        menu.findItem(R.id.action_add_to_my_videos).setVisible(view.canAdd);
    }

    private final static class OptionView implements IVideoPreviewView.IOptionView {

        boolean canAdd;

        @Override
        public void setCanAdd(boolean can) {
            this.canAdd = can;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_to_my_videos) {
            getPresenter().fireAddToMyClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_video, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) mRootView.findViewById(R.id.toolbar));

        mPreviewImage = (ImageView) mRootView.findViewById(R.id.fragment_video_preview_image);

        likeButton = (CircleCounterButton) mRootView.findViewById(R.id.like_button);
        CircleCounterButton shareButton = (CircleCounterButton) mRootView.findViewById(R.id.share_button);
        commentsButton = (CircleCounterButton) mRootView.findViewById(R.id.comments_button);

        commentsButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);

        mTitleText = (TextView) mRootView.findViewById(R.id.fragment_video_title);
        mSubtitleText = (TextView) mRootView.findViewById(R.id.fragment_video_subtitle);

        mRootView.findViewById(R.id.button_play).setOnClickListener(v -> getPresenter().firePlayClick());
        mRootView.findViewById(R.id.try_again_button).setOnClickListener(v -> getPresenter().fireTryAgainClick());

        return mRootView;
    }

    private OwnerLinkSpanFactory.ActionListener ownerLinkAdapter = new LinkActionAdapter() {
        @Override
        public void onOwnerClick(int ownerId) {
            getPresenter().fireOwnerClick(ownerId);
        }
    };

    private void playWithExternalSoftware(String url) {
        if (isEmpty(url)) {
            Toast.makeText(getActivity(), R.string.error_video_playback_is_not_possible, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (nonNull(getActivity().getPackageManager().resolveActivity(intent, 0))) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.no_compatible_software_installed, Toast.LENGTH_SHORT).show();
        }
    }

    private static final Section SECTION_PLAY = new Section(new Text(R.string.section_play_title));
    private static final Section SECTION_OTHER = new Section(new Text(R.string.other));

    @Override
    public IPresenterFactory<VideoPreviewPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new VideoPreviewPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(EXTRA_VIDEO_ID),
                getArguments().getInt(Extra.OWNER_ID),
                getArguments().getParcelable(Extra.VIDEO),
                saveInstanceState
        );
    }

    @Override
    public void displayLoading() {
        if(nonNull(mRootView)){
            mRootView.findViewById(R.id.content).setVisibility(View.GONE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            mRootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.post_loading_text).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.try_again_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void displayLoadingError() {
        if(nonNull(mRootView)){
            mRootView.findViewById(R.id.content).setVisibility(View.GONE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            mRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            mRootView.findViewById(R.id.post_loading_text).setVisibility(View.GONE);
            mRootView.findViewById(R.id.try_again_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void displayVideoInfo(Video video) {
        if(nonNull(mRootView)){
            mRootView.findViewById(R.id.content).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.GONE);
        }

        safelySetText(mTitleText, video.getTitle());

        if(nonNull(mSubtitleText)){
            Spannable subtitle = OwnerLinkSpanFactory.withSpans(video.getDescription(), true, false, ownerLinkAdapter);

            mSubtitleText.setText(subtitle, TextView.BufferType.SPANNABLE);
            mSubtitleText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        String imageUrl = video.getMaxResolutionPhoto();

        if (nonEmpty(imageUrl) && nonNull(mPreviewImage)) {
            PicassoInstance.with()
                    .load(imageUrl)
                    .into(mPreviewImage);
        }
    }

    @Override
    public void displayLikes(int count, boolean userLikes) {
        if(nonNull(likeButton)){
            likeButton.setIcon(userLikes ? R.drawable.heart : R.drawable.heart_outline);
            likeButton.setCount(count);
            likeButton.setActive(userLikes);
        }
    }

    @Override
    public void setCommentButtonVisible(boolean visible) {
        if(nonNull(commentsButton)){
            commentsButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void displayCommentCount(int count) {
        if(nonNull(commentsButton)){
            commentsButton.setCount(count);
        }
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOwnerWall(int accountId, int ownerId) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(getActivity());
    }

    @Override
    public void showSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(nonNull(actionBar)){
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void showComments(int accountId, Commented commented) {
        PlaceFactory.getCommentsPlace(accountId, commented, null).tryOpenWith(getActivity());
    }

    @Override
    public void displayShareDialog(int accountId, Video video, boolean canPostToMyWall) {
        String[] items;
        if (canPostToMyWall) {
            items = new String[]{getString(R.string.repost_send_message), getString(R.string.repost_to_wall)};
        } else {
            items = new String[]{getString(R.string.repost_send_message)};
        }

        new AlertDialog.Builder(getActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            SendAttachmentsActivity.startForSendAttachments(getActivity(), accountId, video);
                            break;
                        case 1:
                            PlaceUtil.goToPostCreation(getActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(video));
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.repost_title)
                .show();
    }

    private static final class Menu {
        static final int P_240 = 240;
        static final int P_360 = 360;
        static final int P_480 = 480;
        static final int P_720 = 720;
        static final int P_1080 = 1080;
        static final int P_EXTERNAL_PLAYER = -1;

        static final int YOUTUBE_FULL = -2;
        static final int YOUTUBE_MIN = -3;
        static final int COUB = -4;

        static final int PLAY_ANOTHER_SOFT = -5;
        static final int PLAY_BROWSER = -6;
    }

    private List<Item> createDirectVkPlayItems(Video video, Section section) {
        List<Item> items = new ArrayList<>();
        if (nonEmpty(video.getMp4link240())) {
            items.add(new Item(Menu.P_240, new Text(R.string.play_240))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link360())) {
            items.add(new Item(Menu.P_360, new Text(R.string.play_360))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link480())) {
            items.add(new Item(Menu.P_480, new Text(R.string.play_480))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link720())) {
            items.add(new Item(Menu.P_720, new Text(R.string.play_720))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link1080())) {
            items.add(new Item(Menu.P_1080, new Text(R.string.play_1080))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }
        return items;
    }

    @Override
    public void showVideoPlayMenu(int accountId, Video video) {
        if (isNull(video)) {
            return;
        }

        List<Item> items = new ArrayList<>();
        items.addAll(createDirectVkPlayItems(video, SECTION_PLAY));

        String external = video.getExternalLink();

        if (nonEmpty(external)) {
            if (external.contains("youtube")) {
                items.add(new Item(Menu.YOUTUBE_FULL, new Text(R.string.title_play_fullscreen))
                        .setIcon(R.drawable.ic_play_youtube)
                        .setSection(SECTION_PLAY));

                items.add(new Item(Menu.YOUTUBE_MIN, new Text(R.string.title_play_in_dialog))
                        .setIcon(R.drawable.ic_play_youtube)
                        .setSection(SECTION_PLAY));

            } else if (external.contains("coub")) {
                if (AppPrefs.isCoubInstalled(getActivity())) {
                    items.add(new Item(Menu.COUB, new Text(R.string.title_play_in_coub))
                            .setIcon(R.drawable.ic_play_coub)
                            .setSection(SECTION_PLAY));
                }
            }

            items.add(new Item(Menu.PLAY_ANOTHER_SOFT, new Text(R.string.title_play_in_another_software))
                    .setSection(SECTION_OTHER)
                    .setIcon(R.drawable.ic_external));
        }

        if (nonEmpty(firstNonEmptyString(video.getMp4link240(),
                video.getMp4link360(),
                video.getMp4link480(),
                video.getMp4link720(),
                video.getMp4link1080()))) {

            // потом выбираем качество
            items.add(new Item(Menu.P_EXTERNAL_PLAYER, new Text(R.string.play_in_external_player))
                    .setIcon(R.drawable.ic_external)
                    .setSection(SECTION_OTHER));
        }

        items.add(new Item(Menu.PLAY_BROWSER, new Text(R.string.title_play_in_browser))
                .setIcon(R.drawable.ic_external)
                .setSection(SECTION_OTHER));

        MenuAdapter adapter = new MenuAdapter(getActivity(), items);

        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setAdapter(adapter, (dialog, which) -> onPlayMenuItemClick(video, items.get(which)))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void onPlayMenuItemClick(Video video, Item item) {
        switch (item.getKey()) {
            case Menu.P_240:
                openInternal(video, InternalVideoSize.SIZE_240);
                break;

            case Menu.P_360:
                openInternal(video, InternalVideoSize.SIZE_360);
                break;

            case Menu.P_480:
                openInternal(video, InternalVideoSize.SIZE_480);
                break;

            case Menu.P_720:
                openInternal(video, InternalVideoSize.SIZE_720);
                break;

            case Menu.P_1080:
                openInternal(video, InternalVideoSize.SIZE_1080);
                break;

            case Menu.P_EXTERNAL_PLAYER:
                showPlayExternalPlayerMenu(video);
                break;

            case Menu.YOUTUBE_FULL:
                playWithYoutube(video, false);
                break;

            case Menu.YOUTUBE_MIN:
                playWithYoutube(video, true);
                break;

            case Menu.COUB:
                playWithCoub(video);
                break;

            case Menu.PLAY_ANOTHER_SOFT:
                playWithExternalSoftware(video.getExternalLink());
                break;

            case Menu.PLAY_BROWSER:
                playWithExternalSoftware(video.getPlayer());
                break;
        }
    }

    private void showPlayExternalPlayerMenu(Video video) {
        Section section = new Section(new Text(R.string.title_select_resolution));
        List<Item> items = createDirectVkPlayItems(video, section);
        MenuAdapter adapter = new MenuAdapter(getActivity(), items);

        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setAdapter(adapter, (dialog, which) -> {
                    Item item = items.get(which);
                    switch (item.getKey()) {
                        case Menu.P_240:
                            playDirectVkLinkInExternalPlayer(video.getMp4link240());
                            break;

                        case Menu.P_360:
                            playDirectVkLinkInExternalPlayer(video.getMp4link360());
                            break;

                        case Menu.P_480:
                            playDirectVkLinkInExternalPlayer(video.getMp4link480());
                            break;

                        case Menu.P_720:
                            playDirectVkLinkInExternalPlayer(video.getMp4link720());
                            break;

                        case Menu.P_1080:
                            playDirectVkLinkInExternalPlayer(video.getMp4link1080());
                            break;
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void playDirectVkLinkInExternalPlayer(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/mp4");

        if (nonNull(getActivity().getPackageManager().resolveActivity(intent, 0))) {
            startActivity(intent);
        } else {
            Utils.showRedTopToast(getActivity(), R.string.no_compatible_software_installed);
        }
    }

    private void openInternal(Video video, int size) {
        PlaceFactory.getVkInternalPlayerPlace(video, size).tryOpenWith(getActivity());
    }

    private void playWithCoub(Video video) {
        String outerLink = video.getExternalLink();

        Intent intent = new Intent();
        intent.setData(Uri.parse(outerLink));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.coub.android", "com.coub.android.ui.ViewCoubActivity"));
        startActivity(intent);
    }

    private void playWithYoutube(Video video, boolean inFloatWindow) {
        int index = video.getExternalLink().indexOf("watch?v=");
        final String videoId = video.getExternalLink().substring(index + 8, video.getExternalLink().length());

        try {
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), YoutubeDeveloperKey.DEVELOPER_KEY, videoId, 0, true, inFloatWindow);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Utils.showRedTopToast(getActivity(), R.string.no_compatible_software_installed);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.video);
        }

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
    protected String tag() {
        return VideoPreviewFragment.class.getSimpleName();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.like_button:
                getPresenter().fireLikeClick();
                break;

            case R.id.comments_button:
                getPresenter().fireCommentsClick();
                break;

            case R.id.share_button:
                getPresenter().fireShareClick();
                break;
        }
    }
}
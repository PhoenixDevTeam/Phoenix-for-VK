package biz.dealnote.messenger.activity;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.media.video.DefaultVideoPlayer;
import biz.dealnote.messenger.media.video.ExoVideoPlayer;
import biz.dealnote.messenger.media.video.IVideoPlayer;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.view.VideoControllerView;

public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        VideoControllerView.MediaPlayerControl, IVideoPlayer.IVideoSizeChangeListener {

    public static final String EXTRA_VIDEO = "video";
    public static final String EXTRA_SIZE = "size";

    private View mDecorView;
    private SurfaceView mSurfaceView;
    private VideoControllerView mControllerView;

    private IVideoPlayer mPlayer;

    private Video video;
    private int size;

    private boolean isLandscape = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get()
                .ui()
                .getMainTheme());

        setContentView(R.layout.activity_video);

        if (Utils.hasLollipop()) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        video = getIntent().getParcelableExtra(EXTRA_VIDEO);
        size = getIntent().getIntExtra(EXTRA_SIZE, Video.SIZE_240);

        mDecorView = getWindow().getDecorView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(video.getTitle());
            actionBar.setSubtitle(video.getDescription());
        }

        mControllerView = new VideoControllerView(this);

        mSurfaceView = findViewById(R.id.videoSurface);
        mSurfaceView.setOnClickListener(v -> resolveControlsVisibility());

        SurfaceHolder videoHolder = mSurfaceView.getHolder();
        videoHolder.addCallback(this);

        resolveControlsVisibility();

        try {
            mPlayer = createPlayer();
            mPlayer.addVideoSizeChangeListener(this);
            mPlayer.play();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mControllerView.setMediaPlayer(this);
        mControllerView.setAnchorView(findViewById(R.id.videoSurfaceContainer));
    }

    private IVideoPlayer createPlayer() throws IOException {
        IProxySettings settings = Injection.provideProxySettings();
        ProxyConfig config = settings.getActiveProxy();

        final String url = getFileUrl();

        return Objects.nonNull(config) ? new ExoVideoPlayer(this, url, config) : new DefaultVideoPlayer(url);
    }

    private void resolveControlsVisibility() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        if (actionBar.isShowing()) {
            //toolbar_with_elevation.animate().translationY(-toolbar_with_elevation.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            actionBar.hide();
            mControllerView.hide();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
            } else {
                mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        } else {
            //toolbar_with_elevation.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
            actionBar.show();
            mControllerView.show();
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    protected void onDestroy() {
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mPlayer.pause();
        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.setSurfaceHolder(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return mPlayer.getBufferPercentage();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void seekTo(int i) {
        mPlayer.seekTo(i);
    }

    @Override
    public void start() {
        mPlayer.play();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {
        setRequestedOrientation(isLandscape ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private String getFileUrl() {
        String url;

        switch (size) {
            case Video.SIZE_240:
                url = video.getMp4link240();
                break;
            case Video.SIZE_360:
                url = video.getMp4link360();
                break;
            case Video.SIZE_480:
                url = video.getMp4link480();
                break;
            case Video.SIZE_720:
                url = video.getMp4link720();
                break;
            case Video.SIZE_1080:
                url = video.getMp4link1080();
                break;
            default:
                throw new IllegalArgumentException("Unknown video size");
        }

        return url;

    }

    private void setFitToFillAspectRatio(int videoWidth, int videoHeight) {
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealSize(size);
        } else {
            getWindowManager().getDefaultDisplay().getSize(size);
        }

        int screenWidth = size.x;
        int screenHeight = size.y;
        ViewGroup.LayoutParams videoParams = mSurfaceView.getLayoutParams();

        if (videoWidth > videoHeight) {
            videoParams.width = screenWidth;
            videoParams.height = screenWidth * videoHeight / videoWidth;
        } else {
            videoParams.width = screenHeight * videoWidth / videoHeight;
            videoParams.height = screenHeight;
        }

        mSurfaceView.setLayoutParams(videoParams);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setFitToFillAspectRatio(mSurfaceView.getWidth(), mSurfaceView.getHeight());

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;
        }
    }

    @Override
    public void onVideoSizeChanged(@NonNull IVideoPlayer player, VideoSize size) {
        setFitToFillAspectRatio(size.getWidth(), size.getHeight());
    }
}
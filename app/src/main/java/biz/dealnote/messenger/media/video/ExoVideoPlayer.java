package biz.dealnote.messenger.media.video;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Util;

import java.lang.ref.WeakReference;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.media.exo.CustomHttpDataSourceFactory;
import biz.dealnote.messenger.media.exo.ExoUtil;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by Ruslan Kolbasa on 14.08.2017.
 * phoenix
 */
public class ExoVideoPlayer implements IVideoPlayer {

    private final SimpleExoPlayer player;

    private final MediaSource source;

    private final OnVideoSizeChangedListener onVideoSizeChangedListener = new OnVideoSizeChangedListener(this);

    public ExoVideoPlayer(Context context, String url, ProxyConfig config) {
        this.player = createPlayer(context);
        this.player.addVideoListener(onVideoSizeChangedListener);
        this.source = createMediaSource(context, url, config);
    }

    private static MediaSource createMediaSource(Context context, String url, ProxyConfig proxyConfig) {
        Proxy proxy = null;
        if (Objects.nonNull(proxyConfig)) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));

            if (proxyConfig.isAuthEnabled()) {
                Authenticator authenticator = new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(proxyConfig.getUser(), proxyConfig.getPass().toCharArray());
                    }
                };

                Authenticator.setDefault(authenticator);
            } else {
                Authenticator.setDefault(null);
            }
        }

        String userAgent = Util.getUserAgent(context.getApplicationContext(), "phoenix-video-exo-player");
        CustomHttpDataSourceFactory factory = new CustomHttpDataSourceFactory(userAgent, proxy);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:
        return new ExtractorMediaSource(Uri.parse(url), factory, extractorsFactory, null, null);
    }

    private static SimpleExoPlayer createPlayer(Context context) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        return ExoPlayerFactory.newSimpleInstance(context.getApplicationContext(), trackSelector);
    }

    private boolean supposedToBePlaying;

    private boolean prepareCalled;

    @Override
    public void play() {
        if (supposedToBePlaying) {
            return;
        }

        supposedToBePlaying = true;

        if (!prepareCalled) {
            player.prepare(source);
            prepareCalled = true;
        }

        ExoUtil.startPlayer(player);
    }

    @Override
    public void pause() {
        if(!supposedToBePlaying){
            return;
        }

        supposedToBePlaying = false;
        ExoUtil.pausePlayer(player);
    }

    @Override
    public void release() {
        player.removeVideoListener(onVideoSizeChangedListener);
        player.release();
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    @Override
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public boolean isPlaying() {
        return supposedToBePlaying;
    }

    @Override
    public int getBufferPercentage() {
        return player.getBufferedPercentage();
    }

    @Override
    public void setSurfaceHolder(SurfaceHolder holder) {
        player.setVideoSurfaceHolder(holder);
    }

    private static final class OnVideoSizeChangedListener implements SimpleExoPlayer.VideoListener {

        final WeakReference<ExoVideoPlayer> ref;

        private OnVideoSizeChangedListener(ExoVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void onVideoSizeChanged(int i, int i1, int i2, float v) {
            ExoVideoPlayer player = ref.get();
            if (player != null) {
                player.onVideoSizeChanged(i, i1);
            }
        }

        @Override
        public void onRenderedFirstFrame() {

        }
    }

    private void onVideoSizeChanged(int w, int h) {
        for(IVideoSizeChangeListener listener : videoSizeChangeListeners){
            listener.onVideoSizeChanged(this, new VideoSize(w, h));
        }
    }

    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.remove(listener);
    }
}
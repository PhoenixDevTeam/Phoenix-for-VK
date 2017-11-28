package biz.dealnote.messenger.media.gif;

import android.net.Uri;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
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

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.media.exo.CustomHttpDataSourceFactory;
import biz.dealnote.messenger.media.exo.ExoEventAdapter;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by admin on 13.08.2017.
 * phoenix
 */
public class ExoGifPlayer implements IGifPlayer {

    private int status;

    private final String url;

    private VideoSize size;

    private SimpleExoPlayer internalPlayer;

    private final ProxyConfig proxyConfig;

    public ExoGifPlayer(String url, ProxyConfig proxyConfig) {
        this.url = url;
        this.proxyConfig = proxyConfig;
        this.status = IStatus.INIT;
    }

    @Override
    public VideoSize getVideoSize() {
        return size;
    }

    @Override
    public void play() throws PlayerPrepareException {
        if(supposedToBePlaying) return;

        supposedToBePlaying = true;

        switch (status) {
            case IStatus.PREPARED:
                AssertUtils.requireNonNull(this.internalPlayer);
                startPlayer(this.internalPlayer);
                break;
            case IStatus.INIT:
                preparePlayer();
                break;
            case IStatus.PREPARING:
                //do nothing
                break;
        }
    }

    private void preparePlayer() throws PlayerPrepareException {
        this.setStatus(IStatus.PREPARING);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        internalPlayer = ExoPlayerFactory.newSimpleInstance(App.getInstance(), trackSelector);

        // DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        // DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(App.getInstance(), Util.getUserAgent(App.getInstance(), "exoplayer2example"), bandwidthMeterA);

        Proxy proxy = null;
        if(Objects.nonNull(proxyConfig)){
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyConfig.getAddress(), proxyConfig.getPort()));

            if(proxyConfig.isAuthEnabled()){
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

        String userAgent = Util.getUserAgent(App.getInstance(), "phoenix-gif-exo-player");
        CustomHttpDataSourceFactory factory = new CustomHttpDataSourceFactory(userAgent, proxy);

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:
        MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(url), factory, extractorsFactory, null, null);
        internalPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
        internalPlayer.addListener(new ExoEventAdapter() {
            @Override
            public void onPlayerStateChanged(boolean b, int i) {
                Logger.d("PhoenixExo", "onPlayerStateChanged, b: " + b + ", i: " + i);
                onInternalPlayerStateChanged(i);
            }
        });

        internalPlayer.addVideoListener(videoListener);
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.prepare(mediaSource);
    }

    private void onInternalPlayerStateChanged(int state){
        if(state == Player.STATE_READY){
            setStatus(IStatus.PREPARED);
        }
    }

    private final SimpleExoPlayer.VideoListener videoListener = new SimpleExoPlayer.VideoListener() {
        @Override
        public void onVideoSizeChanged(int i, int i1, int i2, float v) {
            size = new VideoSize(i, i1);
            ExoGifPlayer.this.onVideoSizeChanged();
        }

        @Override
        public void onRenderedFirstFrame() {

        }
    };

    private void onVideoSizeChanged(){
        for(IVideoSizeChangeListener listener : videoSizeChangeListeners){
            listener.onVideoSizeChanged(this, this.size);
        }
    }

    private boolean supposedToBePlaying;

    private static void pausePlayer(SimpleExoPlayer internalPlayer){
        internalPlayer.setPlayWhenReady(false);
        internalPlayer.getPlaybackState();
    }
    private static void startPlayer(SimpleExoPlayer internalPlayer){
        internalPlayer.setPlayWhenReady(true);
        internalPlayer.getPlaybackState();
    }

    @Override
    public void pause() {
        if(!supposedToBePlaying) return;

        supposedToBePlaying = false;

        if(Objects.nonNull(internalPlayer)){
            try {
                pausePlayer(this.internalPlayer);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if(Objects.nonNull(internalPlayer)){
            internalPlayer.setVideoSurfaceHolder(holder);
        }
    }

    @Override
    public void release() {
        if(Objects.nonNull(internalPlayer)){
            try {
                internalPlayer.release();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setStatus(int newStatus){
        final int oldStatus = this.status;

        if(this.status == newStatus){
            return;
        }

        this.status = newStatus;
        for(IStatusChangeListener listener : statusChangeListeners){
            listener.onPlayerStatusChange(this, oldStatus, newStatus);
        }
    }

    private final List<IVideoSizeChangeListener> videoSizeChangeListeners = new ArrayList<>(1);
    private final List<IStatusChangeListener> statusChangeListeners = new ArrayList<>(1);

    @Override
    public void addVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.add(listener);
    }

    @Override
    public void addStatusChangeListener(IStatusChangeListener listener) {
        this.statusChangeListeners.add(listener);
    }

    @Override
    public void removeVideoSizeChangeListener(IVideoSizeChangeListener listener) {
        this.videoSizeChangeListeners.remove(listener);
    }

    @Override
    public void removeStatusChangeListener(IStatusChangeListener listener) {
        this.statusChangeListeners.remove(listener);
    }

    @Override
    public int getPlayerStatus() {
        return status;
    }
}
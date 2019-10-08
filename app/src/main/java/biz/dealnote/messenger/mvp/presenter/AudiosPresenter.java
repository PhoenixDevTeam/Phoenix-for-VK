package biz.dealnote.messenger.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioFilter;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IAudiosView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by admin on 1/4/2018.
 * Phoenix-for-VK
 */
public class AudiosPresenter extends AccountDependencyPresenter<IAudiosView> {

    private final IAudioInteractor audioInteractor;
    private final ArrayList<Audio> audios;
    private final int ownerId;
    private final boolean audioAvailable;
    private boolean actualReceived;
    private List<AudioFilter> filters;
    private AudioFilter currentFilter;

    public AudiosPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.audioInteractor = InteractorFactory.createAudioInteractor();
        this.audioAvailable = audioInteractor.isAudioPluginAvailable();
        this.audios = new ArrayList<>();
        this.ownerId = ownerId;
        this.filters = createFilterList();

        if (audioAvailable) {
            requestList();
        }
    }

    private CompositeDisposable audioListDisposable = new CompositeDisposable();

    private boolean loadingNow;

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    private boolean endOfContent;

    private void requestNext() {
        setLoadingNow(true);
        final int offset = audios.size();
        audioListDisposable.add(audioInteractor.get(ownerId, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNextListReceived, this::onListGetError));
    }

    private static List<AudioFilter> createFilterList() {
        int engOnly = 0;
        List<AudioFilter> result = new ArrayList<>();
        result.add(new AudioFilter(engOnly, AudioFilter.MY_AUDIO, true));
        result.add(new AudioFilter(engOnly, AudioFilter.TOP_ALL));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.ACOUSTIC_AND_VOCAL));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.ALTERNATIVE));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.CLASSICAL));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.DANCE_AND_HOUSE));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.DRUM_AND_BASS));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.DUBSTEP));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.EASY_LISTENING));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.ELECTROPOP_AND_DISCO));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.ETHNIC));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.INDIE_POP));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.INSTRUMENTAL));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.JAZZ_AND_BLUES));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.METAL));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.OTHER));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.POP));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.RAP_AND_HIPHOP));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.REGGAE));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.ROCK));
        result.add(new AudioFilter(engOnly, VKApiAudio.Genre.TRANCE));
        return result;
    }

    public void requestList() {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.get(ownerId, 0)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onListReceived, this::onListGetError));
    }

    private void onNextListReceived(List<Audio> next) {
        next.removeAll(audios);
        audios.addAll(next);
        endOfContent = next.isEmpty();
        setLoadingNow(false);
        callView(IAudiosView::notifyListChanged);
    }

    private void onListReceived(List<Audio> data) {
        audios.clear();
        audios.addAll(data);
        endOfContent = data.isEmpty();
        actualReceived = true;
        setLoadingNow(false);
        callView(IAudiosView::notifyListChanged);
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, audios, position, false);
        PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public void getListByGenre(boolean foreign, int genre) {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.getPopular(foreign ? 1 : 0, genre)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onListReceived, this::onListGetError));
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        showError(getView(), Utils.getCauseIfRuntime(t));
    }

    public void fireRefresh() {
        if (audioAvailable) {
            audioListDisposable.clear();
            if (currentFilter == null || currentFilter.isFilterNone()) {
                requestList();
            } else {
                getListByGenre(currentFilter.isEnglishOnly() == 1, currentFilter.getGenre());
            }
        }
    }

    public void fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestNext();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosView view) {
        super.onGuiCreated(view);
        view.showFilters(getAccountId() == ownerId);
        view.fillFilters(filters);
        view.displayList(audios);
        view.setBlockedScreen(!audioAvailable);
    }

    public void fireFilterItemClick(AudioFilter source) {
        currentFilter = source;
        for (AudioFilter filter : filters) {
            filter.setActive(filter.getGenre() == source.getGenre());
        }
        callView(IAudiosView::notifyFilterListChanged);
        fireRefresh();
    }
}
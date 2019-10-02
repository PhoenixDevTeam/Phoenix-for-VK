package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.AudioRecyclerAdapter;
import biz.dealnote.messenger.fragment.base.BaseFragment;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.settings.Settings;

/**
 * Created by golde on 27.09.2016.
 */
public class PlaylistFragment extends BaseFragment implements AudioRecyclerAdapter.ClickListener,
        BackPressCallback {

    private RecyclerView mRecyclerView;
    private View root;
    private AudioRecyclerAdapter mAdapter;
    private ArrayList<Audio> mData;

    public static Bundle buildArgs(ArrayList<Audio> playlist) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Extra.AUDIOS, playlist);
        return bundle;
    }

    public static PlaylistFragment newInstance(ArrayList<Audio> playlist) {
        return newInstance(buildArgs(playlist));
    }

    public static PlaylistFragment newInstance(Bundle args) {
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromSavedInstanceState(savedInstanceState);
        }
        mData = requireArguments().getParcelableArrayList(Extra.AUDIOS);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_playlist, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mRecyclerView = root.findViewById(R.id.list);
        LinearLayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new AudioRecyclerAdapter(requireActivity(), mData);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void restoreFromSavedInstanceState(@NonNull Bundle state) {
        //this.mData = state.getParcelableArrayList(Extra.AUDIOS);
    }

    @Override
    public void onClick(int position, Audio audio) {
        MusicPlaybackService.startForPlayList(requireActivity(), mData, position, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIO_CURRENT_PLAYLIST);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.playlist);
            actionBar.setSubtitle(null);
        }
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }
}

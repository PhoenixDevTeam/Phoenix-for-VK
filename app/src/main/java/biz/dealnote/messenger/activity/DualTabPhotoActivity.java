package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.DualTabPhotosFragment;
import biz.dealnote.messenger.fragment.LocalPhotosFragment;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.selection.Sources;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;

import static biz.dealnote.messenger.util.Objects.isNull;

/**
 * Created by admin on 15.04.2017.
 * phoenix
 */
public class DualTabPhotoActivity extends NoMainActivity implements PlaceProvider {

    private int mMaxSelectionCount;
    private Sources mSources;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isNull(savedInstanceState)){
            this.mMaxSelectionCount = getIntent().getIntExtra(Extra.MAX_COUNT, 10);
            this.mSources = getIntent().getParcelableExtra(Extra.SOURCES);

            attachStartFragment();
        } else {
            this.mMaxSelectionCount = savedInstanceState.getInt("mMaxSelectionCount");
            this.mSources = savedInstanceState.getParcelable("mSources");
        }
    }

    public static Intent createIntent(Context context, int maxSelectionCount, @NonNull Sources sources){
        return new Intent(context, DualTabPhotoActivity.class)
                .putExtra(Extra.MAX_COUNT, maxSelectionCount)
                .putExtra(Extra.SOURCES, sources);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mMaxSelectionCount", mMaxSelectionCount);
        outState.putParcelable("mSources", mSources);
    }

    private void attachStartFragment(){
        DualTabPhotosFragment fragment = DualTabPhotosFragment.newInstance(mSources);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("dual-tab-photos")
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        switch (place.type){
            case Place.VK_PHOTO_ALBUM:
                int albumId = place.getArgs().getInt(Extra.ALBUM_ID);
                int accountId = place.getArgs().getInt(Extra.ACCOUNT_ID);
                int ownerId = place.getArgs().getInt(Extra.OWNER_ID);

                VKPhotosFragment fragment = VKPhotosFragment.newInstance(accountId, ownerId, albumId, VKPhotosFragment.ACTION_SELECT_PHOTOS);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .addToBackStack("vk-album-photos")
                        .commit();
                break;

            case Place.LOCAL_IMAGE_ALBUM:
                LocalImageAlbum album = place.getArgs().getParcelable(Extra.ALBUM);

                LocalPhotosFragment localPhotosFragment = LocalPhotosFragment.newInstance(mMaxSelectionCount, album);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, localPhotosFragment)
                        .addToBackStack("local-album-photos")
                        .commit();
                break;
        }
    }
}
package biz.dealnote.messenger.activity;

import android.content.Intent;
import android.os.Bundle;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.VKPhotoAlbumsFragment;
import biz.dealnote.messenger.fragment.VKPhotosFragment;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;

public class PhotoAlbumsActivity extends NoMainActivity implements PlaceProvider {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Intent intent = getIntent();

            final int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            final int ownerId = intent.getExtras().getInt(Extra.OWNER_ID);
            final String action = intent.getStringExtra(Extra.ACTION);

            VKPhotoAlbumsFragment fragment = VKPhotoAlbumsFragment.newInstance(accountId, ownerId, action, null);

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        switch (place.type){
            case Place.VK_PHOTO_ALBUM:
                VKPhotosFragment fragment = VKPhotosFragment.newInstance(place.getArgs());
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .addToBackStack("photos")
                        .commit();
                break;
        }
    }
}
package biz.dealnote.messenger.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.fragment.AddProxyFragment;
import biz.dealnote.messenger.fragment.ProxyManagerFrgament;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceProvider;
import biz.dealnote.messenger.util.Objects;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxyManagerActivity extends NoMainActivity implements PlaceProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Objects.isNull(savedInstanceState)){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), ProxyManagerFrgament.newInstance())
                    .addToBackStack("proxy-manager")
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        if(place.type == Place.PROXY_ADD){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), AddProxyFragment.newInstance())
                    .addToBackStack("proxy-add")
                    .commit();
        }
    }
}
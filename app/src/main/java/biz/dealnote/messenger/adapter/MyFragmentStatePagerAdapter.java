package biz.dealnote.messenger.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

import biz.dealnote.messenger.util.Objects;

public abstract class MyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    private SparseArray<WeakReference<Fragment>> fragments;

    public MyFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>(3);
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment findFragmentByPosition(int position){
        WeakReference<Fragment> weak = fragments.get(position);
        return Objects.isNull(weak) ? null : weak.get();
    }
}

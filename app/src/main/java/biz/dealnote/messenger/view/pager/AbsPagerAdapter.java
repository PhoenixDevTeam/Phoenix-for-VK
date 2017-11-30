package biz.dealnote.messenger.view.pager;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
@SuppressWarnings("NullableProblems")
public abstract class AbsPagerAdapter<H extends AbsPagerHolder> extends PagerAdapter {

    private SparseArray<H> mHolderSparseArray = new SparseArray<>();

    protected abstract H createHolder(int adapterPosition, ViewGroup container);

    protected abstract void bindHolder(@NonNull H holder, int position);

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final H holder = createHolder(position, container);

        bindHolder(holder, position);

        mHolderSparseArray.put(position, holder);
        container.addView(holder.mItemView);
        return holder.mItemView;
    }

    public void rebindHolderAt(int position){
        H holder = mHolderSparseArray.get(position);
        if(holder != null && !holder.isDestroyed()){
            bindHolder(holder, position);
        }
    }

    public void rebindHolders() {
        for (int i = 0; i < mHolderSparseArray.size(); i++) {
            int key = mHolderSparseArray.keyAt(i);
            H holder = mHolderSparseArray.get(key);

            if (holder != null && !holder.isDestroyed()) {
                bindHolder(holder, key);
            }
        }
    }

    public void release(){
        for (int i = 0; i < mHolderSparseArray.size(); i++) {
            int key = mHolderSparseArray.keyAt(i);
            H holder = mHolderSparseArray.get(key);

            if (holder != null && !holder.isDestroyed()) {
                holder.destroy();
            }
        }

        mHolderSparseArray.clear();
    }

    @Override
    public boolean isViewFromObject(View view, Object key) {
        return key == view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        H holder = mHolderSparseArray.get(position);
        if(holder != null){
            holder.onDestroy();
        }

        mHolderSparseArray.remove(position);
        container.removeView((View) view);
    }
}

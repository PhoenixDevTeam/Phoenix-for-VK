package biz.dealnote.messenger.view.pager;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by ruslan.kolbasa on 19.10.2016.
 * phoenix
 */
public class AbsPagerHolder {

    private boolean mDestroyed;
    private int mAdapterPosition;
    public View mItemView;

    public AbsPagerHolder(int adapterPosition, @NonNull View itemView) {
        this.mItemView = itemView;
        this.mAdapterPosition = adapterPosition;
    }

    public final void destroy(){
        onDestroy();
        mDestroyed = true;
    }

    @CallSuper
    protected void onDestroy(){

    }

    public boolean isDestroyed() {
        return mDestroyed;
    }

    public int getAdapterPosition() {
        return mAdapterPosition;
    }
}

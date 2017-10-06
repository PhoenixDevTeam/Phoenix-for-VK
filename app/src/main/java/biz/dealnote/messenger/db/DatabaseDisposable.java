package biz.dealnote.messenger.db;

import android.support.annotation.NonNull;

import biz.dealnote.messenger.model.Criteria;
import io.reactivex.disposables.Disposable;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
public class DatabaseDisposable<C extends Criteria> implements Disposable {

    private final Disposable base;
    private final C criteria;

    public DatabaseDisposable(@NonNull Disposable base, @NonNull C criteria) {
        this.base = base;
        this.criteria = criteria;
    }

    @Override
    public void dispose() {
        base.dispose();
    }

    @Override
    public boolean isDisposed() {
        return base.isDisposed();
    }

    public C getCriteria() {
        return criteria;
    }

    public boolean compareTo(@NonNull C criteria){
        return criteria.equals(this.criteria);
    }
}

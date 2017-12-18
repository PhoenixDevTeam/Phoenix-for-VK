package biz.dealnote.messenger.fragment.base;

import android.support.v4.app.Fragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseFragment extends Fragment {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected void appendDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}
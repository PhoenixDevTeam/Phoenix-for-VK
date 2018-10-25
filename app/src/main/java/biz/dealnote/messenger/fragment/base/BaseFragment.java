package biz.dealnote.messenger.fragment.base;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import biz.dealnote.messenger.util.AssertUtils;
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

    public Bundle requireArguments(){
        return AssertUtils.requireNonNull(getArguments());
    }
}
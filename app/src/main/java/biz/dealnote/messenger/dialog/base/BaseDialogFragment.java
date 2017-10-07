package biz.dealnote.messenger.dialog.base;

import android.support.v4.app.DialogFragment;

import biz.dealnote.messenger.util.ViewUtils;

public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewUtils.keyboardHide(getActivity());
    }
}
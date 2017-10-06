package biz.dealnote.messenger.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.foxykeep.datadroid.requestmanager.Request;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import biz.dealnote.messenger.dialog.base.AccountDependencyDialogFragment;
import biz.dealnote.messenger.exception.ServiceException;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.service.factory.UtilsRequestFactory;

public class ResolveDomainDialog extends AccountDependencyDialogFragment {

    public static Bundle buildArgs(int aid, String url, String domain){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putString(Extra.URL, url);
        args.putString(Extra.DOMAIN, domain);
        return args;
    }

    public static ResolveDomainDialog newInstance(int aid, String url, String domain){
        return newInstance(buildArgs(aid, url, domain));
    }

    public static ResolveDomainDialog newInstance(Bundle args){
        ResolveDomainDialog domainDialog = new ResolveDomainDialog();
        domainDialog.setArguments(args);
        return domainDialog;
    }

    private String url;
    private String domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url = getArguments().getString(Extra.URL);
        this.domain = getArguments().getString(Extra.DOMAIN);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(R.string.loading);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(this);

        request();
        return progressDialog;
    }

    private void request(){
        Request request = UtilsRequestFactory.getResolveScreenNameRequest(domain);
        executeRequest(request);
    }

    private void openDomain(ResolveDomailResponse resolveDomailResult) {
        if(!isAdded()) return;

        dismiss();

        if (resolveDomailResult == null) {
            PlaceFactory.getExternalLinkPlace(getAccountId(), url).tryOpenWith(getActivity());
            return;
        }

        if (ResolveDomailResponse.TYPE_GROUP.equals(resolveDomailResult.type)) {
            int gid = -Math.abs(Integer.parseInt(resolveDomailResult.object_id));
            PlaceFactory.getOwnerWallPlace(getAccountId(), gid, null).tryOpenWith(getActivity());
        }

        if (ResolveDomailResponse.TYPE_USER.equals(resolveDomailResult.type)) {
            int uid = Integer.parseInt(resolveDomailResult.object_id);
            PlaceFactory.getOwnerWallPlace(getAccountId(), uid, null).tryOpenWith(getActivity());
        }
    }

    @Override
    protected void onRequestFinished(@NonNull Request request, Bundle resultData) {
        super.onRequestFinished(request, resultData);

        if (request.getRequestType() == UtilsRequestFactory.REQUEST_SCREEN_NAME) {
            ResolveDomailResponse result = resultData.getParcelable(Extra.RESPONSE);
            openDomain(result);
        }
    }

    @Override
    protected void onRequestError(@NonNull Request request, ServiceException throwable) {
        super.onRequestError(request, throwable);
        if(isAdded()){
            showErrorAlert(throwable.getMessage());
        }
    }

    private void showErrorAlert(String error){
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error)
                .setMessage(error).setPositiveButton(R.string.try_again, (dialog, which) -> request())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss())
                .show();
    }
}

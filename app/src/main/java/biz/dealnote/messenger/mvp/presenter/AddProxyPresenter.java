package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.IAddProxyView;
import biz.dealnote.messenger.settings.IProxySettings;

import static biz.dealnote.messenger.util.Utils.trimmedIsEmpty;

/**
 * Created by Ruslan Kolbasa on 11.07.2017.
 * phoenix
 */
public class AddProxyPresenter extends RxSupportPresenter<IAddProxyView> {

    private boolean authEnabled;
    private String address;
    private String port;
    private String userName;
    private String pass;

    private final IProxySettings settings;

    public AddProxyPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        settings = Injection.provideProxySettings();
    }

    @Override
    public void onGuiCreated(@NonNull IAddProxyView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.setAuthFieldsEnabled(authEnabled);
        viewHost.setAuthChecked(authEnabled);
    }

    @Override
    protected String tag() {
        return AddProxyPresenter.class.getSimpleName();
    }

    public void fireAddressEdit(CharSequence s) {
        address = s.toString();
    }

    public void firePortEdit(CharSequence s) {
        port = s.toString();
    }

    public void fireAuthChecked(boolean isChecked) {
        if (authEnabled == isChecked) {
            return;
        }

        this.authEnabled = isChecked;
        getView().setAuthFieldsEnabled(isChecked);
    }

    public void fireUsernameEdit(CharSequence s) {
        userName = s.toString();
    }

    public void firePassEdit(CharSequence s) {
        pass = s.toString();
    }

    private boolean isValidIpAddress(String ipv4) {
        if(trimmedIsEmpty(ipv4)){
            return false;
        }

        ipv4 = ipv4.trim();

        String[] blocks = ipv4.split("\\.");

        if(blocks.length != 4){
            return false;
        }

        for (String block : blocks) {
            try {
                int num = Integer.parseInt(block);

                if (num > 255 || num < 0) {
                    return false;
                }
            } catch (Exception e){
                return false;
            }
        }

        return true;
    }

    private boolean validateData() {
        try {
            try {
                int portInt = Integer.parseInt(port);
                if (portInt <= 0) {
                    throw new Exception("Invalid port");
                }
            } catch (NumberFormatException e) {
                throw new Exception("Invalid port");
            }

            if (!isValidIpAddress(address)) {
                throw new Exception("Invalid address");
            }

            if (authEnabled && trimmedIsEmpty(userName)) {
                throw new Exception("Invalid username");
            }

            if (authEnabled && trimmedIsEmpty(pass)) {
                throw new Exception("Invalid password");
            }
        } catch (Exception e) {
            showError(getView(), e);
            return false;
        }

        return true;
    }

    public void fireSaveClick() {
        if (!validateData()) {
            return;
        }

        final String finalAddress = address.trim();
        final int finalPort = Integer.parseInt(port.trim());

        if (authEnabled) {
            settings.put(finalAddress, finalPort, userName, pass);
        } else {
            settings.put(finalAddress, finalPort);
        }

        getView().goBack();
    }
}
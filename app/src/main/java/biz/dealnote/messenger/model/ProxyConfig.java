package biz.dealnote.messenger.model;

import biz.dealnote.messenger.api.model.Identificable;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxyConfig implements Identificable {

    private final int id;

    private final String address;

    private final int port;

    private boolean authEnabled;

    private String user;

    private String pass;

    public ProxyConfig(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public ProxyConfig setAuth(String user, String pass) {
        this.authEnabled = true;
        this.user = user;
        this.pass = pass;
        return this;
    }

    public boolean isAuthEnabled() {
        return authEnabled;
    }

    public String getPass() {
        return pass;
    }

    public String getUser() {
        return user;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProxyConfig config = (ProxyConfig) o;
        return id == config.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
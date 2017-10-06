package biz.dealnote.messenger.upload;

import biz.dealnote.messenger.api.model.server.UploadServer;

public class BaseUploadResponse {

    private Throwable throwable;

    private UploadServer server;

    public BaseUploadResponse setServer(UploadServer server) {
        this.server = server;
        return this;
    }

    public UploadServer getServer() {
        return server;
    }

    public BaseUploadResponse() {}

    public void setError(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public boolean isSuccess(){
        // must bee overrided in child classes
        return false;
    }
}
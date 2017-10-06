package biz.dealnote.messenger.upload;

public interface UploadCallback {

    void onPrepareToUpload(UploadObject uploadObject);
    void onProgressUpdate(UploadObject uploadObject, int primaryProgress);
    void onError(UploadObject uploadObject, Throwable error);
    void onSuccess(UploadObject uploadObject, BaseUploadResponse resonse);
    void onCanceled(UploadObject uploadObject);

}

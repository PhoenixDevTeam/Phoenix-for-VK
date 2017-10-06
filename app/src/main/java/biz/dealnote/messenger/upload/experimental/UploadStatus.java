package biz.dealnote.messenger.upload.experimental;

/**
 * Created by ruslan.kolbasa on 06.10.2016.
 * phoenix
 */
public class UploadStatus<T> {

    private T response;
    private int progress;

    public UploadStatus(T response) {
        this.response = response;
        this.progress = 100;
    }

    public UploadStatus(int progress) {
        this.progress = progress;
    }

    public T getResponse() {
        return response;
    }

    public int getProgress() {
        return progress;
    }

    @Override
    public String toString() {
        return response == null ? "InProgress[" + progress + "]" : "Success[" + response + "]";
    }


}

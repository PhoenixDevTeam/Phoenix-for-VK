package biz.dealnote.messenger.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public final class ChatConfig implements Parcelable {

    public static final Creator<ChatConfig> CREATOR = new Creator<ChatConfig>() {
        @Override
        public ChatConfig createFromParcel(Parcel in) {
            return new ChatConfig(in);
        }

        @Override
        public ChatConfig[] newArray(int size) {
            return new ChatConfig[size];
        }
    };

    public ModelsBundle getModels() {
        return models;
    }

    private ModelsBundle models;
    private boolean closeOnSend;
    private String initialText;
    private ArrayList<Uri> uploadFiles;

    public ChatConfig() {
        this.models = new ModelsBundle();
    }

    private ChatConfig(Parcel in) {
        this.closeOnSend = in.readByte() != 0;
        this.models = in.readParcelable(ModelsBundle.class.getClassLoader());
        this.initialText = in.readString();
        this.uploadFiles = in.createTypedArrayList(Uri.CREATOR);
    }

    public void setModels(ModelsBundle models) {
        this.models = models;
    }

    public boolean isCloseOnSend() {
        return closeOnSend;
    }

    public void setCloseOnSend(boolean closeOnSend) {
        this.closeOnSend = closeOnSend;
    }

    public String getInitialText() {
        return initialText;
    }

    public void setInitialText(String initialText) {
        this.initialText = initialText;
    }

    public ArrayList<Uri> getUploadFiles() {
        return uploadFiles;
    }

    public void setUploadFiles(ArrayList<Uri> uploadFiles) {
        this.uploadFiles = uploadFiles;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (closeOnSend ? 1 : 0));
        dest.writeParcelable(models, flags);
        dest.writeString(initialText);
        dest.writeTypedList(uploadFiles);
    }

    public void appendAll(Iterable<? extends AbsModel> models) {
        for (AbsModel model : models) {
            this.models.append(model);
        }
    }
}
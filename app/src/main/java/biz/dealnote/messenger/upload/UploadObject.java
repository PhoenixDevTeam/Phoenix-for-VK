package biz.dealnote.messenger.upload;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.atomic.AtomicInteger;

import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.util.ParcelUtils;

public class UploadObject extends AbsModel implements Parcelable, Identificable {

    public static final int IMAGE_SIZE_800 = 800;
    public static final int IMAGE_SIZE_1200 = 1200;
    public static final int IMAGE_SIZE_FULL = -1;

    public static final int STATUS_QUEUE = 1;
    public static final int STATUS_UPLOADING = 2;
    public static final int STATUS_ERROR = 3;
    public static final int STATUS_CANCELLING = 4;

    private final int accountId;

    /* Идентификатор обьекта загрузки, генерируется базой данных при вставке */
    private int id;

    /* Локальный путь к файлу */
    private Uri fileUri;

    /* Идентификатор обьекта, к которому прикрепляется файл
       (локальный код сообщения, поста, комментария) */
    private UploadDestination destination;

    /* Размер изображения (только для изображений)*/
    private int size;

    /* Текущий статус загрузки (QUEUE,UPLOADING,ERROR,CANCELLING)*/
    private int status;

    /* Текущий прогресс загрузки */
    private int progress;

    /* Текст ошибки, если она произошла */
    private String errorText;

    /** Дополнительные данные */
    private Long fileId;

    private boolean autoCommit;

    public UploadObject(int accountId) {
        this.accountId = accountId;
        this.id = getIncrementedUploadId();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public UploadObject setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    public UploadObject setId(int id) {
        this.id = id;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    @Override
    public int getId() {
        return id;
    }

    public boolean hasThumbnail(){
        return fileId != null;
    }

    public Uri buildThumnailUri(){
        return LocalPhoto.buildUriForPicasso(fileId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadObject that = (UploadObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "UploadObject{" +
                "accountId=" + accountId +
                ", id=" + id +
                //", destination=" + destination +
                ", status=" + status +
                ", progress=" + progress +
                ", errorText='" + errorText + '\'' +
                '}';
    }

    protected UploadObject(Parcel in) {
        super(in);
        this.accountId = in.readInt();
        this.id = in.readInt();

        if(id >= IDGEN.get()){
            IDGEN.set(id + 1);
        }

        this.fileUri = in.readParcelable(Uri.class.getClassLoader());
        this.destination = in.readParcelable(UploadDestination.class.getClassLoader());
        this.size = in.readInt();
        this.status = in.readInt();
        this.progress = in.readInt();
        this.errorText = in.readString();
        this.fileId = ParcelUtils.readObjectLong(in);
    }

    public static final Creator<UploadObject> CREATOR = new Creator<UploadObject>() {
        @Override
        public UploadObject createFromParcel(Parcel in) {
            return new UploadObject(in);
        }

        @Override
        public UploadObject[] newArray(int size) {
            return new UploadObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(accountId);
        dest.writeInt(id);
        dest.writeParcelable(fileUri, flags);
        dest.writeParcelable(destination, flags);
        dest.writeInt(size);
        dest.writeInt(status);
        dest.writeInt(progress);
        dest.writeString(errorText);
        ParcelUtils.writeObjectLong(dest, fileId);
    }

    public UploadObject setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
        return this;
    }

    public UploadObject setDestination(UploadDestination destination) {
        this.destination = destination;
        return this;
    }

    public UploadObject setSize(int size) {
        this.size = size;
        return this;
    }

    public UploadObject setStatus(int status) {
        this.status = status;
        return this;
    }

    public UploadObject setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public UploadObject setErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    public UploadObject setFileId(Long fileId) {
        this.fileId = fileId;
        return this;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public UploadDestination getDestination() {
        return destination;
    }

    public int getSize() {
        return size;
    }

    public int getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public String getErrorText() {
        return errorText;
    }

    public Long getFileId() {
        return fileId;
    }

    private static final AtomicInteger IDGEN = new AtomicInteger();

    private int getIncrementedUploadId() {
        return IDGEN.incrementAndGet();
    }
}

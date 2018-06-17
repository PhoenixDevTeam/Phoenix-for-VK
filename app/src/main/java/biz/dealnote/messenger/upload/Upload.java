package biz.dealnote.messenger.upload;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import biz.dealnote.messenger.api.model.Identificable;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.util.ParcelUtils;

public class Upload extends AbsModel implements Parcelable, Identificable {

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

    public Upload(int accountId) {
        this.accountId = accountId;
        this.id = getIncrementedUploadId();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public Upload setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
        return this;
    }

    public Upload setId(int id) {
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
        Upload that = (Upload) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    protected Upload(Parcel in) {
        super(in);
        this.accountId = in.readInt();
        this.id = in.readInt();
        this.fileUri = in.readParcelable(Uri.class.getClassLoader());
        this.destination = in.readParcelable(UploadDestination.class.getClassLoader());
        this.size = in.readInt();
        this.status = in.readInt();
        this.progress = in.readInt();
        this.errorText = in.readString();
        this.fileId = ParcelUtils.readObjectLong(in);
    }

    public static final Creator<Upload> CREATOR = new Creator<Upload>() {
        @Override
        public Upload createFromParcel(Parcel in) {
            return new Upload(in);
        }

        @Override
        public Upload[] newArray(int size) {
            return new Upload[size];
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

    public Upload setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
        return this;
    }

    public Upload setDestination(UploadDestination destination) {
        this.destination = destination;
        return this;
    }

    public Upload setSize(int size) {
        this.size = size;
        return this;
    }

    public Upload setStatus(int status) {
        this.status = status;
        return this;
    }

    public Upload setProgress(int progress) {
        this.progress = progress;
        return this;
    }

    public Upload setErrorText(String errorText) {
        this.errorText = errorText;
        return this;
    }

    public Upload setFileId(Long fileId) {
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

    private static final AtomicInteger IDGEN = new AtomicInteger(new Random().nextInt(5000));

    private int getIncrementedUploadId() {
        return IDGEN.incrementAndGet();
    }
}
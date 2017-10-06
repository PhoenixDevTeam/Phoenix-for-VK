package biz.dealnote.messenger.model.selection;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ruslan Kolbasa on 16.08.2017.
 * phoenix
 */
public class VkPhotosSelectableSource extends AbsSelectableSource implements Parcelable {

    private final int accountId;
    private final int ownerId;

    /**
     * @param accountId Кто будет загружать список фото
     * @param ownerId Чьи фото будут загружатся
     */
    public VkPhotosSelectableSource(int accountId, int ownerId) {
        super(Types.VK_PHOTOS);
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public int getAccountId() {
        return accountId;
    }

    protected VkPhotosSelectableSource(Parcel in) {
        super(in);
        accountId = in.readInt();
        ownerId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(accountId);
        dest.writeInt(ownerId);
    }

    public static final Creator<VkPhotosSelectableSource> CREATOR = new Creator<VkPhotosSelectableSource>() {
        @Override
        public VkPhotosSelectableSource createFromParcel(Parcel in) {
            return new VkPhotosSelectableSource(in);
        }

        @Override
        public VkPhotosSelectableSource[] newArray(int size) {
            return new VkPhotosSelectableSource[size];
        }
    };

    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
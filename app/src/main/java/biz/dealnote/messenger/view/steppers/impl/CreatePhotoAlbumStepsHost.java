package biz.dealnote.messenger.view.steppers.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.view.steppers.base.AbsStepsHost;

public class CreatePhotoAlbumStepsHost extends AbsStepsHost<CreatePhotoAlbumStepsHost.PhotoAlbumState> {

    public static final int STEP_TITLE_AND_DESCRIPTION = 0;
    public static final int STEP_UPLOAD_AND_COMMENTS = 1;
    public static final int STEP_PRIVACY_VIEW = 2;
    public static final int STEP_PRIVACY_COMMENT = 3;

    public CreatePhotoAlbumStepsHost() {
        super(new PhotoAlbumState());
    }

    @Override
    public int getStepsCount() {
        return 4;
    }

    @Override
    public int getStepTitle(int index) {
        switch (index){
            case STEP_TITLE_AND_DESCRIPTION:
                return R.string.enter_main_album_info;
            case STEP_UPLOAD_AND_COMMENTS:
                return R.string.additional_settings;
            case STEP_PRIVACY_VIEW:
                return R.string.privacy_view;
            case STEP_PRIVACY_COMMENT:
                return R.string.privacy_comment;
            default:
                throw new IllegalStateException("Invalid step index");
        }
    }

    @Override
    public boolean canMoveNext(int index, @NonNull PhotoAlbumState state) {
        switch (index){
            case STEP_TITLE_AND_DESCRIPTION:
                return !TextUtils.isEmpty(state.title) && state.getTitle().trim().length() > 1;
            case STEP_UPLOAD_AND_COMMENTS:
            case STEP_PRIVACY_VIEW:
            case STEP_PRIVACY_COMMENT:
                return true;
            default:
                throw new IllegalStateException("Invalid step index, index: " + index);
        }
    }

    private boolean mAdditionalOptionsEnable;
    private boolean mPrivacySettingsEnable;

    public boolean isPrivacySettingsEnable() {
        return mPrivacySettingsEnable;
    }

    public CreatePhotoAlbumStepsHost setPrivacySettingsEnable(boolean privacySettingsEnable) {
        this.mPrivacySettingsEnable = privacySettingsEnable;
        return this;
    }

    public boolean isAdditionalOptionsEnable() {
        return mAdditionalOptionsEnable;
    }

    public void setAdditionalOptionsEnable(boolean additionalOptionsEnable) {
        this.mAdditionalOptionsEnable = additionalOptionsEnable;
    }

    @Override
    public int getNextButtonText(int index) {
        return index == getStepsCount() - 1 ? R.string.finish : R.string.button_continue;
    }

    @Override
    public int getCancelButtonText(int index) {
        return index == 0 ?  R.string.button_cancel : R.string.button_back;
    }

    public static class PhotoAlbumState extends AbsStepsHost.AbsState implements Parcelable {

        private String title;
        private String description;
        private Privacy privacyView;
        private Privacy privacyComment;
        private boolean uploadByAdminsOnly;
        private boolean commentsDisabled;

        protected PhotoAlbumState(Parcel in) {
            super(in);
            this.title = in.readString();
            this.description = in.readString();
            this.privacyView = in.readParcelable(Privacy.class.getClassLoader());
            this.privacyComment = in.readParcelable(Privacy.class.getClassLoader());
            this.uploadByAdminsOnly = in.readByte() != 0;
            this.commentsDisabled = in.readByte() != 0;
        }

        public PhotoAlbumState() {
            this.privacyView = new Privacy();
            this.privacyComment = new Privacy();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(title);
            dest.writeString(description);
            dest.writeParcelable(privacyView, flags);
            dest.writeParcelable(privacyComment, flags);
            dest.writeByte((byte) (uploadByAdminsOnly ? 1 : 0));
            dest.writeByte((byte) (commentsDisabled ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PhotoAlbumState> CREATOR = new Creator<PhotoAlbumState>() {
            @Override
            public PhotoAlbumState createFromParcel(Parcel in) {
                return new PhotoAlbumState(in);
            }

            @Override
            public PhotoAlbumState[] newArray(int size) {
                return new PhotoAlbumState[size];
            }
        };

        public String getTitle() {
            return title;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setDescription(String description) {
            this.description = description;
            return this;
        }

        public Privacy getPrivacyView() {
            return privacyView;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setPrivacyView(@NonNull Privacy privacyView) {
            this.privacyView = privacyView;
            return this;
        }

        public Privacy getPrivacyComment() {
            return privacyComment;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setPrivacyComment(@NonNull Privacy privacyComment) {
            this.privacyComment = privacyComment;
            return this;
        }

        public boolean isUploadByAdminsOnly() {
            return uploadByAdminsOnly;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setUploadByAdminsOnly(boolean uploadByAdminsOnly) {
            this.uploadByAdminsOnly = uploadByAdminsOnly;
            return this;
        }

        public boolean isCommentsDisabled() {
            return commentsDisabled;
        }

        public CreatePhotoAlbumStepsHost.PhotoAlbumState setCommentsDisabled(boolean commentsDisabled) {
            this.commentsDisabled = commentsDisabled;
            return this;
        }

        @Override
        public String toString() {
            return "PhotoAlbumState{" +
                    "title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", privacyView=" + privacyView +
                    ", privacyComment=" + privacyComment +
                    ", uploadByAdminsOnly=" + uploadByAdminsOnly +
                    ", commentsDisabled=" + commentsDisabled +
                    '}';
        }
    }
}

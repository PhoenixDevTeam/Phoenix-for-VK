package biz.dealnote.messenger.dialog;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.util.Objects;

public class ImageSizeAlertDialog {

    private ImageSizeAlertDialog(ImageSizeAlertDialog.Builder builder) {
        this.mActivity = builder.mActivity;
        this.mOnCancelCallback = builder.mOnCancelCallback;
        this.mOnSelectedCallback = builder.mOnSelectedCallback;
    }

    public void show() {
        new AlertDialog.Builder(mActivity)
                .setTitle(mActivity.getString(R.string.select_image_size_title))
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j) -> {
                    int selectedSize = UploadObject.IMAGE_SIZE_FULL;
                    switch (j) {
                        case 0:
                            selectedSize = UploadObject.IMAGE_SIZE_800;
                            break;
                        case 1:
                            selectedSize = UploadObject.IMAGE_SIZE_1200;
                            break;
                        case 2:
                            selectedSize = UploadObject.IMAGE_SIZE_FULL;
                            break;
                    }

                    if (Objects.nonNull(mOnSelectedCallback)) {
                        mOnSelectedCallback.onSizeSelected(selectedSize);
                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.button_cancel, (dialog1, which) -> {
                    if (Objects.nonNull(mOnCancelCallback)) {
                        mOnCancelCallback.onCancel();
                    }
                })
                .show();
    }

    public interface OnSelectedCallback {
        void onSizeSelected(int size);
    }

    public interface OnCancelCallback {
        void onCancel();
    }

    private final Activity mActivity;
    private OnSelectedCallback mOnSelectedCallback;
    private OnCancelCallback mOnCancelCallback;

    public static class Builder {

        private final Activity mActivity;
        private OnSelectedCallback mOnSelectedCallback;
        private OnCancelCallback mOnCancelCallback;

        public Builder(Activity activity) {
            this.mActivity = activity;
        }

        public Builder setOnSelectedCallback(OnSelectedCallback onSelectedCallback) {
            this.mOnSelectedCallback = onSelectedCallback;
            return this;
        }

        public Builder setOnCancelCallback(OnCancelCallback onCancelCallback) {
            this.mOnCancelCallback = onCancelCallback;
            return this;
        }

        public ImageSizeAlertDialog build() {
            return new ImageSizeAlertDialog(this);
        }

        public void show() {
            build().show();
        }
    }

    public static void showUploadPhotoSizeIfNeed(Activity activity, final Callback callback) {
        Integer size = Settings.get()
                .main()
                .getUploadImageSize();

        if (Objects.isNull(size)) {
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.select_image_size_title))
                    .setItems(R.array.array_image_sizes_names, (dialogInterface, j) -> {
                        int selectedSize = UploadObject.IMAGE_SIZE_FULL;

                        switch (j) {
                            case 0:
                                selectedSize = UploadObject.IMAGE_SIZE_800;
                                break;
                            case 1:
                                selectedSize = UploadObject.IMAGE_SIZE_1200;
                                break;
                            case 2:
                                selectedSize = UploadObject.IMAGE_SIZE_FULL;
                                break;
                        }

                        callback.onSizeSelected(selectedSize);
                    }).setCancelable(true).create();
            dialog.show();
        } else {
            callback.onSizeSelected(size);
        }
    }

    public interface Callback {
        void onSizeSelected(int size);
    }
}

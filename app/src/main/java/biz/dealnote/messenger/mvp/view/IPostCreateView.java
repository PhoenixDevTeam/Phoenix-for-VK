package biz.dealnote.messenger.mvp.view;

import android.net.Uri;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by admin on 20.01.2017.
 * phoenix
 */
public interface IPostCreateView extends IBasePostEditView, IToolbarView {
    void goBack();

    void displayUploadUriSizeDialog(@NonNull List<Uri> uris);
}
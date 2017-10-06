package biz.dealnote.messenger.view.steppers.impl;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.view.steppers.base.AbsStepHolder;
import biz.dealnote.messenger.view.steppers.base.BaseHolderListener;

public class CreatePhotoAlbumStep1Holder extends AbsStepHolder<CreatePhotoAlbumStepsHost> {

    private TextView mTitle;
    private TextView mDescription;
    private ActionListener mActionListener;

    public CreatePhotoAlbumStep1Holder(ViewGroup parent, @NonNull ActionListener actionListener) {
        super(parent, R.layout.step_create_photo_album_1, CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION);
        this.mActionListener = actionListener;
    }

    @Override
    public void initInternalView(View contentView) {
        mTitle = (TextView) contentView.findViewById(R.id.title);
        mDescription = (TextView) contentView.findViewById(R.id.description);

        mTitle.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mActionListener.onTitleEdited(s);
            }
        });

        mTitle.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mActionListener.onDescriptionEdited(s);
            }
        });
    }

    public interface ActionListener extends BaseHolderListener {
        void onTitleEdited(CharSequence text);
        void onDescriptionEdited(CharSequence text);
    }

    @Override
    protected void bindViews(CreatePhotoAlbumStepsHost host) {
        mTitle.setText(host.getState().getTitle());
        mDescription.setText(host.getState().getDescription());
    }
}

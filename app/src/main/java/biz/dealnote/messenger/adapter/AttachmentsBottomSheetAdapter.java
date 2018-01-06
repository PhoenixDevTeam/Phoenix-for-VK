package biz.dealnote.messenger.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.EventListener;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.holder.IdentificableHolder;
import biz.dealnote.messenger.adapter.holder.SharedHolders;
import biz.dealnote.messenger.api.PicassoInstance;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.FwdMessages;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.upload.UploadObject;
import biz.dealnote.messenger.view.CircleRoadProgress;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.isEmpty;

/**
 * Created by admin on 15.04.2017.
 * phoenix
 */
public class AttachmentsBottomSheetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ERROR_COLOR = Color.parseColor("#ff0000");
    private static final int VTYPE_BUTTON = 0;
    private static final int VTYPE_ENTRY = 1;

    private final List<AttachmenEntry> data;
    private final ActionListener actionListener;
    private SharedHolders<EntryHolder> holders;

    @ColorInt
    private int nonErrorTextColor;
    private int nextHolderId;

    public AttachmentsBottomSheetAdapter(Context context, List<AttachmenEntry> data, ActionListener actionListener) {
        this.data = data;
        this.actionListener = actionListener;
        this.holders = new SharedHolders<>(false);
        this.nonErrorTextColor = CurrentTheme.getPrimaryTextColorCode(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VTYPE_BUTTON) {
            return new ImagesButtonHolder(inflater.inflate(R.layout.button_add_photo, parent, false));
        }

        EntryHolder holder = new EntryHolder(inflater.inflate(R.layout.message_attachments_entry, parent, false));
        holder.attachId(generateHolderId());
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VTYPE_BUTTON:
                bindAddPhotoButton((ImagesButtonHolder) holder);
                break;
            case VTYPE_ENTRY:
                bindEntryHolder((EntryHolder) holder, position);
                break;
        }
    }

    private void bindEntryHolder(EntryHolder holder, int position) {
        int dataPosition = position - 1;
        holders.put(dataPosition, holder);

        final AttachmenEntry entry = data.get(dataPosition);
        final AbsModel model = entry.getAttachment();

        if (model instanceof Photo) {
            bindImageHolder(holder, (Photo) model);
        } else if (model instanceof UploadObject) {
            bindUploading(holder, (UploadObject) model);
        } else if(model instanceof Post){
            bindPost(holder, (Post) model);
        } else if(model instanceof Video){
            bindVideo(holder, (Video) model);
        } else if(model instanceof FwdMessages){
            bindMessages(holder, (FwdMessages) model);
        } else if(model instanceof Document){
            bindDoc(holder, (Document) model);
        }

        holder.buttomRemove.setOnClickListener(v -> actionListener.onButtonRemoveClick(entry));
    }

    @SuppressWarnings("unused")
    private void bindMessages(EntryHolder holder, FwdMessages messages){
        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);

        holder.title.setText(R.string.messages);

        bindImageView(holder, null);
    }

    private void bindImageView(EntryHolder holder, String url){
        if(isEmpty(url)){
            PicassoInstance.with().cancelRequest(holder.image);
            holder.image.setImageResource(R.drawable.background_gray);
        } else {
            PicassoInstance.with()
                    .load(url)
                    .placeholder(R.drawable.background_gray)
                    .into(holder.image);
        }
    }

    private void bindVideo(EntryHolder holder, Video video){
        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(video.getTitle());

        bindImageView(holder, video.get320orSmallerPhoto());
    }

    private void bindDoc(EntryHolder holder, Document doc){
        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(doc.getTitle());

        String imgUrl = doc.getPreviewWithSize(PhotoSize.Q, false);

        bindImageView(holder, imgUrl);
    }

    private void bindPost(EntryHolder holder, Post post){
        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);

        String title = post.getTextCopiesInclude();
        if(isEmpty(title)){
            holder.title.setText(R.string.attachment_wall_post);
        } else {
            holder.title.setText(title);
        }

        String imgUrl = post.findFirstImageCopiesInclude(PhotoSize.Q, false);
        bindImageView(holder, imgUrl);
    }

    private void bindUploading(EntryHolder holder, UploadObject upload) {
        holder.tintView.setVisibility(View.VISIBLE);

        boolean inProgress = upload.getStatus() == UploadObject.STATUS_UPLOADING;
        holder.progress.setVisibility(inProgress ? View.VISIBLE : View.INVISIBLE);
        if (inProgress) {
            holder.progress.changePercentage(upload.getProgress());
        } else {
            holder.progress.changePercentage(0);
        }

        @ColorInt
        int titleColor = nonErrorTextColor;

        switch (upload.getStatus()) {
            case UploadObject.STATUS_UPLOADING:
                String precentText = upload.getProgress() + "%";
                holder.title.setText(precentText);
                break;
            case UploadObject.STATUS_CANCELLING:
                holder.title.setText(R.string.cancelling);
                break;
            case UploadObject.STATUS_QUEUE:
                holder.title.setText(R.string.in_order);
                break;
            case UploadObject.STATUS_ERROR:
                holder.title.setText(R.string.error);
                titleColor = ERROR_COLOR;
                break;
        }

        holder.title.setTextColor(titleColor);

        if (upload.hasThumbnail()) {
            PicassoInstance.with()
                    .load(upload.buildThumnailUri())
                    .placeholder(R.drawable.background_gray)
                    .into(holder.image);
        } else {
            PicassoInstance.with().cancelRequest(holder.image);
            holder.image.setImageResource(R.drawable.background_gray);
        }
    }

    public void changeUploadProgress(int dataPosition, int progress, boolean smoothly) {
        EntryHolder holder = holders.findOneByEntityId(dataPosition);
        if (nonNull(holder)) {
            String precentText = progress + "%";
            holder.title.setText(precentText);

            if (smoothly) {
                holder.progress.changePercentageSmoothly(progress);
            } else {
                holder.progress.changePercentage(progress);
            }
        }
    }

    private void bindImageHolder(EntryHolder holder, Photo photo) {
        String url = photo.getUrlForSize(PhotoSize.Q, false);

        holder.progress.setVisibility(View.INVISIBLE);
        holder.tintView.setVisibility(View.GONE);
        holder.title.setText(R.string.photo);

        bindImageView(holder, url);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VTYPE_BUTTON : VTYPE_ENTRY;
    }

    private void bindAddPhotoButton(ImagesButtonHolder holder) {
        holder.button.setOnClickListener(v -> actionListener.onAddPhotoButtonClick());
    }

    @Override
    public int getItemCount() {
        return data.size() + 1;
    }

    private int generateHolderId() {
        nextHolderId++;
        return nextHolderId;
    }

    public interface ActionListener extends EventListener {
        void onAddPhotoButtonClick();

        void onButtonRemoveClick(AttachmenEntry entry);
    }

    private static class ImagesButtonHolder extends RecyclerView.ViewHolder {

        View button;

        ImagesButtonHolder(View itemView) {
            super(itemView);
            this.button = itemView.findViewById(R.id.button);
        }
    }

    private static class EntryHolder extends RecyclerView.ViewHolder implements IdentificableHolder {

        ImageView image;
        TextView title;
        View buttomRemove;
        CircleRoadProgress progress;
        View tintView;

        EntryHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.image);
            this.title = itemView.findViewById(R.id.title);
            this.buttomRemove = itemView.findViewById(R.id.progress_root);
            this.progress = itemView.findViewById(R.id.progress_view);
            this.tintView = itemView.findViewById(R.id.tint_view);
        }

        @Override
        public int getHolderId() {
            return (int) tintView.getTag();
        }

        void attachId(int id) {
            this.tintView.setTag(id);
        }
    }
}
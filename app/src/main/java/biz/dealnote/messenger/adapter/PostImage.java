package biz.dealnote.messenger.adapter;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSizes;
import biz.dealnote.messenger.view.mozaik.PostImagePosition;

public class PostImage {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;
    public static final int TYPE_GIF = 3;

    private final int type;
    private final AbsModel attachment;
    private PostImagePosition position;

    public PostImage(AbsModel model, int type){
        this.attachment = model;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public AbsModel getAttachment() {
        return attachment;
    }

    public PostImage setPosition(PostImagePosition position) {
        this.position = position;
        return this;
    }

    public PostImagePosition getPosition() {
        return position;
    }

    public int getWidth(){
        switch (type){
            case TYPE_IMAGE:
                Photo photo = (Photo) attachment;
                return photo.getWidth() == 0 ? 100 : photo.getWidth();
            case TYPE_VIDEO:
                return 640;
            case TYPE_GIF:
                Document document = (Document) attachment;
                PhotoSizes.Size max = document.getMaxPreviewSize(false);
                return max == null ? 640 : max.getW();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public int getHeight(){
        switch (type){
            case TYPE_IMAGE:
                Photo photo = (Photo) attachment;
                return photo.getHeight() == 0 ? 100 : photo.getHeight();
            case TYPE_VIDEO:
                return 360;
            case TYPE_GIF:
                Document document = (Document) attachment;
                PhotoSizes.Size max = document.getMaxPreviewSize(false);
                return max == null ? 480 : max.getH();
            default:
                throw new UnsupportedOperationException();
        }
    }

    public float getAspectRatio(){
        return (float) getWidth() / (float) getHeight();
    }
}
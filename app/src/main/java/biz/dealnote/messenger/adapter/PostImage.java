package biz.dealnote.messenger.adapter;

import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.view.mozaik.PostImagePosition;

public class PostImage {

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = 2;

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
            default:
                throw new UnsupportedOperationException();
        }
    }

    public void setWidth(int width){
        if(type == TYPE_IMAGE){
            Photo photo = (Photo) attachment;
            photo.setWidth(width);
        }
    }

    public void setHeight(int height){
        if(type == TYPE_IMAGE){
            Photo photo = (Photo) attachment;
            photo.setHeight(height);
        }
    }

    @Override
    public String toString() {
        return "PostImage{" +
                "type=" + type +
                ", attachment=" + attachment +
                '}';
    }

    public float getAspectRatio(){
        return (float) getWidth() / (float) getHeight();
    }
}

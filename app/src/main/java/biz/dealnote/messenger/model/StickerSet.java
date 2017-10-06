package biz.dealnote.messenger.model;

import java.util.ArrayList;

/**
 * Created by admin on 08.01.2017.
 * phoenix
 */
public class StickerSet {

    private String photo70;

    private final ArrayList<Integer> ids;

    public StickerSet(){
        ids = new ArrayList<>();
    }

    public void addId(int id){
        ids.add(id);
    }

    public String getPhoto70() {
        return photo70;
    }

    public StickerSet setPhoto70(String photo70) {
        this.photo70 = photo70;
        return this;
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }
}

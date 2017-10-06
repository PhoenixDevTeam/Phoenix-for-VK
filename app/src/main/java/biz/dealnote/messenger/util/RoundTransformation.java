package biz.dealnote.messenger.util;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class RoundTransformation implements Transformation{

    @Override
    public Bitmap transform(Bitmap source) {
        return ImageHelper.getRoundedBitmap(source);
    }

    @Override
    public String key() {
        return "round()";
    }
}

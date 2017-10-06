package biz.dealnote.messenger.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.squareup.picasso.Transformation;

public class MaskTransformation implements Transformation {

    private static Paint mMaskingPaint = new Paint();
    private Context mContext;
    private int mMaskId;

    static {
        mMaskingPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    }

    /**
     * @param maskId If you change the mask file, please also rename the mask file, or Glide will get
     *               the cache with the old mask. Because getId() return the same values if using the
     *               same make file name. If you have a good idea please tell us, thanks.
     */
    public MaskTransformation(Context context, int maskId) {
        mContext = context;
        mMaskId = maskId;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Drawable mask = getMaskDrawable(mContext, mMaskId);

        Canvas canvas = new Canvas(result);
        mask.setBounds(0, 0, width, height);
        mask.draw(canvas);
        canvas.drawBitmap(source, 0, 0, mMaskingPaint);

        source.recycle();

        return result;
    }

    public static Drawable getMaskDrawable(Context context, int maskId) {
        Drawable drawable = ContextCompat.getDrawable(context, maskId);

        if (drawable == null) {
            throw new IllegalArgumentException("maskId is invalid");
        }

        return drawable;
    }

    @Override
    public String key() {
        return "MaskTransformation(maskId=" + mContext.getResources().getResourceEntryName(mMaskId)
                + ")";
    }
}
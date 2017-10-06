package biz.dealnote.messenger.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by admin on 08.10.2016.
 * phoenix
 */
@IntDef({DocType.TEXT, DocType.ARCHIVE, DocType.GIF, DocType.IMAGE,
        DocType.AUDIO, DocType.VIDEO, DocType.EBOOK, DocType.UNKNOWN})
@Retention(RetentionPolicy.SOURCE)
public @interface DocType {
    int TEXT = 1;
    int ARCHIVE = 2;
    int GIF = 3;
    int IMAGE = 4;
    int AUDIO = 5;
    int VIDEO = 6;
    int EBOOK = 7;
    int UNKNOWN = 8;
}

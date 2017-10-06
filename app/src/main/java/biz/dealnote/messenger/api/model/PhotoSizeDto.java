package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class PhotoSizeDto {

    @SerializedName("src")
    public String src;

    @SerializedName("type")
    public char type;

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;

    public static PhotoSizeDto create(char type, String url) {
        PhotoSizeDto dto = new PhotoSizeDto();
        dto.src = url;
        dto.type = type;
        return dto;
    }

    public static final class Type {
        /**
         * пропорциональная копия изображения с максимальной шириной 75px
         */
        public static final char S = 's';

        /**
         * пропорциональная копия изображения с максимальной шириной 130px
         */
        public static final char M = 'm';

        /**
         * пропорциональная копия изображения с максимальной шириной 604px
         */
        public static final char X = 'x';

        /**
         * пропорциональная копия изображения с максимальной стороной 807px
         */
        public static final char Y = 'y';

        /**
         * пропорциональная копия изображения с максимальным размером 1280x1024
         */
        public static final char Z = 'z';

        /**
         * пропорциональная копия изображения с максимальным размером 2560x2048px
         */
        public static final char W = 'w';

        /**
         * если соотношение "ширина/высота" исходного изображения меньше или равно 3:2,
         * то пропорциональная копия с максимальной шириной 130px.
         * Если соотношение "ширина/высота" больше 3:2, то копия обрезанного слева изображения
         * с максимальной шириной 130px и соотношением сторон 3:2
         */
        public static final char O = 'o';

        /**
         * если соотношение "ширина/высота" исходного изображения меньше или равно 3:2,
         * то пропорциональная копия с максимальной шириной 200px.
         * Если соотношение "ширина/высота" больше 3:2, то копия обрезанного слева и
         * справа изображения с максимальной шириной 200px и соотношением сторон 3:2
         */
        public static final char P = 'p';

        /**
         * если соотношение "ширина/высота" исходного изображения меньше или равно 3:2,
         * то пропорциональная копия с максимальной шириной 320px.
         * Если соотношение "ширина/высота" больше 3:2, то копия обрезанного слева и
         * справа изображения с максимальной шириной 320px и соотношением сторон 3:2
         */
        public static final char Q = 'q';

        /**
         * если соотношение "ширина/высота" исходного изображения меньше или равно 3:2,
         * то пропорциональная копия с максимальной шириной 510px.
         * Если соотношение "ширина/высота" больше 3:2, то копия обрезанного слева
         * и справа изображения с максимальной шириной 510px и соотношением сторон 3:2
         */
        public static final char R = 'r';
    }
}
package biz.dealnote.messenger.db.model;

/**
 * Created by Ruslan Kolbasa on 06.10.2017.
 * Phoenix-for-VK
 */
public class PhotoPatch {

    private Like like;

    public PhotoPatch setLike(Like like) {
        this.like = like;
        return this;
    }

    public Like getLike() {
        return like;
    }

    public static final class Like {

        private final int count;

        private final boolean liked;

        public Like(int count, boolean liked) {
            this.count = count;
            this.liked = liked;
        }

        public boolean isLiked() {
            return liked;
        }

        public int getCount() {
            return count;
        }
    }
}
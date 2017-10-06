package biz.dealnote.messenger.db.model;

/**
 * Created by admin on 05.09.2017.
 * phoenix
 */
public class PostPatch {

    private LikePatch likePatch;

    private DeletePatch deletePatch;

    private PinPatch pinPatch;

    public PostPatch withDeletion(boolean deleted){
        this.deletePatch = new DeletePatch(deleted);
        return this;
    }

    public PostPatch withPin(boolean pinned){
        this.pinPatch = new PinPatch(pinned);
        return this;
    }

    public PostPatch withLikes(int count, boolean usesLikes){
        this.likePatch = new LikePatch(usesLikes, count);
        return this;
    }

    public DeletePatch getDeletePatch() {
        return deletePatch;
    }

    public LikePatch getLikePatch() {
        return likePatch;
    }

    public PinPatch getPinPatch() {
        return pinPatch;
    }

    public static final class LikePatch {

        private final boolean liked;

        private final int count;

        private LikePatch(boolean liked, int count) {
            this.liked = liked;
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public boolean isLiked() {
            return liked;
        }
    }

    public static final class DeletePatch {

        private final boolean deleted;

        private DeletePatch(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDeleted() {
            return deleted;
        }
    }

    public static final class PinPatch {

        private final boolean pinned;

        private PinPatch(boolean pinned) {
            this.pinned = pinned;
        }

        public boolean isPinned() {
            return pinned;
        }
    }
}
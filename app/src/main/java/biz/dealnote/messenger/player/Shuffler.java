package biz.dealnote.messenger.player;

import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

public final class Shuffler {

    private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<Integer>();
    private final TreeSet<Integer> mPreviousNumbers = new TreeSet<Integer>();
    private final Random mRandom = new Random();
    private int mPrevious;
    private int mMaxHistorySize;

    public Shuffler(int maxHistorySize) {
        this.mMaxHistorySize = maxHistorySize;
    }

    public int nextInt(final int interval) {
        int next;
        do {
            next = mRandom.nextInt(interval);
        } while (next == mPrevious && interval > 1
                && !mPreviousNumbers.contains(next));

        mPrevious = next;
        mHistoryOfNumbers.add(mPrevious);
        mPreviousNumbers.add(mPrevious);
        cleanUpHistory();
        return next;
    }

    private void cleanUpHistory() {
        if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= mMaxHistorySize) {
            for (int i = 0; i < Math.max(1, mMaxHistorySize / 2); i++) {
                mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
            }
        }
    }
}
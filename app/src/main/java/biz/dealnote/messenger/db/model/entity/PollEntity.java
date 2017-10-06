package biz.dealnote.messenger.db.model.entity;

import java.util.List;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class PollEntity extends Entity {

    private final int id;

    private final int ownerId;

    private long creationTime;

    private String question;

    private int voteCount;

    private int myAnswerId;

    private boolean anonymous;

    private List<AnswerDbo> answers;

    private boolean board;

    public PollEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public static final class AnswerDbo {

        private final int id;

        private final String text;

        private final int voteCount;

        private final double rate;

        public AnswerDbo(int id, String text, int voteCount, double rate) {
            this.id = id;
            this.text = text;
            this.voteCount = voteCount;
            this.rate = rate;
        }

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public double getRate() {
            return rate;
        }

        public int getVoteCount() {
            return voteCount;
        }
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public PollEntity setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public PollEntity setQuestion(String question) {
        this.question = question;
        return this;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public PollEntity setVoteCount(int voteCount) {
        this.voteCount = voteCount;
        return this;
    }

    public int getMyAnswerId() {
        return myAnswerId;
    }

    public PollEntity setMyAnswerId(int myAnswerId) {
        this.myAnswerId = myAnswerId;
        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public PollEntity setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public List<AnswerDbo> getAnswers() {
        return answers;
    }

    public PollEntity setAnswers(List<AnswerDbo> answers) {
        this.answers = answers;
        return this;
    }

    public boolean isBoard() {
        return board;
    }

    public PollEntity setBoard(boolean board) {
        this.board = board;
        return this;
    }
}
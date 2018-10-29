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

    private int[] myAnswerIds;

    private boolean anonymous;

    private List<Answer> answers;

    private boolean board;

    public boolean closed;

    public int authorId;

    public boolean canVote;

    public boolean canEdit;

    public boolean canReport;

    public boolean canShare;

    public long endDate;

    public boolean multiple;

    public PollEntity setMultiple(boolean multiple) {
        this.multiple = multiple;
        return this;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public PollEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public boolean isClosed() {
        return closed;
    }

    public PollEntity setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public PollEntity setAuthorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public boolean isCanVote() {
        return canVote;
    }

    public PollEntity setCanVote(boolean canVote) {
        this.canVote = canVote;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public PollEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanReport() {
        return canReport;
    }

    public PollEntity setCanReport(boolean canReport) {
        this.canReport = canReport;
        return this;
    }

    public boolean isCanShare() {
        return canShare;
    }

    public PollEntity setCanShare(boolean canShare) {
        this.canShare = canShare;
        return this;
    }

    public long getEndDate() {
        return endDate;
    }

    public PollEntity setEndDate(long endDate) {
        this.endDate = endDate;
        return this;
    }

    public static final class Answer {

        private final int id;

        private final String text;

        private final int voteCount;

        private final double rate;

        public Answer(int id, String text, int voteCount, double rate) {
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

    public int[] getMyAnswerIds() {
        return myAnswerIds;
    }

    public PollEntity setMyAnswerIds(int[] myAnswerIds) {
        this.myAnswerIds = myAnswerIds;
        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public PollEntity setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public PollEntity setAnswers(List<Answer> answers) {
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
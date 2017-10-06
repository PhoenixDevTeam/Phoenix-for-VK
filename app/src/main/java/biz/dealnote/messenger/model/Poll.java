package biz.dealnote.messenger.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public class Poll extends AbsModel implements Parcelable {

    private final int id;

    private final int ownerId;

    private long creationTime;

    private String question;

    private int voteCount;

    private int myAnswerId;

    private boolean anonymous;

    private List<Answer> answers;

    private boolean board;

    public Poll(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected Poll(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        creationTime = in.readLong();
        question = in.readString();
        voteCount = in.readInt();
        myAnswerId = in.readInt();
        anonymous = in.readByte() != 0;
        answers = in.createTypedArrayList(Answer.CREATOR);
        board = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeLong(creationTime);
        dest.writeString(question);
        dest.writeInt(voteCount);
        dest.writeInt(myAnswerId);
        dest.writeByte((byte) (anonymous ? 1 : 0));
        dest.writeTypedList(answers);
        dest.writeByte((byte) (board ? 1 : 0));
    }

    public static final Creator<Poll> CREATOR = new Creator<Poll>() {
        @Override
        public Poll createFromParcel(Parcel in) {
            return new Poll(in);
        }

        @Override
        public Poll[] newArray(int size) {
            return new Poll[size];
        }
    };

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Poll setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getQuestion() {
        return question;
    }

    public Poll setQuestion(String question) {
        this.question = question;
        return this;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public Poll setVoteCount(int voteCount) {
        this.voteCount = voteCount;
        return this;
    }

    public int getMyAnswerId() {
        return myAnswerId;
    }

    public Poll setMyAnswerId(int myAnswerId) {
        this.myAnswerId = myAnswerId;
        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public Poll setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public Poll setAnswers(List<Answer> answers) {
        this.answers = answers;
        return this;
    }

    public boolean isBoard() {
        return board;
    }

    public Poll setBoard(boolean board) {
        this.board = board;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static class Answer extends AbsModel implements Parcelable {

        private final int id;

        private String text;

        private int voteCount;

        private double rate;

        public Answer(int id) {
            this.id = id;
        }

        protected Answer(Parcel in) {
            super(in);
            id = in.readInt();
            text = in.readString();
            voteCount = in.readInt();
            rate = in.readDouble();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(id);
            dest.writeString(text);
            dest.writeInt(voteCount);
            dest.writeDouble(rate);
        }

        public static final Creator<Answer> CREATOR = new Creator<Answer>() {
            @Override
            public Answer createFromParcel(Parcel in) {
                return new Answer(in);
            }

            @Override
            public Answer[] newArray(int size) {
                return new Answer[size];
            }
        };

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public Answer setText(String text) {
            this.text = text;
            return this;
        }

        public int getVoteCount() {
            return voteCount;
        }

        public Answer setVoteCount(int voteCount) {
            this.voteCount = voteCount;
            return this;
        }

        public double getRate() {
            return rate;
        }

        public Answer setRate(double rate) {
            this.rate = rate;
            return this;
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}

package biz.dealnote.messenger.media.record;

import android.media.MediaRecorder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import java.io.IOException;

/**
 * Created by ruslan.kolbasa on 05.10.2016.
 * phoenix
 */

public class Recorder {

    private String mFilePath;
    private MediaRecorder mRecorder;

    /**
     * Общая длительность записи
     */
    private long mPreviousSectionsDuration;
    private Long mCurrentRecordingSectionStartTime;
    private int mStatus;
    private boolean mReleased;

    public Recorder(@NonNull String filePath){
        mFilePath = filePath;
    }

    public void prepare() throws IOException {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.prepare();
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void start()  {
        assertRecorderNotNull();

        if(mStatus == Status.PAUSED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mRecorder.resume();
            } else {
                throw new IllegalStateException("Pause does not supported");
            }
        } else {
            mRecorder.start();
        }

        mCurrentRecordingSectionStartTime = System.currentTimeMillis();
        changeStatusTo(Status.RECORDING_NOW);
    }

    public int getMaxAmplitude() {
        assertRecorderNotNull();
        return mRecorder.getMaxAmplitude();
    }

    public long getCurrentRecordDuration() {
        if(mCurrentRecordingSectionStartTime != null){
            return mPreviousSectionsDuration + System.currentTimeMillis() - mCurrentRecordingSectionStartTime;
        } else {
            return mPreviousSectionsDuration;
        }
    }

    public int getStatus(){
        return mStatus;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public void pause(){
        assertRecorderNotNull();

        if (isPauseSupported()) {
            resetCurrentRecordTime();
            mRecorder.pause();
            changeStatusTo(Status.PAUSED);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static boolean isPauseSupported(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    private void resetCurrentRecordTime(){
        if (mCurrentRecordingSectionStartTime == null){
            return;
        }
        mPreviousSectionsDuration = mPreviousSectionsDuration + (System.currentTimeMillis() - mCurrentRecordingSectionStartTime);
        mCurrentRecordingSectionStartTime = null;
    }

    public void stopAndRelease(){
        assertRecorderNotNull();
        resetCurrentRecordTime();

        try {
            mRecorder.stop();
            mRecorder.release();
        } catch (RuntimeException e){
            e.printStackTrace();
            //TODO show toast
        }

        changeStatusTo(Status.NO_RECORD);

        mRecorder = null;
        mReleased = true;
    }

    public boolean isReleased() {
        return mReleased;
    }

    private void assertRecorderNotNull(){
        if(mRecorder == null){
            throw new IllegalStateException();
        }
    }

    private void changeStatusTo(int status){
        mStatus = status;
    }

    public static final class Status {
        public static final int NO_RECORD = 0;
        public static final int RECORDING_NOW = 1;
        public static final int PAUSED = 2;
    }
}

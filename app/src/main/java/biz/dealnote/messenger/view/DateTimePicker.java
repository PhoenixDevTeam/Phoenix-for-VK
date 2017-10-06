package biz.dealnote.messenger.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Unixtime;

public class DateTimePicker {

    private static final String TAG = DateTimePicker.class.getSimpleName();

    private long time;
    private Context context;
    private Callback callback;

    private DateTimePicker(Builder builder){
        this.time = builder.time;
        this.context = builder.context;
        this.callback = builder.callback;
    }

    private void show(){
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hours = calendar.get(Calendar.HOUR_OF_DAY);
        final int minutes = calendar.get(Calendar.MINUTE);

        Logger.d(TAG, "onTimerClick, init time: " + new Date(time));

        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int newYear, int newMonth, int newDay) {
                showTime(newYear, newMonth, newDay, hours, minutes);
            }
        }, year, month, day).show();
    }

    private void showTime(final int year, final int month, final int day, final int hour, final int minutes){
        new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int newHourOfDay, int newMinutes) {
                callback.onDateTimeSelected(Unixtime.of(year, month, day, newHourOfDay, newMinutes));
            }
        }, hour, minutes, true).show();
    }

    public static class Builder {

        private Context context;
        private Callback callback;
        private long time;

        public Builder(Context context) {
            this.context = context;
            this.time = System.currentTimeMillis();
        }

        public Builder setTime(long unixtime) {
            this.time = unixtime * 1000;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public void show(){
            new DateTimePicker(this).show();
        }
    }

    public interface Callback {
        void onDateTimeSelected(long unixtime);
    }
}

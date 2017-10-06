package biz.dealnote.messenger.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import biz.dealnote.messenger.util.Objects;

public class ActivityUtils {

    public static ArrayList<Uri> checkLocalStreams(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return null;
        }

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                return intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            }
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    ArrayList<Uri> streams = new ArrayList<>(1);
                    streams.add(uri);
                    return streams;
                }
            }
        }

        return null;
    }

    public static String checkLinks(Activity activity) {
        Intent intent = activity.getIntent();
        String link = null;
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return null;
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                link = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        return link;
    }

    public static boolean checkInputExist(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return false;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return false;
        }

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                return true;
            }
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM) || extras.containsKey(Intent.EXTRA_TEXT)) {
                return true;
            }
        }

        return false;
    }

    public static void resetInputPhotos(Activity activity) {
        if (activity.getIntent() == null) {
            return;
        }

        activity.getIntent().removeExtra(Intent.EXTRA_STREAM);
    }

    public static void resetInputText(Activity activity) {
        if (activity.getIntent() == null) {
            return;
        }

        activity.getIntent().removeExtra(Intent.EXTRA_TEXT);
    }

    public static boolean safeHasInputAttachments(Activity activity) {
        return activity.getIntent() != null
                && activity.getIntent().getExtras() != null
                && activity.getIntent().getExtras().containsKey(MainActivity.EXTRA_INPUT_ATTACHMENTS);
    }

    @Nullable
    public static android.support.v7.app.ActionBar supportToolbarFor(Fragment fragment) {
        if (fragment.getActivity() == null) {
            return null;
        }

        return ((AppCompatActivity) fragment.getActivity()).getSupportActionBar();
    }

    public static void setToolbarTitle(Fragment fragment, @StringRes int res){
        ActionBar actionBar = supportToolbarFor(fragment);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(res);
        }
    }

    public static void setToolbarTitle(Fragment fragment, String title){
        ActionBar actionBar = supportToolbarFor(fragment);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(title);
        }
    }

    public static void setToolbarSubtitle(Fragment fragment, @StringRes int res){
        ActionBar actionBar = supportToolbarFor(fragment);
        if(Objects.nonNull(actionBar)){
            actionBar.setSubtitle(res);
        }
    }

    public static void setToolbarSubtitle(Fragment fragment, String title){
        ActionBar actionBar = supportToolbarFor(fragment);
        if(Objects.nonNull(actionBar)){
            actionBar.setSubtitle(title);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public static void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
package biz.dealnote.messenger.service;

import android.content.Context;

import biz.dealnote.messenger.R;

import static biz.dealnote.messenger.util.Utils.isEmpty;

/**
 * Created by admin on 02.07.2016.
 * phoenix
 */
public class ErrorLocalizer {

    private static ApiLocalizer sApiLocalizer = new ApiLocalizer();
    private static AppLocalizer sAppLocalizer = new AppLocalizer();

    public interface Localizer {
        String getMessage(Context context, int code, String ifUnknown, Object... params);
    }

    public static class BaseLocazer implements Localizer {
        @Override
        public String getMessage(Context context, int code, String ifUnknown, Object... params) {
            return isEmpty(ifUnknown) ? context.getString(R.string.unknown_error) : ifUnknown;
        }
    }

    public static class ApiLocalizer extends BaseLocazer {
        @Override
        public String getMessage(Context context, int code, String ifUnknown, Object... params) {
            switch (code){
                case 1: return context.getString(R.string.api_error_1);
                case 2: return context.getString(R.string.api_error_2);
                case 3: return context.getString(R.string.api_error_3);
                case 4: return context.getString(R.string.api_error_4);
                case ApiErrorCodes.USER_AUTHORIZATION_FAILED: return context.getString(R.string.api_error_5);
                case 6: return context.getString(R.string.api_error_6);
                case 7: return context.getString(R.string.api_error_7);
                case 8: return context.getString(R.string.api_error_8);
                case 9: return context.getString(R.string.api_error_9);
                case 10: return context.getString(R.string.api_error_10);
                case 11: return context.getString(R.string.api_error_11);
                case ApiErrorCodes.CAPTCHA_NEED: return context.getString(R.string.api_error_14);
                case ApiErrorCodes.ACCESS_DENIED: return context.getString(R.string.api_error_15);
                case 16: return context.getString(R.string.api_error_16);
                case 17: return context.getString(R.string.api_error_17);
                case ApiErrorCodes.PAGE_HAS_BEEN_REMOVED_OR_BLOCKED: return context.getString(R.string.api_error_18);
                case 19: return context.getString(R.string.api_error_19);
                case 20: return context.getString(R.string.api_error_20);
                case 21: return context.getString(R.string.api_error_21);
                case 23: return context.getString(R.string.api_error_23);
                case 24: return context.getString(R.string.api_error_24);
                case 100: return context.getString(R.string.api_error_100);
                case 101: return context.getString(R.string.api_error_101);
                case 103: return context.getString(R.string.api_error_103);
                case 105: return context.getString(R.string.api_error_105);
                case 113: return context.getString(R.string.api_error_113);
                case 114: return context.getString(R.string.api_error_114);
                case 118: return context.getString(R.string.api_error_118);
                case 121: return context.getString(R.string.api_error_121);
                case 150: return context.getString(R.string.api_error_150);
                case 174: return context.getString(R.string.api_error_174);
                case 175: return context.getString(R.string.api_error_175);
                case 176: return context.getString(R.string.api_error_176);
                case 200: return context.getString(R.string.api_error_200);
                case 201: return context.getString(R.string.api_error_201);
                case 203: return context.getString(R.string.api_error_203);
                case 210: return context.getString(R.string.api_error_210);
                case 214: return context.getString(R.string.api_error_214);
                case 219: return context.getString(R.string.api_error_219);
                case 220: return context.getString(R.string.api_error_220);
                case 221: return context.getString(R.string.api_error_221);
                case 300: return context.getString(R.string.api_error_300);
                case 500: return context.getString(R.string.api_error_500);
                case 600: return context.getString(R.string.api_error_600);
                case 603: return context.getString(R.string.api_error_603);
                case 701: return context.getString(R.string.api_error_701);
                case 800: return context.getString(R.string.api_error_800);
                case 1150: return context.getString(R.string.api_error_1150);
                case 1151: return context.getString(R.string.api_error_1151);
            }

            return super.getMessage(context, code, ifUnknown, params);
        }
    }

    public static class AppLocalizer extends BaseLocazer {

    }

    public static Localizer api(){
        return sApiLocalizer;
    }

    public static Localizer app(){
        return sAppLocalizer;
    }
}

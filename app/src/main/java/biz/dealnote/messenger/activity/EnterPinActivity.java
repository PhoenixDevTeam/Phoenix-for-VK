package biz.dealnote.messenger.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.fragment.EnterPinFragment;
import biz.dealnote.messenger.util.Utils;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class EnterPinActivity extends NoMainActivity {

    public static Class getClass(Context context){
        return Utils.is600dp(context) ? EnterPinActivity.class : EnterPinActivityPortraitOnly.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, EnterPinFragment.newInstance())
                    .commit();
        }
    }
}

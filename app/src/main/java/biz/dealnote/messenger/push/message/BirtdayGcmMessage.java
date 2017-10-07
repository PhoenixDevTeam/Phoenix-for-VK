package biz.dealnote.messenger.push.message;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;

public class BirtdayGcmMessage {

    //Bundle[{type=birthday, uids=20924995, _genSrv=605120, sandbox=0, collapse_key=birthday}]

    //key: google.sent_time, value: 1481879102336, class: class java.lang.Long
    //key: type, value: birthday, class: class java.lang.String
    //key: uids, value: 61354506,8056682, class: class java.lang.String
    //key: google.message_id, value: 0:1481879102338566%8c76e97a3fbd627d, class: class java.lang.String
    //key: no_sound, value: 1, class: class java.lang.String
    //key: _genSrv, value: 605119, class: class java.lang.String
    //key: sandbox, value: 0, class: class java.lang.String
    //key: collapse_key, value: birthday, class: class java.lang.String

    public ArrayList<Integer> uids;
    public String _genSrv;
    public String type;

    public static BirtdayGcmMessage fromBundle(@NonNull Bundle bundle) {
        BirtdayGcmMessage message = new BirtdayGcmMessage();

        String uidsString = bundle.getString("uids");
        String[] uidsStringArray = TextUtils.isEmpty(uidsString) ? null : uidsString.split(",");

        if(uidsStringArray != null){
            message.uids = new ArrayList<>(uidsStringArray.length);
            for (String anUidsStringArray : uidsStringArray) {
                try {
                    message.uids.add(Integer.parseInt(anUidsStringArray));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        message._genSrv = bundle.getString("_genSrv");
        message.type = bundle.getString("type");
        return message;
    }
}

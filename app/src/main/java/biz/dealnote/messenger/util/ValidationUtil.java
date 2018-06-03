package biz.dealnote.messenger.util;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static biz.dealnote.messenger.util.Utils.trimmedIsEmpty;

public class ValidationUtil {

    public static boolean isValidURL(String url) {
        URL u;

        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }

        try {
            u.toURI();
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }

    public static boolean isValidIpAddress(String ipv4) {
        if(trimmedIsEmpty(ipv4)){
            return false;
        }

        ipv4 = ipv4.trim();

        String[] blocks = ipv4.split("\\.");

        if(blocks.length != 4){
            return false;
        }

        for (String block : blocks) {
            try {
                int num = Integer.parseInt(block);

                if (num > 255 || num < 0) {
                    return false;
                }
            } catch (Exception e){
                return false;
            }
        }

        return true;
    }
}
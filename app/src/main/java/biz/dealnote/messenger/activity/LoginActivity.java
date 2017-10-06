package biz.dealnote.messenger.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Auth;
import biz.dealnote.messenger.api.util.VKStringUtils;
import biz.dealnote.messenger.model.Token;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebView webview = (WebView) findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        //otherwise CookieManager will fall with java.lang.IllegalStateException: CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()
        CookieSyncManager.createInstance(this);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        String clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
        String scope = getIntent().getStringExtra(EXTRA_SCOPE);
        String groupIds = getIntent().getStringExtra(EXTRA_GROUP_IDS);

        String url = Auth.getUrl(clientId, scope, groupIds);
        webview.loadUrl(url);
    }

    private static final String EXTRA_CLIENT_ID = "client_id";
    private static final String EXTRA_SCOPE = "scope";
    private static final String EXTRA_GROUP_IDS = "group_ids";

    public static Intent createIntent(Context context, String clientId, String scope){
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope);
    }

    public static Intent createIntent(Context context, String clientId, String scope, Collection<Integer> groupIds){
        String ids = Utils.join(groupIds, ",", Object::toString);
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope)
                .putExtra(EXTRA_GROUP_IDS, ids);
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            parseUrl(url);
        }
    }

    private static String tryExtractAccessToken(String url){
        return VKStringUtils.extractPattern(url, "access_token=(.*?)&");
    }

    private static ArrayList<Token> tryExtractAccessTokens(String url) throws Exception {
        Pattern p = Pattern.compile("access_token_(\\d*)=(.*?)&");

        ArrayList<Token> tokens = new ArrayList<>();

        Matcher matcher = p.matcher(url);
        while (matcher.find()){
            String groupid = matcher.group(1);
            String token = matcher.group(2);

            if(nonEmpty(groupid) && nonEmpty(token)){
                tokens.add(new Token(-Integer.parseInt(groupid), token));
            }
        }

        if(tokens.isEmpty()){
            throw new Exception("Failed to parse redirect url " + url);
        }

        return tokens;
    }

    private static String tryExtractUserId(String url){
        return VKStringUtils.extractPattern(url, "user_id=(\\d*)");
    }

    private void parseUrl(String url) {
        try {
            if (url == null) {
                return;
            }

            Logger.d(TAG, "url=" + url);

            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    Intent intent = new Intent();

                    try {
                        ArrayList<Token> tokens = tryExtractAccessTokens(url);
                        intent.putParcelableArrayListExtra("group_tokens", tokens);
                    } catch (Exception e){
                        String accessToken = tryExtractAccessToken(url);
                        String userId = tryExtractUserId(url);

                        intent.putExtra(Extra.TOKEN, accessToken);
                        intent.putExtra(Extra.USER_ID, Integer.parseInt(userId));
                    }

                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Token> extractGroupTokens(Intent data){
        return data.getParcelableArrayListExtra("group_tokens");
    }
}
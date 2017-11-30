package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.fragment.base.AccountDependencyFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.link.VkLinkParser;
import biz.dealnote.messenger.link.types.AbsLink;
import biz.dealnote.messenger.link.types.AwayLink;
import biz.dealnote.messenger.link.types.PageLink;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.util.Logger;

public class BrowserFragment extends AccountDependencyFragment implements BackPressCallback {

    public static final String TAG = BrowserFragment.class.getSimpleName();

    public static Bundle buildArgs(int accountId, @NonNull String url){
        Bundle args = new Bundle();
        args.putString(Extra.URL, url);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static BrowserFragment newInstance(Bundle args){
        BrowserFragment fragment = new BrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }
    }

    protected WebView mWebView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, container, false);
        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) root.findViewById(R.id.toolbar));
        mWebView = (WebView) root.findViewById(R.id.webview);

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setWebViewClient(new VkLinkSupportWebClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                BrowserFragment.this.title = title;
                refreshActionBar();
            }
        });

        //mWebView.getSettings().setJavaScriptEnabled(true); // из-за этого не срабатывал метод
        // shouldOverrideUrlLoading в WebClient

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        } else if (webState != null) {
            mWebView.restoreState(webState);
            webState = null;
        } else {
            loadAtFirst();
        }

        return root;
    }

    protected void loadAtFirst() {
        String url = getArguments().getString(Extra.URL);
        Logger.d(TAG, "url: " + url);
        mWebView.loadUrl(url);
    }

    private void refreshActionBar() {
        if (!isAdded()) {
            return;
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.browser);
            actionBar.setSubtitle(title);
        }
    }

    private String title;

    @Override
    public void onResume() {
        super.onResume();
        refreshActionBar();

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(true)
                .build()
                .apply(getActivity());
    }

    private Bundle webState;

    @Override
    public void onPause() {
        super.onPause();
        webState = new Bundle();
        mWebView.saveState(webState);
    }

    private static final String SAVE_TITLE = "save_title";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_TITLE, title);
        mWebView.saveState(outState);
    }

    private void restoreFromInstanceState(@NonNull Bundle bundle) {
        if (mWebView != null) {
            mWebView.restoreState(bundle);
        }

        title = bundle.getString(SAVE_TITLE);
        Logger.d(TAG, "restoreFromInstanceState, bundle: " + bundle);
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        }

        return true;
    }

    private class VkLinkSupportWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AbsLink link = VkLinkParser.parse(url);
            Logger.d(TAG, "shouldOverrideUrlLoading, link: " + link + ", url: " + url);

            //link: null, url: https://vk.com/doc124456557_415878705

            if (link == null || link instanceof PageLink) {
                view.loadUrl(url);
                return true;
            }

            if (link instanceof AwayLink) {
                LinkHelper.openLinkInBrowser(getActivity(), ((AwayLink) link).link);
                return true;
            }

            if (LinkHelper.openVKLink(getActivity(), getAccountId(), link)) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            title = view.getTitle();
            refreshActionBar();
        }
    }
}

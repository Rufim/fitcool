package ru.webclient.fitcool.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.iid.FirebaseInstanceId;

import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.PreferenceMaster;
import ru.webclient.fitcool.MainActivity;
import ru.webclient.fitcool.service.FitcoolFirebaseInstanceIDService;

/**
 * Created by Admin on 22.01.2018.
 */

public class WebViewFragment extends BaseFragment {

    public final static String URL_ARG = "url";

    private WebView webView;
    private boolean mIsWebViewAvailable;
    private boolean mRotated = false;

    private static WebViewFragment instance;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        if (webView == null) {
            webView = new WebView(getActivity());
        }
        if (!rotated()) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if(url == null || url.startsWith("/") || url.startsWith("#") || url.contains("fitcool") || url.contains("youtube")) {
                        return false;
                    }
                    return true;
                }
            });
            PreferenceMaster master = new PreferenceMaster(getContext());
            if(!master.getValue(FitcoolFirebaseInstanceIDService.IS_SEND, false) &&  FirebaseInstanceId.getInstance().getToken() != null) {
                FitcoolFirebaseInstanceIDService.sendRegistrationToServer(getContext(), FirebaseInstanceId.getInstance().getToken());
            }
            webView.loadUrl(getUrl());
        }
        mIsWebViewAvailable = true;
        instance = this;
        return webView;
    }

    public static WebViewFragment getInstance() {
        return instance;
    }

    public String getUrl(){
        return getArguments().getString(URL_ARG);
    }


    @Override
    public boolean allowBackPress() {
        if (webView.copyBackForwardList().getCurrentIndex() > 0) {
            webView.goBack();
            return false;
        }
        return true;
    }

    /**
     * let us know if the webView has been rotated.
     *
     * @return
     */
    public boolean rotated() {
        return mRotated;
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        super.onPause();

        if (honeyOrHigher())
            webView.onPause();

        mRotated = true;
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {

        if (honeyOrHigher())
            webView.onResume();

        super.onResume();
    }

    private boolean honeyOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;

        if (webView != null) {
            ViewGroup parentViewGroup = (ViewGroup) webView.getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(webView);
            }
        }

        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the WebView.
     */
    public WebView getWebView() {
        return mIsWebViewAvailable ? webView : null;
    }
}

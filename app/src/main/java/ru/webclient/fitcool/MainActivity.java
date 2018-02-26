package ru.webclient.fitcool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.TextUtils;
import ru.webclient.fitcool.fragment.WebViewFragment;
import ru.webclient.fitcool.service.FitcoolFirebaseMessagingService;

/**
 * Created by Admin on 28.03.2017.
 */
public class MainActivity extends BaseActivity {

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        toolbarClassic = true;
        super.onCreate(savedInstanceState);
        hideActionBar();
        lockDrawerClosed();
    }

    protected void handleIntent(Intent intent) {
        String url;
        if(intent == null || TextUtils.isEmpty(intent.getStringExtra(WebViewFragment.URL_ARG))) {
            url = "https://fitcool.org/";
        } else {
            url = intent.getStringExtra(WebViewFragment.URL_ARG);
        }
        Fragment fr = getCurrentFragment();
        if(fr == null || !(fr instanceof WebViewFragment)) {
            FragmentBuilder builder = new FragmentBuilder(getSupportFragmentManager());
            builder.putArg(WebViewFragment.URL_ARG, url);
            replaceFragment(WebViewFragment.class, builder);
        } else {
            ((WebViewFragment) fr).getWebView().loadUrl(url);
        }

        Map<String, String> test = new HashMap<>();
        test.put("url", url);
        test.put("title", "HW");
        test.put("message", "Hello world!");
        FitcoolFirebaseMessagingService.sendNotification(this, test);
    }

    @Override
    public void onBackPressed() {
        Fragment fr = this.getCurrentFragment();
        if(fr instanceof BaseActivity.BackCallback) {
            if(((BaseActivity.BackCallback)fr).allowBackPress()) {
                if (getSupportFragmentManager().getBackStackEntryCount() <= 0) {
                    if (doubleBackToExitPressedOnce) {
                        super.onBackPressed();
                        return;
                    }
                    this.doubleBackToExitPressedOnce = true;
                    GuiUtils.showSnackbar(this.container, R.string.back_to_exit, getResources().getColor(R.color.white),  GuiUtils.getThemeColor(this, R.attr.colorPrimary));
                    new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                } else {
                    super.onBackPressed();
                }
                System.gc();
            }
        } else {
            super.onBackPressed();
            System.gc();
        }

    }


}

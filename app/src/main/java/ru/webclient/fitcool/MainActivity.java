package ru.webclient.fitcool;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebView;

import ru.kazantsev.template.activity.BaseActivity;
import ru.kazantsev.template.util.FragmentBuilder;
import ru.kazantsev.template.util.TextUtils;
import ru.webclient.fitcool.fragment.WebViewFragment;

/**
 * Created by Admin on 28.03.2017.
 */
public class MainActivity extends BaseActivity {

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        toolbarClassic = true;
        super.onCreate(savedInstanceState);
        final View decorView = getWindow().getDecorView();
        // Hide both the navigation bar and the status bar.
        // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
        // a general rule, you should design your app to hide the status bar whenever you
        // hide the navigation bar.
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |   View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener
                (visibility -> {
                    // Note that system bars will only be "visible" if none of the
                    // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        // TODO: The system bars are visible. Make any desired
                        decorView.postDelayed(() -> decorView.setSystemUiVisibility(uiOptions), 4000);
                    } else {
                        // TODO: The system bars are NOT visible. Make any desired

                    }
                });
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
                    showSnackbar(R.string.back_to_exit);
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

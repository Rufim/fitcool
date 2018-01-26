package ru.webclient.fitcool;

import android.content.Intent;
import android.os.Bundle;
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
        FragmentBuilder builder = new FragmentBuilder(getSupportFragmentManager());
        if(intent == null || TextUtils.isEmpty(intent.getStringExtra(WebViewFragment.URL_ARG))) {
            builder.putArg(WebViewFragment.URL_ARG, "https://fitcool.org/");
        } else {
            builder.putArg(WebViewFragment.URL_ARG, intent.getStringExtra(WebViewFragment.URL_ARG));
        }
        replaceFragment(WebViewFragment.class, builder);
    }



}
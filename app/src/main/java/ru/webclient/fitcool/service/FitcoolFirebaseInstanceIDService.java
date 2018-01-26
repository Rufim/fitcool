package ru.webclient.fitcool.service;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import net.vrallev.android.cat.Cat;

import ru.kazantsev.template.util.AndroidSystemUtils;
import ru.kazantsev.template.util.GuiUtils;
import ru.kazantsev.template.util.PreferenceMaster;
import ru.kazantsev.template.util.SystemUtils;
import ru.webclient.fitcool.MainActivity;
import ru.webclient.fitcool.fragment.WebViewFragment;

/**
 * Created by Admin on 22.05.2017.
 */
public class FitcoolFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static final String IS_SEND = "isSend";
    public static final String LAST_TOKEN = "lastToken";
    private static final String JS_NAME = "droid";
    private static final String TAG = "MyFirebaseIIDService";
    private static String appToken = null;
    private static Thread update;


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Cat.d("Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(getApplicationContext(), refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(Context context, String token) {
        appToken = token;
        if(update == null) {
            update = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (WebViewFragment.getInstance()  == null || WebViewFragment.getInstance().getWebView() == null) {
                        SystemUtils.sleepQuietly(100);
                        if(update == null || Thread.currentThread().isInterrupted()) return;
                    }
                    final PreferenceMaster master = new PreferenceMaster(context);
                    boolean isSend = master.getValue(IS_SEND, false);
                    String oldToken  = master.getValue(LAST_TOKEN);
                    if(!isSend || oldToken == null || !oldToken.equals(appToken)) {
                        GuiUtils.runInUI(context, new GuiUtils.RunUIThread() {
                            @Override
                            public void run(Object... objects) {
                                WebView webView = WebViewFragment.getInstance().getWebView();
                                String token = appToken;
                                webView.addJavascriptInterface(new TokenObject(token), JS_NAME);
                                update = null;
                                master.putValue(IS_SEND, true);
                                master.putValue(LAST_TOKEN, token);
                            }
                        });
                    }
                }
            });
            update.start();
        }
    }

    public static class TokenObject {

        final String token;

        TokenObject(String token) {
            this.token = token;
        }

        @JavascriptInterface
        public String getID() { return token; }
    }
}

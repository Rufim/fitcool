package ru.webclient.fitcool.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.iid.FirebaseInstanceId;

import net.vrallev.android.cat.Cat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.kazantsev.template.fragments.BaseFragment;
import ru.kazantsev.template.util.PreferenceMaster;
import ru.kazantsev.template.util.TextUtils;
import ru.webclient.fitcool.service.FitcoolFirebaseInstanceIDService;

/**
 * Created by Admin on 22.01.2018.
 */

public class WebViewFragment extends BaseFragment {

    public final static String URL_ARG = "url";

    private WebView webView;
    private boolean mIsWebViewAvailable;
    private boolean mRotated = false;
    private ValueCallback<Uri[]> filePathCallback;
    private ValueCallback<Uri> uploadMessage;
    private String cameraPhotoPath;
    private static WebViewFragment instance;
    public static final int REQUEST_CODE_LOLIPOP = 1;
    private final static int RESULT_CODE_ICE_CREAM = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        if (webView == null) {
            CookieSyncManager.createInstance(getContext());
            CookieSyncManager.getInstance().startSync();
            webView = new WebView(getActivity());
        }
        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore the previous URL and history stack
            webView.restoreState(savedInstanceState);
        }
        if (!rotated()) {
            CookieManager cookieManager = CookieManager.getInstance();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(webView,true);
            } else {
                cookieManager.setAcceptCookie(true);
            }
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url == null || url.startsWith("/") || url.startsWith("#") || url.contains("fitcool") || url.contains("youtube")) {
                        return false;
                    }
                    return true;
                }
            });
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    //Required functionality here
                    return super.onJsAlert(view, url, message, result);
                }

                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                }

                // For Android 3.0+
                public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                    uploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("*/*");
                    startActivityForResult(Intent.createChooser(i, "File Browser"),
                            RESULT_CODE_ICE_CREAM);
                }

                //For Android 4.1
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                    uploadMessage = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    startActivityForResult(Intent.createChooser(i, "File Chooser"),
                            RESULT_CODE_ICE_CREAM);

                }

                //For Android5.0+
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        FileChooserParams fileChooserParams) {
                    if (WebViewFragment.this.filePathCallback != null) {
                        WebViewFragment.this.filePathCallback.onReceiveValue(null);
                    }
                    WebViewFragment.this.filePathCallback = filePathCallback;

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", cameraPhotoPath);
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                            Cat.e("Unable to create Image File", ex);
                        }

                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            cameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }

                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("image/*");

                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                    startActivityForResult(chooserIntent, REQUEST_CODE_LOLIPOP);

                    return true;
                }
            });
            PreferenceMaster master = new PreferenceMaster(getContext());
            if(TextUtils.notEmpty(FirebaseInstanceId.getInstance().getToken())) {
                String token = FirebaseInstanceId.getInstance().getToken();
                webView.addJavascriptInterface(new FitcoolFirebaseInstanceIDService.MyJavaInterface(token), FitcoolFirebaseInstanceIDService.JS_NAME);
                master.putValue(FitcoolFirebaseInstanceIDService.IS_SEND, true);
                master.putValue(FitcoolFirebaseInstanceIDService.LAST_TOKEN, token);
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

    public String getUrl() {
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

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_CODE_ICE_CREAM:
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                }
                uploadMessage.onReceiveValue(uri);
                uploadMessage = null;
                break;
            case REQUEST_CODE_LOLIPOP:
                Uri[] results = null;
                // Check that the response is a good one
                if (resultCode == Activity.RESULT_OK) {
                    if (data == null) {
                        // If there is not data, then we may have taken a photo
                        if (cameraPhotoPath != null) {
                            results = new Uri[]{Uri.parse(cameraPhotoPath)};
                        }
                    } else {
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                        }
                    }
                }

                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
                break;
        }
    }
}

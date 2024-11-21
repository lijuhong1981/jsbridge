package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class WebViewBridgeManager {
    public static final String TAG = WebViewBridgeManager.class.getSimpleName();
    public static final String FILE_LOCAL_HOST = "file.local";

    private final Activity mActivity;
    public final WebView webView;
    public final WebSettings webSettings;
    private final ArrayList<MessageReceiver> mMessageReceivers = new ArrayList<>();
    private MethodHandler mMethodHandler;

    public static String getOrientationString(int orientation) {
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return "PORTRAIT".toLowerCase();
            case Configuration.ORIENTATION_LANDSCAPE:
                return "LANDSCAPE".toLowerCase();
            default:
                return "unknown";
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewBridgeManager(@NonNull Activity activity, @NonNull WebView webView, @Nullable WebSettingsOptions options) {
        mActivity = activity;
        this.webView = webView;
        if (options == null)
            options = new WebSettingsOptions();
        webSettings = this.webView.getSettings();
        webSettings.setAllowContentAccess(options.allowContentAccess);
        webSettings.setAllowFileAccess(options.allowFileAccess);
        webSettings.setBlockNetworkImage(options.blockNetworkImage);
        webSettings.setBlockNetworkLoads(options.blockNetworkLoads);
        webSettings.setBuiltInZoomControls(options.builtInZoomControls);
        webSettings.setCacheMode(options.cacheMode);
        webSettings.setDisplayZoomControls(options.displayZoomControls);
        webSettings.setDomStorageEnabled(options.domStorageEnabled);
        webSettings.setGeolocationEnabled(options.geolocationEnabled);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(options.javaScriptCanOpenWindowsAutomatically);
        webSettings.setLayoutAlgorithm(options.layoutAlgorithm);
        webSettings.setLoadsImagesAutomatically(options.loadsImagesAutomatically);
        webSettings.setLoadWithOverviewMode(options.loadWithOverviewMode);
        webSettings.setMediaPlaybackRequiresUserGesture(options.mediaPlaybackRequiresUserGesture);
        webSettings.setMixedContentMode(options.mixedContentMode);
        webSettings.setNeedInitialFocus(options.needInitialFocus);
        webSettings.setOffscreenPreRaster(options.offscreenPreRaster);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            webSettings.setSafeBrowsingEnabled(options.safeBrowsingEnabled);
        webSettings.setSupportMultipleWindows(options.supportMultipleWindows);
        webSettings.setSupportZoom(options.supportZoom);
        webSettings.setUseWideViewPort(options.useWideViewPort);
        if (!TextUtils.isEmpty(options.defaultTextEncodingName))
            webSettings.setDefaultTextEncodingName(options.defaultTextEncodingName);
        if (options.disabledActionModeMenuItems >= 0)
            webSettings.setDisabledActionModeMenuItems(options.disabledActionModeMenuItems);
        if (!TextUtils.isEmpty(options.cursiveFontFamily))
            webSettings.setCursiveFontFamily(options.cursiveFontFamily);
        if (!TextUtils.isEmpty(options.fantasyFontFamily))
            webSettings.setFantasyFontFamily(options.fantasyFontFamily);
        if (!TextUtils.isEmpty(options.fixedFontFamily))
            webSettings.setFixedFontFamily(options.fixedFontFamily);
        if (!TextUtils.isEmpty(options.sansSerifFontFamily))
            webSettings.setSansSerifFontFamily(options.sansSerifFontFamily);
        if (!TextUtils.isEmpty(options.serifFontFamily))
            webSettings.setSerifFontFamily(options.serifFontFamily);
        if (!TextUtils.isEmpty(options.standardFontFamily))
            webSettings.setStandardFontFamily(options.standardFontFamily);
        if (options.defaultFixedFontSize > 0)
            webSettings.setDefaultFixedFontSize(options.defaultFixedFontSize);
        if (options.defaultFontSize > 0)
            webSettings.setDefaultFontSize(options.defaultFontSize);
        if (options.minimumFontSize > 0)
            webSettings.setMinimumFontSize(options.minimumFontSize);
        if (options.minimumLogicalFontSize > 0)
            webSettings.setMinimumLogicalFontSize(options.minimumLogicalFontSize);
        if (options.textZoom > 0)
            webSettings.setTextZoom(options.textZoom);
        if (!TextUtils.isEmpty(options.userAgentString))
            webSettings.setUserAgentString(options.userAgentString);

        webSettings.setJavaScriptEnabled(true);
        this.webView.addJavascriptInterface(new AndroidInterface(), "jsbridgeInterface");
        this.webView.setWebViewClient(new MyWebViewClient());
        this.webView.setWebChromeClient(new MyWebChromeClient());
        mMethodHandler = new DefaultMethodHandler(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mMethodHandler != null)
            mMethodHandler.onActivityResult(requestCode, resultCode, data);
    }

    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        try {
            Message msg = new Message();
            msg.type = "configurationChanged";
            msg.body = new JSONObject();
            msg.body.put("orientation", getOrientationString(newConfig.orientation));
            msg.body.put("fontScale", newConfig.fontScale);
            msg.body.put("mcc", newConfig.mcc);
            msg.body.put("mnc", newConfig.mnc);
            msg.body.put("screenLayout", newConfig.screenLayout);
            msg.body.put("touchscreen", newConfig.touchscreen);
            msg.body.put("keyboard", newConfig.keyboard);
            msg.body.put("keyboardHidden", newConfig.keyboardHidden);
            msg.body.put("hardKeyboardHidden", newConfig.hardKeyboardHidden);
            msg.body.put("navigation", newConfig.navigation);
            msg.body.put("navigationHidden", newConfig.navigationHidden);
            msg.body.put("uiMode", newConfig.uiMode);
            msg.body.put("screenWidthDp", newConfig.screenWidthDp);
            msg.body.put("screenHeightDp", newConfig.screenHeightDp);
            msg.body.put("smallestScreenWidthDp", newConfig.smallestScreenWidthDp);
            msg.body.put("densityDpi", newConfig.densityDpi);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                msg.body.put("colorMode", newConfig.colorMode);
            }
            postMessage(msg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerMessageReceiver(@NonNull MessageReceiver handler) {
        if (!mMessageReceivers.contains(handler))
            mMessageReceivers.add(handler);
    }

    public void unregisterMessageReceiver(@NonNull MessageReceiver handler) {
        mMessageReceivers.remove(handler);
    }

    private void notifyMessageReceivers(@NonNull Message message) {
        MessageCallbackHandler callbackHandler = new MessageCallbackHandler(this, message.id);
        for (int i = 0, size = mMessageReceivers.size(); i < size; i++) {
            mMessageReceivers.get(i).onMessage(message, callbackHandler);
        }
    }

    public void setMethodHandler(@Nullable MethodHandler handler) {
        if (mMethodHandler != handler)
            mMethodHandler = handler;
    }

    public MethodHandler getMethodHandler() {
        return mMethodHandler;
    }

    private void notifyMethodHandler(@NonNull Message message) {
        try {
            String method = message.body.getString("method");
            JSONObject params = message.body.getJSONObject("params");
            MethodCallbackHandler callbackHandler = new MethodCallbackHandler(this, message.id);
            if (mMethodHandler != null)
                mMethodHandler.onMethod(method, params, callbackHandler);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void postMessage(@NonNull JSONObject jsonObject) {
        mActivity.runOnUiThread(() -> {
            String jsonString = jsonObject.toString();
            Log.d(TAG, "postMessageToWeb: " + jsonString);
            String script = String.format("onBridgeMessage('%s');", jsonString);
            webView.evaluateJavascript(script, null);
        });
    }

    public void postMessage(@NonNull Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (TextUtils.isEmpty(message.id)) {
                UUID uuid = UUID.randomUUID();
                message.id = uuid.toString();
            }
            jsonObject.put("id", message.id);
            jsonObject.put("type", message.type);
            jsonObject.put("body", message.body);
            postMessage(jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    class AndroidInterface {

        @JavascriptInterface
        public void onBridgeMessage(String jsonString) {
            Log.d(TAG, "onReceiveMessageFromWeb: " + jsonString);
            try {
                Message message = Message.fromJson(jsonString);
                if (message.type.equals("callMethod"))
                    notifyMethodHandler(message);
                else
                    notifyMessageReceivers(message);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class MyWebViewClient extends WebViewClient {

//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            return super.shouldOverrideUrlLoading(view, request);
//        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted: " + url);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d(TAG, "onPageFinished: " + url);
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageCommitVisible(WebView view, String url) {
            Log.d(TAG, "onPageCommitVisible: " + url);
            super.onPageCommitVisible(view, url);
        }

//        @Override
//        public void onLoadResource(WebView view, String url) {
//            super.onLoadResource(view, url);
//        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            if (Objects.requireNonNull(uri.getHost()).contains(FILE_LOCAL_HOST)) {
                String filePath = uri.getQueryParameter("file");
                if (filePath == null) {
                    return super.shouldInterceptRequest(view, request);
                }
                try {
                    Log.d(TAG, "shouldInterceptRequest: " + filePath);
                    String extName = filePath.substring(filePath.lastIndexOf(".") + 1);
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extName);
                    FileInputStream input = new FileInputStream(filePath.trim());
                    return new WebResourceResponse(mimeType, "UTF-8", input);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "shouldInterceptRequest error:", e);
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Log.e(TAG, "onReceivedError: " + error.getDescription());
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            Log.e(TAG, "onReceivedHttpError: url=" +  request.getUrl() + "; statusCode=" + errorResponse.getStatusCode() + "; reason=" + errorResponse.getReasonPhrase());
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            Log.e(TAG, "onReceivedSslError: " + error.toString());
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }
    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(TAG, consoleMessage.message() + " -- From line " +
                    consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.i(TAG, "onProgressChanged: " + newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            Log.i(TAG, "onPermissionRequest: " + request.toString());
            request.grant(request.getResources());
        }
    }
}

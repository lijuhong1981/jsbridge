package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Objects;

public class WebViewBridgeManager {
    public static final String TAG = WebViewBridgeManager.class.getSimpleName();
    public static final String FILE_LOCAL_HOST = "file.local";

    private final Activity mActivity;
    private final WebView mWebView;
    private final ArrayList<MessageReceiver> mMessageReceivers = new ArrayList<>();
    private MethodHandler mMethodHandler;

    @SuppressLint("SetJavaScriptEnabled")
    public WebViewBridgeManager(@NonNull Activity activity, @NonNull WebView webView) {
        mActivity = activity;
        mWebView = webView;
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new AndroidInterface(), "jsbridgeInterface");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mMethodHandler = new DefaultMethodHandler(activity);
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (mMethodHandler != null)
            mMethodHandler.onActivityResult(requestCode, resultCode, data);
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

    public void setMethodHandler(@NonNull MethodHandler handler) {
        if (mMethodHandler != handler)
            mMethodHandler = handler;
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
            mWebView.evaluateJavascript(script, null);
        });
    }

    public void postMessage(@NonNull Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
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
    }
}
package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.smtt.export.external.interfaces.PermissionRequest;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class X5WebViewBridgeManager extends BaseWebViewBridgeManager {
    public final WebView webView;
    public final WebSettings webSettings;

    private WebSettings.LayoutAlgorithm convertLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm layoutAlgorithm) {
        switch (layoutAlgorithm) {
            case NARROW_COLUMNS:
                return WebSettings.LayoutAlgorithm.NARROW_COLUMNS;
            case SINGLE_COLUMN:
                return WebSettings.LayoutAlgorithm.SINGLE_COLUMN;
        }
        return WebSettings.LayoutAlgorithm.NORMAL;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public X5WebViewBridgeManager(@NonNull Activity activity, @NonNull WebView webView, @Nullable WebSettingsOptions options) {
        super(activity);
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
        webSettings.setLayoutAlgorithm(convertLayoutAlgorithm(options.layoutAlgorithm));
        webSettings.setLoadsImagesAutomatically(options.loadsImagesAutomatically);
        webSettings.setLoadWithOverviewMode(options.loadWithOverviewMode);
        webSettings.setMediaPlaybackRequiresUserGesture(options.mediaPlaybackRequiresUserGesture);
//        webSettings.setMixedContentMode(options.mixedContentMode);
        webSettings.setNeedInitialFocus(options.needInitialFocus);
//        webSettings.setOffscreenPreRaster(options.offscreenPreRaster);
        webSettings.setSafeBrowsingEnabled(options.safeBrowsingEnabled);
        webSettings.setSupportMultipleWindows(options.supportMultipleWindows);
        webSettings.setSupportZoom(options.supportZoom);
        webSettings.setUseWideViewPort(options.useWideViewPort);
        if (!TextUtils.isEmpty(options.defaultTextEncodingName))
            webSettings.setDefaultTextEncodingName(options.defaultTextEncodingName);
//        if (options.disabledActionModeMenuItems >= 0)
//            webSettings.setDisabledActionModeMenuItems(options.disabledActionModeMenuItems);
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
    }

    @Override
    void evaluateJavascript(@NonNull String scriptString) {
        this.webView.evaluateJavascript(scriptString, null);
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
                    String extName = filePath.substring(filePath.lastIndexOf(".") + 1);
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extName);
                    Log.v(TAG, "shouldInterceptRequest: filePath=" + filePath + "; mimeType=" + mimeType);
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
            Log.e(TAG, "onReceivedHttpError: url=" + request.getUrl() + "; statusCode=" + errorResponse.getStatusCode() + "; reason=" + errorResponse.getReasonPhrase());
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
//        @Override
//        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//            Log.d(TAG, consoleMessage.message() + " -- From line " +
//                    consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
//            return true;
//        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            Log.i(TAG, "onProgressChanged: " + newProgress);
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            Log.i(TAG, "onPermissionRequest: " + request.toString());
            super.onPermissionRequest(request);
            request.grant(request.getResources());
        }
    }
}

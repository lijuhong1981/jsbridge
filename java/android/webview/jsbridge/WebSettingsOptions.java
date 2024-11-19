package android.webview.jsbridge;

import android.webkit.WebSettings;

public class WebSettingsOptions {
    public boolean allowContentAccess = true;
    public boolean allowFileAccess = true;
    public boolean blockNetworkImage = false;
    public boolean blockNetworkLoads = false;
    public boolean builtInZoomControls = false;
    public int cacheMode = WebSettings.LOAD_NO_CACHE;
    public boolean displayZoomControls = false;
    public boolean domStorageEnabled = true;
    public boolean geolocationEnabled = true;
    public boolean javaScriptCanOpenWindowsAutomatically = true;
    public WebSettings.LayoutAlgorithm layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL;
    public boolean loadsImagesAutomatically = true;
    public boolean loadWithOverviewMode = false;
    public boolean mediaPlaybackRequiresUserGesture = false;
    public int mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
    public boolean needInitialFocus = false;
    public boolean offscreenPreRaster = true;
    public boolean safeBrowsingEnabled = false;
    public boolean supportMultipleWindows = false;
    public boolean supportZoom = false;
    public boolean useWideViewPort = true;

    public String defaultTextEncodingName = null;
    public int disabledActionModeMenuItems = -1;
    public String cursiveFontFamily = null;
    public String fantasyFontFamily = null;
    public String fixedFontFamily = null;
    public String sansSerifFontFamily = null;
    public String serifFontFamily = null;
    public String standardFontFamily = null;
    public int defaultFixedFontSize = 0;
    public int defaultFontSize = 0;
    public int minimumFontSize = 0;
    public int minimumLogicalFontSize = 0;
    public int textZoom = 0;
    public String userAgentString = null;
}

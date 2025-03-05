package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.Tools;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GetConfigurationInfoHandler extends NoActivityResultMethodHandler {
    public GetConfigurationInfoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "getConfigurationInfo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            Configuration configuration = activity.getResources().getConfiguration();
            JSONObject resultData = new JSONObject();
            resultData.put("orientation", Tools.getOrientationString(configuration.orientation));
            resultData.put("fontScale", configuration.fontScale);
            resultData.put("mcc", configuration.mcc);
            resultData.put("mnc", configuration.mnc);
            resultData.put("screenLayout", configuration.screenLayout);
            resultData.put("touchscreen", configuration.touchscreen);
            resultData.put("keyboard", configuration.keyboard);
            resultData.put("keyboardHidden", configuration.keyboardHidden);
            resultData.put("hardKeyboardHidden", configuration.hardKeyboardHidden);
            resultData.put("navigation", configuration.navigation);
            resultData.put("navigationHidden", configuration.navigationHidden);
            resultData.put("uiMode", configuration.uiMode);
            resultData.put("screenWidthDp", configuration.screenWidthDp);
            resultData.put("screenHeightDp", configuration.screenHeightDp);
            resultData.put("smallestScreenWidthDp", configuration.smallestScreenWidthDp);
            resultData.put("densityDpi", configuration.densityDpi);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resultData.put("colorMode", configuration.colorMode);
            }
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getConfigurationInfo error:", e);
        }
    }
}

package android.webview.jsbridge.methods;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GetDisplayInfoHandler extends NoActivityResultMethodHandler{
    public GetDisplayInfoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "getDisplayInfo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            JSONObject resultData = new JSONObject();
            resultData.put("widthPixels", displayMetrics.widthPixels);
            resultData.put("heightPixels", displayMetrics.heightPixels);
            resultData.put("density", displayMetrics.density);
            resultData.put("densityDpi", displayMetrics.densityDpi);
            resultData.put("xdpi", displayMetrics.xdpi);
            resultData.put("ydpi", displayMetrics.ydpi);
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getDisplayInfo error:", e);
        }
    }
}

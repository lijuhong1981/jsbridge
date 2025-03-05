package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GetAppInfoHandler extends NoActivityResultMethodHandler {
    public GetAppInfoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "getAppInfo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            String packageName = activity.getPackageName();
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(packageName, 0);
            JSONObject resultData = new JSONObject();
            resultData.put("packageName", packageName);
            resultData.put("versionName", packageInfo.versionName);
            resultData.put("versionCode", packageInfo.versionCode);
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException | PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getAppInfo error:", e);
        }
    }
}

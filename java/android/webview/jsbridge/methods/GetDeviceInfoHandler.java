package android.webview.jsbridge.methods;

import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GetDeviceInfoHandler extends NoActivityResultMethodHandler {
    public GetDeviceInfoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "getDeviceInfo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("BOARD", Build.BOARD);
            resultData.put("BOOTLOADER", Build.BOOTLOADER);
            resultData.put("BRAND", Build.BRAND);
            resultData.put("DEVICE", Build.DEVICE);
            resultData.put("DISPLAY", Build.DISPLAY);
            resultData.put("FINGERPRINT", Build.FINGERPRINT);
            resultData.put("HARDWARE", Build.HARDWARE);
            resultData.put("HOST", Build.HOST);
            resultData.put("ID", Build.ID);
            resultData.put("MANUFACTURER", Build.MANUFACTURER);
            resultData.put("MODEL", Build.MODEL);
            resultData.put("PRODUCT", Build.PRODUCT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultData.put("ODM_SKU", Build.ODM_SKU);
                resultData.put("SKU", Build.SKU);
                resultData.put("SOC_MANUFACTURER", Build.SOC_MANUFACTURER);
                resultData.put("SOC_MODEL", Build.SOC_MODEL);
            }
            resultData.put("USER", Build.USER);
            resultData.put("VERSION", getVersionData());
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getDeviceInfo error:", e);
        }
    }

    private @NonNull JSONObject getVersionData() throws JSONException {
        JSONObject versionData = new JSONObject();
        versionData.put("BASE_OS", Build.VERSION.BASE_OS);
        versionData.put("CODENAME", Build.VERSION.CODENAME);
        versionData.put("INCREMENTAL", Build.VERSION.INCREMENTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            versionData.put("MEDIA_PERFORMANCE_CLASS", Build.VERSION.MEDIA_PERFORMANCE_CLASS);
        }
        versionData.put("PREVIEW_SDK_INT", Build.VERSION.PREVIEW_SDK_INT);
        versionData.put("RELEASE", Build.VERSION.RELEASE);
        versionData.put("SDK_INT", Build.VERSION.SDK_INT);
        versionData.put("SECURITY_PATCH", Build.VERSION.SECURITY_PATCH);
        return versionData;
    }
}

package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SetOrientationHandler extends NoActivityResultMethodHandler {
    public SetOrientationHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "setOrientation";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            try {
                String value = params.getString("value");
                switch (value.toLowerCase()) {
                    case "portrait":
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case "landscape":
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    default:
                        Log.w(WebViewBridgeManager.TAG, "setOrientation unknown orientation value " + value);
                        break;
                }
                callbackHandler.notifySuccessCallback();
            } catch (JSONException e) {
//            throw new RuntimeException(e);
                callbackHandler.notifyErrorCallback(e);
                Log.e(WebViewBridgeManager.TAG, "setOrientation error:", e);
            }
        });
    }
}

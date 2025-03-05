package android.webview.jsbridge.methods;

import android.app.Activity;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowToastHandler extends NoActivityResultMethodHandler {
    public ShowToastHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "showToast";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            Toast.makeText(activity, params.getString("text"), Toast.LENGTH_SHORT).show();
            callbackHandler.notifySuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "showToast error:", e);
        }
    }
}

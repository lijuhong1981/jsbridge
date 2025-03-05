package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class DialPhoneHandler extends NoActivityResultMethodHandler {
    public DialPhoneHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "dialPhone";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            final String phoneNumber = params.getString("phoneNumber");
            activity.runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
                callbackHandler.notifySuccessCallback();
            });
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "dialPhone error:", e);
        }
    }
}

package android.webview.jsbridge.methods;

import android.app.Activity;
import android.os.Vibrator;
import android.webview.jsbridge.MethodCallbackHandler;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class CancelVibratorHandler extends NoActivityResultMethodHandler {
    public CancelVibratorHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "cancelVibrator";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            Vibrator vibrator = activity.getSystemService(Vibrator.class);
            vibrator.cancel();
            callbackHandler.notifySuccessCallback();
        });
    }
}

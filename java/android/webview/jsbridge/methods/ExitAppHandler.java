package android.webview.jsbridge.methods;

import android.app.Activity;
import android.webview.jsbridge.MethodCallbackHandler;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ExitAppHandler extends NoActivityResultMethodHandler {
    public ExitAppHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "exitApp";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        boolean force = false;
        try {
            force = params.getBoolean("force");
        } catch (JSONException ignored) {
        }
        if (force)
            System.exit(0);
        else
            activity.finishAffinity();
    }
}

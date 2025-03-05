package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Deprecated, use SetMuteHandler instead.
 * */
@Deprecated
public class SetSpeakerOnHandler extends NoActivityResultMethodHandler {
    public SetSpeakerOnHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "setSpeakerOn";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            boolean value = params.getBoolean("value");
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(value);
            callbackHandler.notifySuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "setMicrophoneOn error:", e);
        }
    }
}

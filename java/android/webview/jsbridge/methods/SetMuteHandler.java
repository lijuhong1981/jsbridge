package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.UnsupportedSchemeException;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class SetMuteHandler extends NoActivityResultMethodHandler {
    public SetMuteHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "setMute";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            String type = params.getString("type");
            boolean mute = params.getBoolean("mute");
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            switch (type) {
                case "microphone":
                    audioManager.setMicrophoneMute(mute);
                    break;
                case "speaker":
                    audioManager.setSpeakerphoneOn(!mute);
                    break;
                default:
                    throw new UnsupportedSchemeException("The type " + type + " is unsupported.");
            }
            callbackHandler.notifySuccessCallback();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "muteDevice error:", e);
        }
    }
}

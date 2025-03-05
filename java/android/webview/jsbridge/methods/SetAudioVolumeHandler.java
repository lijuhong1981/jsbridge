package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.UnsupportedSchemeException;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class SetAudioVolumeHandler extends NoActivityResultMethodHandler {
    public SetAudioVolumeHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "setAudioVolume";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            String type = params.getString("type");
            int volume = params.getInt("volume");
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            int streamType;
            switch (type) {
                case "voice":
                    streamType = AudioManager.STREAM_VOICE_CALL;
                    break;
                case "system":
                    streamType = AudioManager.STREAM_SYSTEM;
                    break;
                case "ring":
                    streamType = AudioManager.STREAM_RING;
                    break;
                case "music":
                    streamType = AudioManager.STREAM_MUSIC;
                    break;
                case "alarm":
                    streamType = AudioManager.STREAM_ALARM;
                    break;
                case "notification":
                    streamType = AudioManager.STREAM_NOTIFICATION;
                    break;
                default:
                    throw new UnsupportedSchemeException("The type " + type + " is unsupported.");
            }
            audioManager.setStreamVolume(streamType, volume, 0);
            callbackHandler.notifySuccessCallback();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "setAudioVolume error:", e);
        }
    }
}

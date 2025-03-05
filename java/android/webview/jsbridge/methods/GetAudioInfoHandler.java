package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class GetAudioInfoHandler extends NoActivityResultMethodHandler {
    public GetAudioInfoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "getAudioInfo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            JSONObject resultData = new JSONObject();
            resultData.put("microphoneOn", !audioManager.isMicrophoneMute());
            resultData.put("speakerOn", audioManager.isSpeakerphoneOn());
            resultData.put("mediaVolume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                resultData.put("minMediaVolume", audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC));
                resultData.put("maxMediaVolume", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            }
            resultData.put("voiceVolume", audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                resultData.put("minVoiceVolume", audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL));
                resultData.put("maxVoiceVolume", audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
            }
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.notifyErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getAudioInfo error:", e);
        }
    }
}

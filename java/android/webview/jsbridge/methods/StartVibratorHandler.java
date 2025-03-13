package android.webview.jsbridge.methods;

import android.app.Activity;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.webview.jsbridge.MethodCallbackHandler;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class StartVibratorHandler extends NoActivityResultMethodHandler {
    public StartVibratorHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "startVibrator";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            Vibrator vibrator = activity.getSystemService(Vibrator.class);
            if (!vibrator.hasVibrator()) {
                callbackHandler.notifyErrorCallback(new UnsupportedOperationException("This device unsupported vibrator."));
                return;
            }
            long milliseconds = 1000L;
            try {
                milliseconds = params.getLong("milliseconds");
            } catch (JSONException ignored) {
            }
            int amplitude = 0;
            try {
                amplitude = params.getInt("amplitude");
            } catch (JSONException ignored) {
            }
            long[] timings = null;
            try {
                JSONArray array = params.getJSONArray("timings");
                int length = array.length();
                timings = new long[length];
                for (int i = 0; i < length; i++) {
                    timings[i] = array.getLong(i);
                }
            } catch (JSONException ignored) {
            }
            int[] amplitudes = null;
            try {
                JSONArray array = params.getJSONArray("amplitudes");
                int length = array.length();
                amplitudes = new int[length];
                for (int i = 0; i < length; i++) {
                    amplitudes[i] = array.getInt(i);
                }
            } catch (JSONException ignored) {
            }
            int repeat = -1;
            try {
                repeat = params.getInt("repeat");
            } catch (JSONException ignored) {
            }
            if (timings != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (amplitudes == null) {
                        amplitudes = new int[timings.length];
                        Arrays.fill(amplitudes, amplitude > 0 ? amplitude : VibrationEffect.DEFAULT_AMPLITUDE);
                    }
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, repeat));
                } else
                    vibrator.vibrate(timings, repeat);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, amplitude > 0 ? amplitude : VibrationEffect.DEFAULT_AMPLITUDE));
                else
                    vibrator.vibrate(milliseconds);
            }
            callbackHandler.notifySuccessCallback();
        });
    }
}

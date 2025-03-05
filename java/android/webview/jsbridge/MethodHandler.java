package android.webview.jsbridge;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.List;

public interface MethodHandler {
    String getMethod();

    boolean hasActivityResult();

    List<Integer> getRequestCodes();

    void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler);

    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
}

package android.webview.jsbridge;

import android.content.Intent;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public interface MethodHandler {
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler);

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);
}

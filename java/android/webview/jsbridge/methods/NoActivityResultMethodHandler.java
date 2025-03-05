package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Intent;
import android.webview.jsbridge.MethodHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class NoActivityResultMethodHandler implements MethodHandler {
    public final Activity activity;

    public NoActivityResultMethodHandler(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean hasActivityResult() {
        return false;
    }

    @Override
    public List<Integer> getRequestCodes() {
        return Collections.emptyList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }
}

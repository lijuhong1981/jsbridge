package android.webview.jsbridge.methods;

import android.app.Activity;
import android.webview.jsbridge.MethodHandler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ActivityResultMethodHandler implements MethodHandler {
    public final Activity activity;
    public final List<Integer> requestCodes = new ArrayList<>();

    public ActivityResultMethodHandler(@NonNull Activity activity) {
        this.activity = activity;
        registerRequestCodes();
    }

    abstract void registerRequestCodes();

    @Override
    public boolean hasActivityResult() {
        return true;
    }

    @Override
    public List<Integer> getRequestCodes() {
        return requestCodes;
    }
}

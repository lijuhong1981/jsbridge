package android.webview.jsbridge;

import android.content.Intent;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface WebViewBridgeManagerInterface {
    void onPause();

    void onResume();

    void onStop();

    void onDestroy();

    void onWindowFocusChanged(boolean hasFocus);

    void onConfigurationChanged(@NonNull Configuration newConfig);

    void onScreenOn();

    void onScreenOff();

    void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    void registerMessageReceiver(@NonNull MessageReceiver receiver);

    void unregisterMessageReceiver(@NonNull MessageReceiver receiver);

    void notifyMessageReceivers(@NonNull Message message);

    void registerMethodHander(MethodHandler handler);

    void unregisterMethodHandler(MethodHandler handler);

    void notifyMethodHandler(@NonNull Message message);

    void postMessage(@NonNull String jsonString);

    void postMessage(@NonNull Message message);

    void postLogMessage(int priority, @Nullable String tag, @NonNull String msg, @Nullable Throwable tr);

    void postLogMessage(int priority, @Nullable String tag, @NonNull String msg);

    void postLogMessage(@Nullable String tag, @NonNull String msg, @Nullable Throwable tr);

    void postLogMessage(@Nullable String tag, @NonNull String msg);

    void postErrorMessage(@Nullable String tag, @NonNull String msg, @Nullable Throwable tr);
}

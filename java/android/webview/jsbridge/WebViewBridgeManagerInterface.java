package android.webview.jsbridge;

import android.content.Intent;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface WebViewBridgeManagerInterface {
    public void onPause();

    public void onResume();

    public void onStop();

    public void onDestroy();

    public void onWindowFocusChanged(boolean hasFocus);

    public void onConfigurationChanged(@NonNull Configuration newConfig);

    public void onScreenOn();

    public void onScreenOff();

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    public void registerMessageReceiver(@NonNull MessageReceiver receiver);

    public void unregisterMessageReceiver(@NonNull MessageReceiver receiver);

    void notifyMessageReceivers(@NonNull Message message);

    public void registerMethodHander(MethodHandler handler);

    public void unregisterMethodHandler(MethodHandler handler);

    void notifyMethodHandler(@NonNull Message message);

    public void postMessage(@NonNull String jsonString);

    public void postMessage(@NonNull Message message);
}

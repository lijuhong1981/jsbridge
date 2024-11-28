package android.webview.jsbridge;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.Optional;

public class MessageCallbackHandler {
    private final WebViewBridgeManager mManager;
    private final String mCallbackId;

    public MessageCallbackHandler(@NonNull WebViewBridgeManager manager, @NonNull String callbackMsgId) {
        mManager = manager;
        mCallbackId = callbackMsgId;
    }

    protected String getCallbackMsgType() {
        return "messageCallback";
    }

    public void notifyCallback(@NonNull JSONObject responseBody, boolean persistCallback) {
        Message message = new Message();
        message.id = mCallbackId;
        message.type = getCallbackMsgType();
        message.body = responseBody;
        message.persistCallback = persistCallback;
        mManager.postMessage(message);
    }

    public void notifyCallback(@NonNull JSONObject responseBody) {
        notifyCallback(responseBody, false);
    }
}

package android.webview.jsbridge;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class MessageCallbackHandler {
    private final WebViewBridgeManagerInterface mManager;
    private final String mCallbackId;

    public MessageCallbackHandler(@NonNull WebViewBridgeManagerInterface manager, @NonNull String callbackMsgId) {
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

package android.webview.jsbridge;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public class MessageCallbackHandler {
    private final JsBridgeManager mManager;
    private final String mCallbackId;
    private boolean mCallbacked = false;

    public MessageCallbackHandler(@NonNull JsBridgeManager manager, @NonNull String callbackMsgId) {
        mManager = manager;
        mCallbackId = callbackMsgId;
    }

    protected String getCallbackMsgType() {
        return "messageCallback";
    }

    public void doCallback(@NonNull JSONObject responseBody) {
        if (mCallbacked) {
            Log.w(JsBridgeManager.TAG, "The callback has executed.");
            return;
        }
        Message message = new Message();
        message.id = mCallbackId;
        message.type = getCallbackMsgType();
        message.body = responseBody;
        mManager.postMessage(message);
        mCallbacked = true;
    }
}

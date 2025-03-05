package android.webview.jsbridge;

public interface MessageReceiver {
    void onMessage(Message message, MessageCallbackHandler callbackHandler);
}

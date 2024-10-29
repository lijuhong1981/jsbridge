package android.webview.jsbridge;

public interface MessageReceiver {
    public void onMessage(Message message, MessageCallbackHandler callbackHandler);
}

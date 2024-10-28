package com.webview.jsbridge;

/**
 * 消息处理句柄
 * */
public interface MessageHandler {
    public void onMessage(Message message, MessageCallbackHandler callbackHandler);
}

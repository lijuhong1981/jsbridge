package com.webview.jsbridge;

import androidx.annotation.NonNull;

public class MethodCallbackHandler extends MessageCallbackHandler{

    public MethodCallbackHandler(@NonNull JsBridgeManager manager, @NonNull String callbackMsgId) {
        super(manager, callbackMsgId);
    }

    @Override
    protected String getCallbackMsgType() {
        return "methodCallback";
    }
}

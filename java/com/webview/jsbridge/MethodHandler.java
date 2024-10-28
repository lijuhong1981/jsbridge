package com.webview.jsbridge;

import org.json.JSONObject;

public interface MethodHandler {
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler);
}

package com.webview.jsbridge;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsBridgeManager {
    public static final String TAG = JsBridgeManager.class.getSimpleName();

    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                String jsonString = (String) msg.obj;
                String script = String.format("onBridgeMessage('%s');", jsonString);
                mWebView.evaluateJavascript(script, null);
            }
        }
    };
    private final WebView mWebView;
    private final ArrayList<MessageHandler> mMessageHandlers = new ArrayList<>();
    private final ArrayList<MethodHandler> mMethodHandlers = new ArrayList<>();

    @SuppressLint("SetJavaScriptEnabled")
    public JsBridgeManager(@NonNull WebView webView) {
        mWebView = webView;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new AndroidInterface(), "jsbridgeInterface");
    }

    public void registerMessageReceiver(@NonNull MessageHandler handler) {
        if (!mMessageHandlers.contains(handler))
            mMessageHandlers.add(handler);
    }

    public void unregisterMessageReceiver(@NonNull MessageHandler handler) {
        mMessageHandlers.remove(handler);
    }

    private void notifyMessageHandlers(@NonNull Message message) {
        MessageCallbackHandler callbackHandler = new MessageCallbackHandler(this, message.id);
        for (int i = 0, size = mMessageHandlers.size(); i < size; i++) {
            mMessageHandlers.get(i).onMessage(message, callbackHandler);
        }
    }

    public void registerMethodHandler(@NonNull MethodHandler handler) {
        if (!mMethodHandlers.contains(handler))
            mMethodHandlers.add(handler);
    }

    public void unregisterMethodHandler(@NonNull MethodHandler handler) {
        mMethodHandlers.remove(handler);
    }

    private void notifyMethodHandlers(@NonNull Message message) {
        try {
            String method = message.body.getString("method");
            JSONObject params = message.body.getJSONObject("params");
            MethodCallbackHandler callbackHandler = new MethodCallbackHandler(this, message.id);
            for (int i = 0, size = mMethodHandlers.size(); i < size; i++) {
                mMethodHandlers.get(i).onMethod(method, params, callbackHandler);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void postMessage(@NonNull String jsonString) {
        mMainHandler.sendMessage(mMainHandler.obtainMessage(1, jsonString));
    }

    public void postMessage(@NonNull Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", message.id);
            jsonObject.put("type", message.type);
            jsonObject.put("body", message.body);
            postMessage(jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    class AndroidInterface {

        @JavascriptInterface
        public void onBridgeMessage(String jsonString) {
            Log.d(TAG, "onBridgeMessage: " + jsonString);
            try {
                Message message = Message.fromJson(jsonString);
                if (message.type.equals("callMethod"))
                    notifyMethodHandlers(message);
                else
                    notifyMessageHandlers(message);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

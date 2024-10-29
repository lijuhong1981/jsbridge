package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
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
    private final Activity mActivity;
    private final WebView mWebView;
    private final ArrayList<MessageReceiver> mMessageReceivers = new ArrayList<>();
    private MethodHandler mMethodHandler;

    @SuppressLint("SetJavaScriptEnabled")
    public JsBridgeManager(@NonNull Activity activity,@NonNull WebView webView) {
        mActivity = activity;
        mWebView = webView;
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new AndroidInterface(), "jsbridgeInterface");
        mMethodHandler = new DefaultMethodHandler(activity);
    }

    public void registerMessageReceiver(@NonNull MessageReceiver handler) {
        if (!mMessageReceivers.contains(handler))
            mMessageReceivers.add(handler);
    }

    public void unregisterMessageReceiver(@NonNull MessageReceiver handler) {
        mMessageReceivers.remove(handler);
    }

    private void notifyMessageReceivers(@NonNull Message message) {
        MessageCallbackHandler callbackHandler = new MessageCallbackHandler(this, message.id);
        for (int i = 0, size = mMessageReceivers.size(); i < size; i++) {
            mMessageReceivers.get(i).onMessage(message, callbackHandler);
        }
    }

    public void setMethodHandler(@NonNull MethodHandler handler) {
        if (mMethodHandler != handler)
            mMethodHandler = handler;
    }

    private void notifyMethodHandler(@NonNull Message message) {
        try {
            String method = message.body.getString("method");
            JSONObject params = message.body.getJSONObject("params");
            MethodCallbackHandler callbackHandler = new MethodCallbackHandler(this, message.id);
            if (mMethodHandler != null)
                mMethodHandler.onMethod(method, params, callbackHandler);
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
                    notifyMethodHandler(message);
                else
                    notifyMessageReceivers(message);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

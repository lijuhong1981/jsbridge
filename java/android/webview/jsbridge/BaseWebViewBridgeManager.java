package android.webview.jsbridge;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webview.jsbridge.methods.CallPhoneHandler;
import android.webview.jsbridge.methods.CancelVibratorHandler;
import android.webview.jsbridge.methods.CaptureImageHandler;
import android.webview.jsbridge.methods.CaptureVideoHandler;
import android.webview.jsbridge.methods.DialPhoneHandler;
import android.webview.jsbridge.methods.DownloadFileHandler;
import android.webview.jsbridge.methods.ExitAppHandler;
import android.webview.jsbridge.methods.GetAppInfoHandler;
import android.webview.jsbridge.methods.GetAudioInfoHandler;
import android.webview.jsbridge.methods.GetConfigurationInfoHandler;
import android.webview.jsbridge.methods.GetDeviceInfoHandler;
import android.webview.jsbridge.methods.GetDisplayInfoHandler;
import android.webview.jsbridge.methods.PickContentHandler;
import android.webview.jsbridge.methods.SetAudioVolumeHandler;
import android.webview.jsbridge.methods.SetMicrophoneOnHandler;
import android.webview.jsbridge.methods.SetMuteHandler;
import android.webview.jsbridge.methods.SetOrientationHandler;
import android.webview.jsbridge.methods.SetSpeakerOnHandler;
import android.webview.jsbridge.methods.ShowAlertDialogHandler;
import android.webview.jsbridge.methods.ShowToastHandler;
import android.webview.jsbridge.methods.StartVibratorHandler;
import android.webview.jsbridge.methods.UploadFileHandler;
import android.webview.jsbridge.methods.ViewFileHandler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

abstract class BaseWebViewBridgeManager implements WebViewBridgeManagerInterface {
    public static final String TAG = BaseWebViewBridgeManager.class.getSimpleName();
    public static final String FILE_LOCAL_HOST = "file.local";

    public final Activity activity;
    private final ArrayList<MessageReceiver> mMessageReceivers = new ArrayList<>();
    private final Map<String, MethodHandler> mMethodHandlers = new HashMap<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action))
                onScreenOn();
            else if (Intent.ACTION_SCREEN_OFF.equals(action))
                onScreenOff();
        }
    };

    BaseWebViewBridgeManager(Activity activity) {
        this.activity = activity;
        registerDefaultMethodHandlers();
        registerBroadcastReceiver();
    }

    private void registerDefaultMethodHandlers() {
        registerMethodHander(new ShowToastHandler(activity));
        registerMethodHander(new ShowAlertDialogHandler(activity));
        registerMethodHander(new GetAppInfoHandler(activity));
        registerMethodHander(new GetDeviceInfoHandler(activity));
        registerMethodHander(new GetDisplayInfoHandler(activity));
        registerMethodHander(new GetConfigurationInfoHandler(activity));
        registerMethodHander(new GetAudioInfoHandler(activity));
        registerMethodHander(new SetOrientationHandler(activity));
        registerMethodHander(new SetMicrophoneOnHandler(activity));
        registerMethodHander(new SetSpeakerOnHandler(activity));
        registerMethodHander(new SetMuteHandler(activity));
        registerMethodHander(new SetAudioVolumeHandler(activity));
        registerMethodHander(new DialPhoneHandler(activity));
        registerMethodHander(new CallPhoneHandler(activity));
        registerMethodHander(new CaptureImageHandler(activity));
        registerMethodHander(new CaptureVideoHandler(activity));
        registerMethodHander(new PickContentHandler(activity));
        registerMethodHander(new ViewFileHandler(activity));
        registerMethodHander(new ExitAppHandler(activity));
        registerMethodHander(new UploadFileHandler(activity));
        registerMethodHander(new DownloadFileHandler(activity));
        registerMethodHander(new StartVibratorHandler(activity));
        registerMethodHander(new CancelVibratorHandler(activity));
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        activity.registerReceiver(mReceiver, filter);
    }

    @Override
    public void onPause() {
        Message msg = new Message();
        msg.type = "onPause";
        postMessage(msg);
    }

    @Override
    public void onResume() {
        Message msg = new Message();
        msg.type = "onResume";
        postMessage(msg);
    }

    @Override
    public void onStop() {
        Message msg = new Message();
        msg.type = "onStop";
        postMessage(msg);
    }

    @Override
    public void onDestroy() {
        activity.unregisterReceiver(mReceiver);
        Message msg = new Message();
        msg.type = "onDestroy";
        postMessage(msg);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        try {
            Message msg = new Message();
            msg.type = "onWindowFocusChanged";
            msg.body = new JSONObject();
            msg.body.put("hasFocus", hasFocus);
            postMessage(msg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        try {
            Message msg = new Message();
            msg.type = "onConfigurationChanged";
            msg.body = new JSONObject();
            msg.body.put("orientation", Tools.getOrientationString(newConfig.orientation));
            msg.body.put("fontScale", newConfig.fontScale);
            msg.body.put("mcc", newConfig.mcc);
            msg.body.put("mnc", newConfig.mnc);
            msg.body.put("screenLayout", newConfig.screenLayout);
            msg.body.put("touchscreen", newConfig.touchscreen);
            msg.body.put("keyboard", newConfig.keyboard);
            msg.body.put("keyboardHidden", newConfig.keyboardHidden);
            msg.body.put("hardKeyboardHidden", newConfig.hardKeyboardHidden);
            msg.body.put("navigation", newConfig.navigation);
            msg.body.put("navigationHidden", newConfig.navigationHidden);
            msg.body.put("uiMode", newConfig.uiMode);
            msg.body.put("screenWidthDp", newConfig.screenWidthDp);
            msg.body.put("screenHeightDp", newConfig.screenHeightDp);
            msg.body.put("smallestScreenWidthDp", newConfig.smallestScreenWidthDp);
            msg.body.put("densityDpi", newConfig.densityDpi);
            msg.body.put("colorMode", newConfig.colorMode);
            postMessage(msg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onScreenOn() {
        Message msg = new Message();
        msg.type = "onScreenOn";
        postMessage(msg);
    }

    @Override
    public void onScreenOff() {
        Message msg = new Message();
        msg.type = "onScreenOff";
        postMessage(msg);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        for (MethodHandler handler : mMethodHandlers.values()) {
            if (handler.hasActivityResult()) {
                List<Integer> requestCodes = handler.getRequestCodes();
                if (requestCodes.contains(requestCode))
                    handler.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void registerMessageReceiver(@NonNull MessageReceiver receiver) {
        if (!mMessageReceivers.contains(receiver))
            mMessageReceivers.add(receiver);
    }

    @Override
    public void unregisterMessageReceiver(@NonNull MessageReceiver receiver) {
        mMessageReceivers.remove(receiver);
    }

    @Override
    public void notifyMessageReceivers(@NonNull Message message) {
        MessageCallbackHandler callbackHandler = new MessageCallbackHandler(this, message.id);
        for (int i = 0, size = mMessageReceivers.size(); i < size; i++) {
            mMessageReceivers.get(i).onMessage(message, callbackHandler);
        }
    }

    @Override
    public void registerMethodHander(MethodHandler handler) {
        mMethodHandlers.put(handler.getMethod(), handler);
    }

    @Override
    public void unregisterMethodHandler(MethodHandler handler) {
        mMethodHandlers.remove(handler.getMethod());
    }

    @Override
    public void notifyMethodHandler(@NonNull Message message) {
        try {
            String method = message.body.getString("method");
            JSONObject params = message.body.getJSONObject("params");
            MethodCallbackHandler callbackHandler = new MethodCallbackHandler(this, message.id);
            MethodHandler handler = mMethodHandlers.get(method);
            if (handler != null)
                handler.handleMethod(params, callbackHandler);
            else
                callbackHandler.notifyErrorCallback(new NoSuchMethodError("The method " + method + " has unregistered."));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postMessage(@NonNull String jsonString) {
        activity.runOnUiThread(() -> {
            String replaceString = jsonString
                    .replace("\\r", "\\\\r")
                    .replace("\\n", "\\\\n")
                    .replace("\"", "\\\"");
            Log.d(TAG, "postMessageToWeb: " + replaceString);
            String script = "onBridgeMessage('" + replaceString + "')";
            evaluateJavascript(script);
        });
    }

    abstract void evaluateJavascript(@NonNull String scriptString);

    @Override
    public void postMessage(@NonNull Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
            if (TextUtils.isEmpty(message.id)) {
                UUID uuid = UUID.randomUUID();
                message.id = uuid.toString();
            }
            jsonObject.put("id", message.id);
            jsonObject.put("type", message.type);
            jsonObject.put("body", message.body);
            if (message.persistCallback)
                jsonObject.put("persistCallback", true);
            postMessage(jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postLogMessage(int priority, @Nullable String tag, @NonNull String msg, @Nullable Throwable tr) {
        try {
            Message message = new Message();
            message.type = "logMessage";
            message.body = new JSONObject();
            message.body.put("priority", priority);
            message.body.put("tag", tag);
            message.body.put("msg", msg);
            if (tr != null) {
                JSONObject errorInfo = new JSONObject();
                errorInfo.put("class", tr.getClass().getSimpleName());
                errorInfo.put("description", tr.getMessage());
                message.body.put("error", errorInfo);
            }
            postMessage(msg);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postLogMessage(int priority, @Nullable String tag, @NonNull String msg) {
        postLogMessage(priority, tag, msg, null);
    }

    @Override
    public void postLogMessage(@Nullable String tag, @NonNull String msg) {
        postLogMessage(0, tag, msg, null);
    }

    @Override
    public void postErrorMessage(@Nullable String tag, @NonNull String msg, @Nullable Throwable tr) {
        postLogMessage(Log.ERROR, tag, msg, tr);
    }

    class AndroidInterface {

        @JavascriptInterface
        public void onBridgeMessage(String jsonString) {
            Log.d(TAG, "onReceiveMessageFromWeb: " + jsonString);
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

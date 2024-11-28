package android.webview.jsbridge;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class MethodCallbackHandler extends MessageCallbackHandler{

    public MethodCallbackHandler(@NonNull WebViewBridgeManager manager, @NonNull String callbackMsgId) {
        super(manager, callbackMsgId);
    }

    @Override
    protected String getCallbackMsgType() {
        return "methodCallback";
    }

    public void notifySuccessCallback(@NonNull JSONObject resultData, boolean persistCallback) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", resultData);
            super.notifyCallback(response, persistCallback);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifySuccessCallback(@NonNull JSONObject resultData) {
        notifySuccessCallback(resultData, false);
    }

    public void notifySuccessCallback() {
        notifySuccessCallback(new JSONObject());
    }

    public void notifyErrorCallback(@NonNull Throwable error, boolean persistCallback) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", false);
            JSONObject errorInfo = new JSONObject();
            errorInfo.put("class", error.getClass().getSimpleName());
            errorInfo.put("description", error.getMessage());
            response.put("error", errorInfo);
            super.notifyCallback(response, persistCallback);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void notifyErrorCallback(@NonNull Throwable error) {
        notifyErrorCallback(error, false);
    }
}

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

    public void doSuccessCallback(@NonNull JSONObject resultData) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", resultData);
            super.doCallback(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void doSuccessCallback() {
        doSuccessCallback(new JSONObject());
    }

    public void doErrorCallback(@NonNull Throwable error) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", false);
            JSONObject errorInfo = new JSONObject();
            errorInfo.put("class", error.getClass().getSimpleName());
            errorInfo.put("description", error.getMessage());
            response.put("error", errorInfo);
            super.doCallback(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

package android.webview.jsbridge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class MethodCallbackHandler extends MessageCallbackHandler{

    public MethodCallbackHandler(@NonNull JsBridgeManager manager, @NonNull String callbackMsgId) {
        super(manager, callbackMsgId);
    }

    @Override
    protected String getCallbackMsgType() {
        return "methodCallback";
    }

    public void doSuccessCallback(JSONObject resultData) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", true);
            response.put("data", resultData);
            super.doCallback(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void doErrorCallback(@NonNull Throwable error) {
        try {
            JSONObject response = new JSONObject();
            response.put("success", false);
            response.put("errorClass", error.getClass().getSimpleName());
            response.put("errorInfo", error.getMessage());
            super.doCallback(response);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

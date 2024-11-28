package android.webview.jsbridge;

import org.json.JSONObject;

public interface UploadFileHandler {
    public void uploadFile(JSONObject params, MethodCallbackHandler callbackHandler);
}

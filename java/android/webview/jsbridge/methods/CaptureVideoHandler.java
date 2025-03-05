package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.Tools;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

import java.util.concurrent.CancellationException;

public class CaptureVideoHandler extends ActivityResultMethodHandler {
    protected MethodCallbackHandler mCallbackHandler;

    public CaptureVideoHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    void registerRequestCodes() {
        requestCodes.add(RequestCodes.REQUEST_CAPTURE_VIDEO);
    }

    @Override
    public String getMethod() {
        return "captureVideo";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            mCallbackHandler = callbackHandler;
            activity.startActivityForResult(intent, RequestCodes.REQUEST_CAPTURE_VIDEO);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                try {
                    if (data == null || data.getData() == null)
                        throw new NullPointerException("The pickContent result data is null.");
                    Uri contentUri = data.getData();
                    String filePath = Tools.getFilePathFromContentUri(activity, contentUri);
                    String url = "http://" + WebViewBridgeManager.FILE_LOCAL_HOST + "?file=" + filePath;
                    JSONObject resultData = new JSONObject();
                    resultData.put("filePath", filePath);
                    resultData.put("url", url);
                    mCallbackHandler.notifySuccessCallback(resultData);
                } catch (Exception e) {
//                            throw new RuntimeException(e);
                    mCallbackHandler.notifyErrorCallback(e);
                }
                break;
            case Activity.RESULT_CANCELED:
                mCallbackHandler.notifyErrorCallback(new CancellationException("The captureVideo canceled."));
                break;
            default:
                mCallbackHandler.notifyErrorCallback(new UnknownError("Unknown resultCode " + resultCode));
                break;
        }
        mCallbackHandler = null;
    }
}

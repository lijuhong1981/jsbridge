package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.Tools;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.CancellationException;

public class CaptureImageHandler extends ActivityResultMethodHandler {
    protected File mImageFile;
    protected MethodCallbackHandler mCallbackHandler;

    public CaptureImageHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    void registerRequestCodes() {
        requestCodes.add(RequestCodes.REQUEST_CAPTURE_IMAGE);
    }

    @Override
    public String getMethod() {
        return "captureImage";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imageFile = Tools.newImageFile(activity);
                Log.d(WebViewBridgeManager.TAG, "captureImage imageFile=" + imageFile.getAbsolutePath());
                Uri imageUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileProvider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                mImageFile = imageFile;
                mCallbackHandler = callbackHandler;
                activity.startActivityForResult(intent, RequestCodes.REQUEST_CAPTURE_IMAGE);
            } catch (Exception e) {
//                throw new RuntimeException(e);
                callbackHandler.notifyErrorCallback(e);
                Log.e(WebViewBridgeManager.TAG, "captureImage error:", e);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (resultCode) {
            case Activity.RESULT_OK:
                try {
                    String filePath = mImageFile.getAbsolutePath();
                    String url = "http://" + WebViewBridgeManager.FILE_LOCAL_HOST + "?file=" + filePath;
                    JSONObject resultData = new JSONObject();
                    resultData.put("filePath", filePath);
                    resultData.put("url", url);
                    mCallbackHandler.notifySuccessCallback(resultData);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case Activity.RESULT_CANCELED:
                mCallbackHandler.notifyErrorCallback(new CancellationException("The captureImage canceled."));
                break;
            default:
                mCallbackHandler.notifyErrorCallback(new UnknownError("Unknown resultCode " + resultCode));
                break;
        }
        mImageFile = null;
        mCallbackHandler = null;
    }
}

package android.webview.jsbridge.methods;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ViewFileHandler extends NoActivityResultMethodHandler {
    public ViewFileHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "viewFile";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            try {
                // 查看文件地址
                String url = params.getString("url");
                String type = null;
                try {
                    type = params.getString("type");
                } catch (JSONException e) {
//                    throw new RuntimeException(e);
                }
                // 未指定MIME Type，根据文件扩展名获取
                if (TextUtils.isEmpty(type)) {
                    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (TextUtils.isEmpty(type))
                    intent.setData(Uri.parse(url));
                else
                    intent.setDataAndType(Uri.parse(url), type);
//                PackageManager packageManager = manager.activity.getPackageManager();
//                @SuppressLint("QueryPermissionsNeeded")
//                List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);
//                Log.v(WebViewBridgeManager.TAG, "query resolveInfos size: " + resolveInfos.size());
//                for (ResolveInfo resolveInfo : resolveInfos) {
//                    // 可以获取应用的包名、类名等信息
//                    String packageName = resolveInfo.activityInfo.packageName;
//                    String className = resolveInfo.activityInfo.name;
//                    Log.d(WebViewBridgeManager.TAG, "type: " + type + "; packageName: " + packageName + "; className: " + className);
//                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                activity.startActivity(intent);
                callbackHandler.notifySuccessCallback();
            } catch (JSONException e) {
//            throw new RuntimeException(e);
                callbackHandler.notifyErrorCallback(e);
                Log.e(WebViewBridgeManager.TAG, "openFile error:", e);
            }
        });
    }
}

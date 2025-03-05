package android.webview.jsbridge.methods;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.Tools;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.UUID;

public class DownloadFileHandler extends NoActivityResultMethodHandler {
    public DownloadFileHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "downloadFile";
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        try {
            // 下载文件地址
            String url = params.getString("url");
            String fileName = null;
            try {
                fileName = params.getString("fileName");
            } catch (JSONException e) {
//                throw new RuntimeException(e);
            }
            if (TextUtils.isEmpty(fileName))
                fileName = Tools.getFileNameFromUrl(url);
            // 获取不到文件名，使用随机uuid作为文件名
            if (TextUtils.isEmpty(fileName))
                fileName = UUID.randomUUID().toString();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(true)
                    .setTitle(fileName)
                    .setDescription("正在下载...")
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadId = Objects.requireNonNull(downloadManager).enqueue(request);
            Log.d(WebViewBridgeManager.TAG, "start downloadFile url = " + url + "; downloadId = " + downloadId);
            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (receivedId == downloadId) {
                        activity.unregisterReceiver(this);
                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadId);
                        try (Cursor cursor = downloadManager.query(query)) {
                            if (cursor.moveToFirst()) {
                                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                int status = cursor.getInt(statusIndex);
                                Log.d(WebViewBridgeManager.TAG, "queryDownloadStatusInReceiver url = " + url + "; status = " + status);
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    // 获取文件总大小
                                    int totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                                    int totalBytes = cursor.getInt(totalBytesIndex);
                                    // 获取下载文件的本地路径
                                    int localIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                                    String localUrl = cursor.getString(localIndex);
                                    Uri localUri = Uri.parse(localUrl);
                                    String filePath = localUri.getPath();
                                    Log.v(WebViewBridgeManager.TAG, "get downloadFile url = " + url + "; filePath = " + filePath);
                                    // 下载完成通知
                                    notifyCompleteCallback(callbackHandler, totalBytes, filePath);
                                } else if (status == DownloadManager.STATUS_FAILED) {
                                    // 下载失败通知
                                    callbackHandler.notifyErrorCallback(new UnknownError("STATUS_FAILED"));
                                }
                            } else {
                                notifyCancelCallback(callbackHandler);
                            }
                        }
                    }
                }
            };
            activity.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED);
            activity.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED), Context.RECEIVER_EXPORTED);
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
                        Log.e(WebViewBridgeManager.TAG, "", e);
                        break;
                    }
                    Log.i(WebViewBridgeManager.TAG, "queryDownloadProcessInThread url = " + url);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    try (Cursor cursor = downloadManager.query(query)) {
                        if (cursor.moveToFirst()) {
                            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            int status = cursor.getInt(statusIndex);
                            Log.d(WebViewBridgeManager.TAG, "queryDownloadStatusInThread url = " + url + "; status = " + status);
                            if (status == DownloadManager.STATUS_RUNNING) {
                                // 获取文件已下载大小
                                int bytesWrittenIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                                int bytesWritten = cursor.getInt(bytesWrittenIndex);
                                // 获取文件总大小
                                int totalBytesIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                                int totalBytes = cursor.getInt(totalBytesIndex);
                                // 下载进度通知
                                notifyPartialCallback(callbackHandler, bytesWritten, totalBytes);
                                continue;
                            }
                        }
                    }
                    break;
                }
            }).start();
        } catch (Exception e) {
//            throw new RuntimeException(e);
            Log.e(WebViewBridgeManager.TAG, "downloadFile error", e);
            callbackHandler.notifyErrorCallback(e);
        }
    }

    private void notifyPartialCallback(MethodCallbackHandler callbackHandler, long bytesWritten, long totalBytes) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("bytesWritten", bytesWritten);
            resultData.put("totalBytes", totalBytes);
            resultData.put("status", "partial");
            callbackHandler.notifySuccessCallback(resultData, true);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void notifyCompleteCallback(MethodCallbackHandler callbackHandler, long totalBytes, String filePath) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("bytesWritten", totalBytes);
            resultData.put("totalBytes", totalBytes);
            resultData.put("filePath", filePath);
            resultData.put("status", "complete");
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void notifyCancelCallback(MethodCallbackHandler callbackHandler) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("status", "cancel");
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

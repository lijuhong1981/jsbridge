package android.webview.jsbridge;

import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class DefaultUploadFileHandler implements UploadFileHandler {
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

    private void notifyCompleteCallback(MethodCallbackHandler callbackHandler, long totalBytes) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("bytesWritten", totalBytes);
            resultData.put("totalBytes", totalBytes);
            resultData.put("status", "complete");
            callbackHandler.notifySuccessCallback(resultData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadFile(JSONObject params, MethodCallbackHandler callbackHandler) {
        new Thread(() -> {
            try {
                String uploadUrl = params.getString("uploadUrl");
                String filePath = params.getString("filePath");
                JSONObject headers = null;
                try {
                    headers = params.getJSONObject("headers");
                } catch (JSONException e) {
//                    throw new RuntimeException(e);
                }

                File file = new File(filePath);
                if (!file.exists())
                    throw new FileNotFoundException("Not found the " + filePath + " file.");

                long totalBytes = file.length();
                String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null)
                    mimeType = "application/octet-stream";
                Log.d(WebViewBridgeManager.TAG, "getMimeTypeFromExtension: mimeType = " + mimeType + "; extension = " + extension);

                // 建立网络连接
                URL url = new URL(uploadUrl); // 替换为实际的服务器上传地址
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", mimeType);
                if (headers != null) {
                    Iterator<String> keys = headers.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Object value = headers.get(key);
                        connection.setRequestProperty(key, value.toString());
                    }
                }

                // 设置请求体，写入视频数据
                OutputStream outputStream = connection.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[2048];
                long bytesWritten = 0;
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    bytesWritten += bytesRead;
                    notifyPartialCallback(callbackHandler, bytesWritten, totalBytes);
                }
                fileInputStream.close();
                outputStream.close();

                // 获取服务器响应
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    notifyCompleteCallback(callbackHandler, totalBytes);
                } else {
                    callbackHandler.notifyErrorCallback(new IOException("The uploadFile failed, responseCode is " + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
//                throw new RuntimeException(e);
                Log.e(WebViewBridgeManager.TAG, "uploadFile error", e);
                callbackHandler.notifyErrorCallback(e);
            }
        }).start();
    }
}

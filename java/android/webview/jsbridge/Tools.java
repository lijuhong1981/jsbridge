package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class Tools {

    @SuppressLint("SimpleDateFormat")
    public static String getCurrentTimeStamp(@Nullable String pattern) {
        if (TextUtils.isEmpty(pattern))
            pattern = "yyyyMMdd_HHmmss";
        return new SimpleDateFormat(pattern).format(new Date(System.currentTimeMillis()));
    }

    public static String getCurrentTimeStamp() {
        return getCurrentTimeStamp(null);
    }

    public static File newImageFile(@NonNull Context context, @Nullable String format) {
        UUID uuid = UUID.randomUUID();
        if (TextUtils.isEmpty(format))
            format = "jpg";
        String imageFileName = uuid.toString() + "." + format;
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
    }

    public static File newImageFile(@NonNull Context context) {
        return newImageFile(context, null);
    }

    public static String getOrientationString(int orientation) {
        switch (orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return "portrait";
            case Configuration.ORIENTATION_LANDSCAPE:
                return "landscape";
            default:
                return "unknown";
        }
    }

    private static String getFilePathFromContentResolver(@NonNull Context context, @NonNull Uri uri, @Nullable String id) throws Exception {
        String filePath = null;
        String selection = null;
        if (!TextUtils.isEmpty(id))
            selection = MediaStore.Files.FileColumns._ID + "=" + id;
        try (Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                filePath = cursor.getString(columnIndex);
            }
        }
        return filePath;
    }

    @SuppressLint({"ObsoleteSdkInt"})
    public static String getFilePathFromContentUri(@NonNull Context context, @NonNull Uri uri) throws Exception {
        final String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
                final String authority = uri.getAuthority();
                if ("com.android.externalstorage.documents".equals(authority)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    final String id = split[1];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + id;
                    }
                } else if ("com.android.providers.media.documents".equals(authority)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    final String id = split[1];
                    final Uri contentUri;
                    if (type.equalsIgnoreCase("image")) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if (type.equalsIgnoreCase("audio")) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    } else if (type.equalsIgnoreCase("video")) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else {
                        contentUri = MediaStore.Files.getContentUri("external");
                    }
                    return getFilePathFromContentResolver(context, contentUri, id);
                } else if ("com.android.providers.downloads.documents".equals(authority)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try(Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                            if (cursor != null && cursor.moveToFirst()) {
                                int columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                                String fileName = cursor.getString(columnIndex);
                                return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + fileName;
                            }
                        }
                    }
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = Uri.parse("content://downloads/public_downloads");
                    return getFilePathFromContentResolver(context, contentUri, docId);
                } else if ("content".equalsIgnoreCase(authority)) {
                    return getFilePathFromContentResolver(context, uri, null);
                }
            }
            return getFilePathFromContentResolver(context, uri, null);
        } else if ("file".equalsIgnoreCase(scheme)) {
            return uri.getPath();
        }
        throw new UnsupportedSchemeException("The getFilePathFromMediaStoreUri unsupported the " + scheme + " scheme.");
    }

    public static String getFileNameFromUrl(String url) {
        Uri uri = Uri.parse(url);
        String path = uri.getPath();
        if (path == null) {
            return null;
        }
        return Objects.requireNonNull(path).substring(path.lastIndexOf('/') + 1);
    }

    public static void printCursorContent(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String[] names = cursor.getColumnNames();
                for (String name : names) {
                    int index = cursor.getColumnIndex(name);
                    int type = cursor.getType(index);
                    switch (type) {
                        case Cursor.FIELD_TYPE_NULL:
                            Log.v(WebViewBridgeManager.TAG, "name: " + name + "; value: " + null);
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            Log.v(WebViewBridgeManager.TAG, "name: " + name + "; value: " + cursor.getInt(index));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            Log.v(WebViewBridgeManager.TAG, "name: " + name + "; value: " + cursor.getFloat(index));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            Log.v(WebViewBridgeManager.TAG, "name: " + name + "; value: " + cursor.getString(index));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            Log.v(WebViewBridgeManager.TAG, "name: " + name + "; value: " + Arrays.toString(cursor.getBlob(index)));
                            break;
                    }
                }
            }
        }
    }
}

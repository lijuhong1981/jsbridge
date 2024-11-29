package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

    public static File createImageFile(@NonNull Context context) throws IOException {
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
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

    public static String getFilePathFromContentResolver(@NonNull Context context, @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) throws Exception {
        String filePath = null;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                    filePath = cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return filePath;
    }

    public static String getFilePathFromContentResolver(@NonNull Context context, @NonNull Uri uri) throws Exception {
        return getFilePathFromContentResolver(context, uri, null, null);
    }

    @SuppressLint("ObsoleteSdkInt")
    public static String getFilePathFromUri(@NonNull Context context, @NonNull Uri uri) throws Exception {
        final String scheme = uri.getScheme();
        if ("content".equalsIgnoreCase(scheme)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    final String id = split[1];
                    final String authority = uri.getAuthority();
                    if ("com.android.externalstorage.documents".equals(authority)) {
                        if ("primary".equalsIgnoreCase(type)) {
                            return Environment.getExternalStorageDirectory() + "/" + split[1];
                        }
                    } else if ("com.android.providers.media.documents".equals(authority)) {
                        Uri contentUri = null;
                        if (type.equalsIgnoreCase("image")) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if (type.equalsIgnoreCase("audio")) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        } else if (type.equalsIgnoreCase("video")) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else
                            throw new UnsupportedSchemeException("The type " + type + " is unsupported.");
                        final String selection = MediaStore.Files.FileColumns._ID + "=" + id;
                        return getFilePathFromContentResolver(context, contentUri, selection, null);
                    } else if ("com.android.providers.media.downloads.documents".equals(authority)) {
                        Uri contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                Long.parseLong(docId));
                        return getFilePathFromContentResolver(context, contentUri);
                    } else if ("content".equalsIgnoreCase(authority)) {
                        return getFilePathFromContentResolver(context, uri);
                    }
                }
            }
            return getFilePathFromContentResolver(context, uri);
        } else if ("file".equalsIgnoreCase(scheme)) {
            return uri.getPath();
        }
        throw new UnsupportedSchemeException("The getFilePathFromMediaStoreUri unsupported the " + scheme + " scheme.");
    }
}

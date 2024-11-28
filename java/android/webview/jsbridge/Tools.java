package android.webview.jsbridge;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.UnsupportedSchemeException;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Tools {

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

    public static String getFilePathFromMediaStoreUri(@NonNull Context context, @NonNull Uri uri) throws UnsupportedSchemeException {
        String filePath = null;
        String scheme = uri.getScheme();
        switch (Objects.requireNonNull(scheme)) {
            case "content":
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                    cursor.close();
                }
                break;
            case "file":
                filePath = uri.getPath();
                break;
            default:
                throw new UnsupportedSchemeException("The getFilePathFromMediaStoreUri unsupported the " + scheme + " scheme.");
        }
        return filePath;
    }
}

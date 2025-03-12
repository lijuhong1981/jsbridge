package android.webview.jsbridge.methods;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webview.jsbridge.MethodCallbackHandler;
import android.webview.jsbridge.Tools;
import android.webview.jsbridge.WebViewBridgeManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.util.concurrent.CancellationException;

public class PickContentHandler extends ActivityResultMethodHandler {
    protected MethodCallbackHandler mCallbackHandler;

    public PickContentHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public void registerRequestCodes() {
        requestCodes.add(RequestCodes.REQUEST_PICK_FILE);
        requestCodes.add(RequestCodes.REQUEST_PICK_IMAGE);
        requestCodes.add(RequestCodes.REQUEST_PICK_VIDEO);
        requestCodes.add(RequestCodes.REQUEST_PICK_AUDIO);
        requestCodes.add(RequestCodes.REQUEST_PICK_CONTACTS);
    }

    @Override
    public String getMethod() {
        return "pickContent";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
            String type = "file";
            try {
                type = params.getString("type");
            } catch (JSONException ignored) {
            }
            Intent intent = null;
            int requestCode;
            switch (type) {
                case "file":
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    requestCode = RequestCodes.REQUEST_PICK_FILE;
                    break;
                case "image":
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    requestCode = RequestCodes.REQUEST_PICK_IMAGE;
                    break;
                case "video":
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    requestCode = RequestCodes.REQUEST_PICK_VIDEO;
                    break;
                case "audio":
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    requestCode = RequestCodes.REQUEST_PICK_AUDIO;
                    break;
                case "contacts":
                    intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    requestCode = RequestCodes.REQUEST_PICK_CONTACTS;
                    break;
                default:
                    callbackHandler.notifyErrorCallback(new NoSuchFieldException("The pickContent unsupported the " + type + " type."));
                    return;
            }
            mCallbackHandler = callbackHandler;
            activity.startActivityForResult(intent, requestCode);
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
                    String type = getTypeFromRequestCode(requestCode);
                    JSONObject resultData = new JSONObject();
                    resultData.put("type", type);
                    if (requestCode == RequestCodes.REQUEST_PICK_CONTACTS) {
                        Cursor cursor = activity.getContentResolver().query(
                                contentUri,
                                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                                null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            @SuppressLint("Range")
                            String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                            resultData.put("displayName", displayName);
                            Cursor phoneCursor = activity.getContentResolver().query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?",
                                    new String[]{contentUri.getLastPathSegment()}, null);
                            if (phoneCursor != null) {
                                JSONArray phoneNumbers = new JSONArray();
                                while (phoneCursor.moveToNext()) {
                                    @SuppressLint("Range")
                                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    phoneNumbers.put(phoneNumber);
                                }
                                phoneCursor.close();
                                resultData.put("phoneNumbers", phoneNumbers);
                            }
                            cursor.close();
                        }
                    } else {
                        String filePath = Tools.getFilePathFromContentUri(activity, contentUri);
                        if (TextUtils.isEmpty(filePath))
                            throw new FileNotFoundException("Not found the filePath from uri " + contentUri);
                        String url = "http://" + WebViewBridgeManager.FILE_LOCAL_HOST + "?file=" + filePath;
                        resultData.put("filePath", filePath);
                        resultData.put("url", url);
                    }
                    mCallbackHandler.notifySuccessCallback(resultData);
                } catch (Exception e) {
//                            throw new RuntimeException(e);
                    mCallbackHandler.notifyErrorCallback(e);
                }
                break;
            case Activity.RESULT_CANCELED:
                mCallbackHandler.notifyErrorCallback(new CancellationException("The pickContent canceled."));
                break;
            default:
                mCallbackHandler.notifyErrorCallback(new UnknownError("Unknown resultCode " + resultCode));
                break;
        }
        mCallbackHandler = null;
    }

    private static String getTypeFromRequestCode(int requestCode) throws NoSuchFieldException {
        switch (requestCode) {
            case RequestCodes.REQUEST_PICK_FILE:
                return "file";
            case RequestCodes.REQUEST_PICK_IMAGE:
                return "image";
            case RequestCodes.REQUEST_PICK_VIDEO:
                return "video";
            case RequestCodes.REQUEST_PICK_AUDIO:
                return "audio";
            case RequestCodes.REQUEST_PICK_CONTACTS:
                return "contacts";
            default:
                throw new NoSuchFieldException("The getTypeFromRequestCode unsupported requestCode " + requestCode);
        }
    }
}

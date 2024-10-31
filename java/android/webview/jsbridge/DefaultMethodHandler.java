package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultMethodHandler implements MethodHandler {
    public static final int REQUEST_TAKE_PHOTO = 0x1000;

    protected final Activity mActivity;
    protected File mTakePhotoFile;
    protected MethodCallbackHandler mTakePhotoCallbackHandler;

    public DefaultMethodHandler(@NonNull Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler) {
        switch (method) {
            case "methodTest":
                callbackHandler.doSuccessCallback();
                break;
            case "showToast":
                showToast(params, callbackHandler);
                break;
            case "showAlertDialog":
                showAlertDialog(params, callbackHandler);
                break;
            case "getAppInfo":
                getAppInfo(callbackHandler);
                break;
            case "getDeviceInfo":
                getDeviceInfo(callbackHandler);
                break;
            case "getDisplayInfo":
                getDisplayInfo(callbackHandler);
                break;
            case "dialPhone":
                dialPhone(params, callbackHandler);
                break;
            case "callPhone":
                callPhone(params, callbackHandler);
                break;
            case "takePhoto":
                takePhoto(params, callbackHandler);
                break;
            case "exitApp":
                exitApp(params, callbackHandler);
                break;
            default:
                callbackHandler.doErrorCallback(new NoSuchMethodException("Not found the " + method + " method."));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v(WebViewBridgeManager.TAG, "onActivityResult requestCode: " + requestCode + " ; resultCode: " + resultCode + " ; data: " + data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                switch (resultCode){
                    case Activity.RESULT_OK:
                        try {
                            String url = "http://" + WebViewBridgeManager.FILE_LOCAL_HOST + "?file=" + mTakePhotoFile.getAbsolutePath();
                            JSONObject resultData = new JSONObject();
                            resultData.put("url", url);
                            mTakePhotoCallbackHandler.doSuccessCallback(resultData);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        mTakePhotoCallbackHandler.doErrorCallback(new Exception("canceled"));
                        break;
                    default:
                        mTakePhotoCallbackHandler.doErrorCallback(new Exception("unknow error"));
                        break;
                }
                mTakePhotoFile = null;
                mTakePhotoCallbackHandler = null;
                break;
        }
    }

    public void showToast(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            Toast.makeText(mActivity, params.getString("text"), Toast.LENGTH_SHORT).show();
            callbackHandler.doSuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "showToast error:", e);
        }
    }

    private DialogInterface.OnClickListener getClickListener(String positiveButtonLabel, String negativeButtonLabel, String neutralButtonLabel, MethodCallbackHandler callbackHandler) {
        return (dialog, which) -> {
            dialog.dismiss();
            try {
                JSONObject resultData = new JSONObject();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        resultData.put("clickButton", "positive");
                        resultData.put("clickButtonLabel", positiveButtonLabel);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        resultData.put("clickButton", "negative");
                        resultData.put("clickButtonLabel", negativeButtonLabel);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        resultData.put("clickButton", "neutral");
                        resultData.put("clickButtonLabel", neutralButtonLabel);
                        break;
                }
                resultData.put("clickButtonIndex", which);
                callbackHandler.doSuccessCallback(resultData);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public void showAlertDialog(JSONObject params, MethodCallbackHandler callbackHandler) {
        mActivity.runOnUiThread(() -> {
            String title = null;
            try {
                title = params.getString("title");
            } catch (JSONException ignored) {
            }
            String content = null;
            try {
                content = params.getString("content");
            } catch (JSONException ignored) {
            }
            boolean showPositiveButton = true;
            try {
                showPositiveButton = params.getBoolean("showPositiveButton");
            } catch (JSONException ignored) {
            }
            String positiveButtonLabel = "确定";
            try {
                positiveButtonLabel = params.getString("positiveButtonLabel");
            } catch (JSONException ignored) {
            }
            boolean showNegativeButton = false;
            try {
                showNegativeButton = params.getBoolean("showNegativeButton");
            } catch (JSONException ignored) {
            }
            String negativeButtonLabel = "取消";
            try {
                negativeButtonLabel = params.getString("negativeButtonLabel");
            } catch (JSONException ignored) {
            }
            boolean showNeutralButton = false;
            try {
                showNeutralButton = params.getBoolean("showNeutralButton");
            } catch (JSONException ignored) {
            }
            String neutralButtonLabel = "中立";
            try {
                neutralButtonLabel = params.getString("neutralButtonLabel");
            } catch (JSONException ignored) {
            }
            boolean cancelable = true;
            try {
                cancelable = params.getBoolean("cancelable");
            } catch (JSONException ignored) {
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            if (title != null) builder.setTitle(title);
            if (content != null) builder.setMessage(content);
            DialogInterface.OnClickListener clickListener = getClickListener(positiveButtonLabel, negativeButtonLabel, neutralButtonLabel, callbackHandler);
            if (showPositiveButton) builder.setPositiveButton(positiveButtonLabel, clickListener);
            if (showNegativeButton) builder.setNegativeButton(negativeButtonLabel, clickListener);
            if (showNeutralButton) builder.setNeutralButton(neutralButtonLabel, clickListener);
            if (cancelable) {
                builder.setCancelable(true);
                builder.setOnCancelListener(dialog -> {
//                    dialog.dismiss();
                    clickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                });
            }
            builder.create().show();
        });
    }

    public void getAppInfo(MethodCallbackHandler callbackHandler) {
        try {
            String packageName = mActivity.getPackageName();
            PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(packageName, 0);
            JSONObject resultData = new JSONObject();
            resultData.put("packageName", packageName);
            resultData.put("versionName", packageInfo.versionName);
            resultData.put("versionCode", packageInfo.versionCode);
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException | PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getAppInfo error:", e);
        }
    }

    public void getDeviceInfo(MethodCallbackHandler callbackHandler) {
        try {
            JSONObject resultData = new JSONObject();
            resultData.put("BOARD", Build.BOARD);
            resultData.put("BOOTLOADER", Build.BOOTLOADER);
            resultData.put("BRAND", Build.BRAND);
            resultData.put("DEVICE", Build.DEVICE);
            resultData.put("DISPLAY", Build.DISPLAY);
            resultData.put("FINGERPRINT", Build.FINGERPRINT);
            resultData.put("HARDWARE", Build.HARDWARE);
            resultData.put("HOST", Build.HOST);
            resultData.put("ID", Build.ID);
            resultData.put("MANUFACTURER", Build.MANUFACTURER);
            resultData.put("MODEL", Build.MODEL);
            resultData.put("PRODUCT", Build.PRODUCT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultData.put("ODM_SKU", Build.ODM_SKU);
                resultData.put("SKU", Build.SKU);
                resultData.put("SOC_MANUFACTURER", Build.SOC_MANUFACTURER);
                resultData.put("SOC_MODEL", Build.SOC_MODEL);
            }
            resultData.put("USER", Build.USER);
            JSONObject versionData = new JSONObject();
            versionData.put("BASE_OS", Build.VERSION.BASE_OS);
            versionData.put("CODENAME", Build.VERSION.CODENAME);
            versionData.put("INCREMENTAL", Build.VERSION.INCREMENTAL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                versionData.put("MEDIA_PERFORMANCE_CLASS", Build.VERSION.MEDIA_PERFORMANCE_CLASS);
            }
            versionData.put("PREVIEW_SDK_INT", Build.VERSION.PREVIEW_SDK_INT);
            versionData.put("RELEASE", Build.VERSION.RELEASE);
            versionData.put("SDK_INT", Build.VERSION.SDK_INT);
            versionData.put("SECURITY_PATCH", Build.VERSION.SECURITY_PATCH);
            resultData.put("VERSION", versionData);
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getDeviceInfo error:", e);
        }
    }

    public void getDisplayInfo(MethodCallbackHandler callbackHandler) {
        try {
            DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
            JSONObject resultData = new JSONObject();
            resultData.put("widthPixels", displayMetrics.widthPixels);
            resultData.put("heightPixels", displayMetrics.heightPixels);
            resultData.put("density", displayMetrics.density);
            resultData.put("densityDpi", displayMetrics.densityDpi);
            resultData.put("xdpi", displayMetrics.xdpi);
            resultData.put("ydpi", displayMetrics.ydpi);
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getDisplayInfo error:", e);
        }
    }

    public void dialPhone(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            final String phoneNumber = params.getString("phoneNumber");
            mActivity.runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
                callbackHandler.doSuccessCallback();
            });
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "dialPhone error:", e);
        }
    }

    public void callPhone(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            final String phoneNumber = params.getString("phoneNumber");
            mActivity.runOnUiThread(() -> {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
                callbackHandler.doSuccessCallback();
            });
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "callPhone error:", e);
        }
    }

    protected File getImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        return new File(mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
    }

    public void takePhoto(JSONObject params, MethodCallbackHandler callbackHandler) {
        mActivity.runOnUiThread(() -> {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File imageFile = getImageFile();
                Log.d(WebViewBridgeManager.TAG, "takePhoto imageFile=" + imageFile.getAbsolutePath());
                Uri imageUri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".fileProvider", imageFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                mTakePhotoFile = imageFile;
                mTakePhotoCallbackHandler = callbackHandler;
                mActivity.startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
//                throw new RuntimeException(e);
                callbackHandler.doErrorCallback(e);
                Log.e(WebViewBridgeManager.TAG, "takePhoto error:", e);
            }
        });
    }

    public void exitApp(JSONObject params, MethodCallbackHandler callbackHandler) {
        boolean force = false;
        try {
            force = params.getBoolean("force");
        } catch (JSONException ignored) {
        }
        if (force)
            System.exit(0);
        else
            mActivity.finishAffinity();
    }
}

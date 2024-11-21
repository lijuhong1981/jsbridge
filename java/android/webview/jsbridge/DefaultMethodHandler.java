package android.webview.jsbridge;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
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
            case "getConfigurationInfo":
                getConfigurationInfo(callbackHandler);
                break;
            case "getAudioInfo":
                getAudioInfo(callbackHandler);
                break;
            case "setOrientation":
                setOrientation(params, callbackHandler);
                break;
            case "setSpeakerOn":
                setSpeakerOn(params, callbackHandler);
                break;
            case "setMicrophoneOn":
                setMicrophoneOn(params, callbackHandler);
                break;
            case "setMediaVolume":
                setMediaVolume(params, callbackHandler);
                break;
            case "setVoiceVolume":
                setVoiceVolume(params, callbackHandler);
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
                switch (resultCode) {
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

    private DialogInterface.OnClickListener getClickListener(String positiveButtonName, String negativeButtonName, String neutralButtonName, MethodCallbackHandler callbackHandler) {
        return (dialog, which) -> {
            dialog.dismiss();
            try {
                JSONObject resultData = new JSONObject();
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        resultData.put("clickButton", "positive");
                        resultData.put("clickButtonName", positiveButtonName);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        resultData.put("clickButton", "negative");
                        resultData.put("clickButtonName", negativeButtonName);
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        resultData.put("clickButton", "neutral");
                        resultData.put("clickButtonName", neutralButtonName);
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
            String positiveButtonName = "确定";
            try {
                positiveButtonName = params.getString("positiveButtonName");
            } catch (JSONException ignored) {
            }
            boolean showNegativeButton = false;
            try {
                showNegativeButton = params.getBoolean("showNegativeButton");
            } catch (JSONException ignored) {
            }
            String negativeButtonName = "取消";
            try {
                negativeButtonName = params.getString("negativeButtonName");
            } catch (JSONException ignored) {
            }
            boolean showNeutralButton = false;
            try {
                showNeutralButton = params.getBoolean("showNeutralButton");
            } catch (JSONException ignored) {
            }
            String neutralButtonName = "中立";
            try {
                neutralButtonName = params.getString("neutralButtonName");
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
            DialogInterface.OnClickListener clickListener = getClickListener(positiveButtonName, negativeButtonName, neutralButtonName, callbackHandler);
            if (showPositiveButton) builder.setPositiveButton(positiveButtonName, clickListener);
            if (showNegativeButton) builder.setNegativeButton(negativeButtonName, clickListener);
            if (showNeutralButton) builder.setNeutralButton(neutralButtonName, clickListener);
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
            resultData.put("VERSION", getVersionData());
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getDeviceInfo error:", e);
        }
    }

    private @NonNull JSONObject getVersionData() throws JSONException {
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
        return versionData;
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

    public void getConfigurationInfo(MethodCallbackHandler callbackHandler) {
        try {
            Configuration configuration = mActivity.getResources().getConfiguration();
            JSONObject resultData = new JSONObject();
            resultData.put("orientation", WebViewBridgeManager.getOrientationString(configuration.orientation));
            resultData.put("fontScale", configuration.fontScale);
            resultData.put("mcc", configuration.mcc);
            resultData.put("mnc", configuration.mnc);
            resultData.put("screenLayout", configuration.screenLayout);
            resultData.put("touchscreen", configuration.touchscreen);
            resultData.put("keyboard", configuration.keyboard);
            resultData.put("keyboardHidden", configuration.keyboardHidden);
            resultData.put("hardKeyboardHidden", configuration.hardKeyboardHidden);
            resultData.put("navigation", configuration.navigation);
            resultData.put("navigationHidden", configuration.navigationHidden);
            resultData.put("uiMode", configuration.uiMode);
            resultData.put("screenWidthDp", configuration.screenWidthDp);
            resultData.put("screenHeightDp", configuration.screenHeightDp);
            resultData.put("smallestScreenWidthDp", configuration.smallestScreenWidthDp);
            resultData.put("densityDpi", configuration.densityDpi);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                resultData.put("colorMode", configuration.colorMode);
            }
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getConfigurationInfo error:", e);
        }
    }

    public void getAudioInfo(MethodCallbackHandler callbackHandler) {
        try {
            AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            JSONObject resultData = new JSONObject();
            resultData.put("microphoneOn", !audioManager.isMicrophoneMute());
            resultData.put("speakerOn", audioManager.isSpeakerphoneOn());
            resultData.put("mediaVolume", audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                resultData.put("minMediaVolume", audioManager.getStreamMinVolume(AudioManager.STREAM_MUSIC));
                resultData.put("maxMediaVolume", audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            }
            resultData.put("voiceVolume", audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                resultData.put("minVoiceVolume", audioManager.getStreamMinVolume(AudioManager.STREAM_VOICE_CALL));
                resultData.put("maxVoiceVolume", audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
            }
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "getAudioInfo error:", e);
        }
    }

    public void setOrientation(JSONObject params, MethodCallbackHandler callbackHandler) {
        mActivity.runOnUiThread(() -> {
            try {
                String value = params.getString("value");
                switch (value.toLowerCase()) {
                    case "portrait":
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        break;
                    case "landscape":
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        break;
                    default:
                        Log.w(WebViewBridgeManager.TAG, "setOrientation unknown orientation value " + value);
                        break;
                }
                callbackHandler.doSuccessCallback();
            } catch (JSONException e) {
//            throw new RuntimeException(e);
                callbackHandler.doErrorCallback(e);
                Log.e(WebViewBridgeManager.TAG, "setOrientation error:", e);
            }
        });
    }

    public void setSpeakerOn(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            boolean value = params.getBoolean("value");
            AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setSpeakerphoneOn(value);
            callbackHandler.doSuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "muteSpeaker error:", e);
        }
    }

    public void setMicrophoneOn(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            boolean value = params.getBoolean("value");
            AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMicrophoneMute(!value);
            callbackHandler.doSuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "muteMicrophone error:", e);
        }
    }

    public void setMediaVolume(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            int value = params.getInt("value");
            AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
            callbackHandler.doSuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "muteMicrophone error:", e);
        }
    }

    public void setVoiceVolume(JSONObject params, MethodCallbackHandler callbackHandler) {
        try {
            int value = params.getInt("value");
            AudioManager audioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, value, 0);
            callbackHandler.doSuccessCallback();
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(WebViewBridgeManager.TAG, "muteMicrophone error:", e);
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

package android.webview.jsbridge;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class DefaultMethodHandler implements  MethodHandler
{
    protected final Activity mActivity;

    public  DefaultMethodHandler(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler) {
        switch (method) {
            case "methodTest":
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("result", true);
                    callbackHandler.doCallback(jsonObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "showToast":
                try {
                    showToast(params.getString("text"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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
        }
    }

    public void showToast(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

    public void getAppInfo(MethodCallbackHandler callbackHandler) {
        try {
            String packageName = mActivity.getPackageName();
            PackageInfo packageInfo = mActivity.getPackageManager().getPackageInfo(packageName, 0);
            JSONObject resultData = new JSONObject();
            resultData.put("packageName",packageName);
            resultData.put("versionName",packageInfo.versionName);
            resultData.put("versionCode",packageInfo.versionCode);
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException | PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(JsBridgeManager.TAG, "getAppInfo error:", e);
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
            Log.e(JsBridgeManager.TAG, "getDeviceInfo error:", e);
        }
    }

    public void getDisplayInfo(MethodCallbackHandler callbackHandler) {
        try {
            DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
            JSONObject resultData = new JSONObject();
            resultData.put("widthPixels",displayMetrics.widthPixels);
            resultData.put("heightPixels",displayMetrics.heightPixels);
            resultData.put("density",displayMetrics.density);
            resultData.put("densityDpi",displayMetrics.densityDpi);
            resultData.put("xdpi",displayMetrics.xdpi);
            resultData.put("ydpi",displayMetrics.ydpi);
            callbackHandler.doSuccessCallback(resultData);
        } catch (JSONException e) {
//            throw new RuntimeException(e);
            callbackHandler.doErrorCallback(e);
            Log.e(JsBridgeManager.TAG, "getDisplayInfo error:", e);
        }
    }
}

package android.webview.jsbridge.methods;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webview.jsbridge.MethodCallbackHandler;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ShowAlertDialogHandler extends NoActivityResultMethodHandler{
    public ShowAlertDialogHandler(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public String getMethod() {
        return "showAlertDialog";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        activity.runOnUiThread(() -> {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                callbackHandler.notifySuccessCallback(resultData);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        };
    }
}

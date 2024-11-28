package android.webview.jsbridge;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    public String id; //消息id
    public String type; //消息类型
    public JSONObject body; //消息体内容
    public boolean persistCallback = false; //是否保留Callback，为false时，Web端收到响应消息后就会移除Callback，为true则会保留

    public static Message fromJson(@NonNull String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        Message message = new Message();
        message.id = jsonObject.getString("id");
        message.type = jsonObject.getString("type");
        message.body = jsonObject.getJSONObject("body");
        return message;
    }
}

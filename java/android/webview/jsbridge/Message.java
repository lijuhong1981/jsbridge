package android.webview.jsbridge;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Message {
    public String id; //消息id
    public String type; //消息类型
    public JSONObject body; //消息体内容

    public static Message obtainMessage(String type, JSONObject body) {
        Message message = new Message();
        UUID uuid = UUID.randomUUID();
        message.id = uuid.toString();
        message.type = type;
        message.body = body;
        return message;
    }

    public static Message fromJson(@NonNull String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        Message message = new Message();
        message.id = jsonObject.getString("id");
        message.type = jsonObject.getString("type");
        message.body = jsonObject.getJSONObject("body");
        return message;
    }
}

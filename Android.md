# Android端使用

## 安装

拷贝java目录下代码至android工程app/src/main/java目录下

## 初始化

在Activity的onCreate方法下初始化WebViewBridgeManager

```java
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_webview);
    WebView webView = findViewById(R.id.webView);
    // WebSettingsOptions是对WebView下WebSettings的配置项，WebViewBridgeManager会按照WebSettingsOptions中的参数配置设置WebSettings
    WebSettingsOptions options = new WebSettingsOptions();
    ...
    mManager = new WebViewBridgeManager(this, webView, options);
}
```

在Activity的onActivityResult方法下添加如下代码

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mManager.onActivityResult(requestCode, resultCode, data);
}
```

在Activity的onConfigurationChanged方法下添加如下代码

```java
@Override
public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mManager.onConfigurationChanged(newConfig);
}
```

## 向Web端发送消息

```java
JSONObject body = new JSONObject();
...
Message msg = new Message();
// msg.id = ""; //消息id，不指定则由postMessage函数随机生成
msg.type = "type"; //消息类型
msg.body = body; //消息体内容，JSON对象
mManager.postMessage(msg);
```

## 注册消息接收器，接收Web端发送来的消息

```java
mManager.registerMessageReceiver(new MessageReceiver() {
    @Override
    public void onMessage(Message message, MessageCallbackHandler callbackHandler) {
        // TODO
        ...
        //如果需要回应消息，可使用下面代码
        JSONObject response = new JSONObject();
        ...
        callbackHandler.notifyCallback(response);
    }
});
```

## 注册方法句柄以响应Web端调用的callMethod方法

```java
mManager.setMethodHandler(new MethodHandler() {
    @Override
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler) {
        switch (method) {
            case "methodTest":
                callbackHandler.notifySuccessCallback();
                break;
            ...
            default:
                callbackHandler.notifyErrorCallback(new NoSuchMethodException("Not found the " + method + " method."));
                break;
        }
        //如果方法需要回复，可使用下面代码
        JSONObject result = new JSONObject();
        ...
        callbackHandler.notifySuccessCallback(result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //使用startActivityForResult的响应
        // TODO
    }
});
```

代码中已内置了一个DefaultMethodHandler类，实现了一些常用的方法，WebViewBridgeManager初始化时会默认使用该类，也可继承该类后扩展

```java
mManager.setMethodHandler(new DefaultMethodHandler(WebViewActivity.this){
    @Override
    public void onMethod(String method, JSONObject params, MethodCallbackHandler callbackHandler) {
        super.onMethod(method, params, callbackHandler);
        //TODO
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //TODO
    }
});
```

## 文件上传

代码中已内置了一个DefaultUploadFileHandler类，实现了基础的文件上传，WebViewBridgeManager初始化时会默认使用该类，也可通过WebViewBridgeManager的setUploadFileHandler接口来实现自己的文件上传

```java
mManager.setUploadFileHandler(new UploadFileHandler() {
    @Override
    public void uploadFile(JSONObject params, MethodCallbackHandler callbackHandler) {
        String uploadUrl = params.getString("uploadUrl"); //上传地址
        String filePath = params.getString("filePath"); //文件路径
        JSONObject headers = params.getJSONObject("headers"); //请求头
        // TODO
    }
});
```

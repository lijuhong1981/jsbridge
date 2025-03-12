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

如需要Activity暂停与继续(如手机锁屏、解锁等)事件，请添加如下代码

```java
@Override
protected void onPause() {
    super.onPause();
    mManager.onPause();
}

@Override
protected void onResume() {
    super.onResume();
    mManager.onResume();
}
```

如需要Activity销毁事件，请添加如下代码

```java
@Override
protected void onDestroy() {
    super.onDestroy();
    mManager.onDestroy();
}
```

如需要Activity窗口焦点变化事件，请添加如下代码

```java
@Override
public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    mManager.onWindowFocusChanged(hasFocus);
}
```

如需要屏幕变化事件，如横竖屏切换等，请添加如下代码

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
// TODO 往body中添加数据
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

注册不需要执行onActivityResult的方法
```java
mManager.registerMethodHander(new NoActivityResultMethodHandler(this) {
    @Override
    public String getMethod() {
        return "methodName";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        // TODO
        ...
        // 处理完成，执行回调
        callbackHandler.notifySuccessCallback();
    }
});
```

注册需要执行onActivityResult的方法
```java
mManager.registerMethodHander(new ActivityResultMethodHandler(this) {
    @Override
    public void registerRequestCodes() {
        // 添加onActivityResult的RequestCode，可添加多个
        requestCodes.add(RequestCodes.REQUEST_CAPTURE_IMAGE);
    }

    @Override
    public String getMethod() {
        return "methodName";
    }

    @Override
    public void handleMethod(@NonNull JSONObject params, @NonNull MethodCallbackHandler callbackHandler) {
        // TODO
        ...
        // 调用startActivityForResult
        startActivityForResult(intent, RequestCodes.REQUEST_CAPTURE_IMAGE);
        // 保存callbackHandler
        mCallbackHandler = callbackHandler;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // TODO
            ...
            // 处理完成，执行回调
            mCallbackHandler.notifySuccessCallback(resultData);
        }
    }
});
```

已内置的方法请参见[demo页面](demo/index.html)

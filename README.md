# jsbridge
用于AndroidApp端与Web端相互通讯的小插件

API文档点击这里 [API documentation](API.md).

## 通过文件导入

浏览器直接导入

```html
<script type="text/javascript" src="jsbridge/jsbridge.min.js"></script>
```

模块化导入

```js
// CommonJS:
const jsbridge = require('jsbridge/jsbridge.min.js');

// ES6:
import jsbridge from 'jsbridge/jsbridge.esm.min.js';
```

## 通过npm导入

### 安装

```bash
npm install @lijuhong1981/jsbridge
```

### 导入

```js
// CommonJS:
const jsbridge = require('@lijuhong1981/jsbridge');

// ES6:
import * as jsbridge from '@lijuhong1981/jsbridge';
```

## 使用

### 向App端发送消息

```js
/**
 * 发送消息
 * @param {object} message 发送的消息对象
 * @param {string} message.type 消息类型，必填项
 * @param {string|undefined} message.id 消息id，不填则由程序自动生成
 * @param {object|undefined} message.body 消息体内容对象
 * @param {Function|undefined} message.callback 消息回调函数，如果消息有回应，则通过该函数回调，可不填
 * @returns {string|false} 发送的消息id，为false则说明发送未成功
 */
jsbridge.postMessage({ type, body });
```

### 调用App端方法

```js
/**
 * 调用方法
 * @param {string} method 方法名，必填项
 * @param {object|undefined} params 调用参数，键值对形式，可不填
 * @param {Function|undefined} callback 回调函数，回应该方法的调用结果，可不填
 * @returns {string|false} 发送的消息id，为false则说明调用未成功
 */
jsbridge.callMethod(method, params = {}, (result) => {
    // TODO
});
```

### 接收App端发送的消息

```js
// 注册消息接收
jsbridge.onMessage.addEventListener((message) => {
    console.log("onMessage:", message);
    // TODO
});
```

## 启动demo

```bash
npm run serve
```

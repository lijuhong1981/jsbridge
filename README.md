# jsbridge
用于android端与web端相互通讯的小插件

## 导入

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

### 发送消息

```js
/**
 * 发送消息
 * @param {string} type 消息类型，必填项
 * @param {object|undefined} body 消息体内容，会转换为JSON字符串发送
 * @returns {string} 发送的消息id，为false则说明发送未成功
 */
jsbridge.postMessage(type, body);
```

### 调用方法

```js
/**
 * 调用android端方法
 * @param {string} method 方法名，必填项
 * @param {object|undefined} params 调用参数，键值对形式
 * @param {Function|undefined} callback 回调函数
 * @returns {boolean} 调用是否成功
 */
jsbridge.callMethod(method, params = {}, callback);
```

### 接收消息

```js
// 注册消息监听器
jsbridge.onMessage.addEventListener((message) => {
    console.log("onMessage:", message);
    // TODO
});
```
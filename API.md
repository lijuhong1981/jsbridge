# jsbridge API

Details about the classes, methods, and properties provided by pcircs.

<!--- API BEGIN --->

## Constants

<dl>
<dt><a href="#onMessage">onMessage</a> : <code>EventSubscriber</code></dt>
<dd><p>接收app端发送过来的消息</p>
</dd>
<dt><a href="#onEvent">onEvent</a> : <code>EventEmitter</code></dt>
<dd><p>接收app端发送过来的事件，与onMessage的区别在于onEvent将Message对象的type属性作为事件类型，body属性作为事件对象发送</p>
</dd>
</dl>

## Functions

<dl>
<dt><a href="#checkValid">checkValid()</a> ⇒ <code>boolean</code></dt>
<dd><p>检查当前jsbridge是否可用</p>
</dd>
<dt><a href="#postMessage">postMessage(message, callback)</a> ⇒ <code>string</code> | <code>false</code></dt>
<dd><p>向app端发送消息</p>
</dd>
<dt><a href="#callMethod">callMethod(method, params, callback)</a> ⇒ <code>string</code> | <code>false</code></dt>
<dd><p>调用app端方法</p>
</dd>
</dl>

## Typedefs

<dl>
<dt><a href="#MessageCallback">MessageCallback</a> : <code>function</code></dt>
<dd><p>消息回调通知</p>
</dd>
<dt><a href="#ResultData">ResultData</a> : <code>object</code></dt>
<dd><p>方法执行结果对象定义</p>
</dd>
<dt><a href="#MethodCallback">MethodCallback</a> : <code>function</code></dt>
<dd><p>方法回调通知</p>
</dd>
</dl>

<a name="onMessage"></a>

## onMessage : <code>EventSubscriber</code>
接收app端发送过来的消息

**Kind**: global constant  
**Example**  
```js
onMessage.addEventListener((message) => {});
```
<a name="onEvent"></a>

## onEvent : <code>EventEmitter</code>
接收app端发送过来的事件，与onMessage的区别在于onEvent将Message对象的type属性作为事件类型，body属性作为事件对象发送

**Kind**: global constant  
**Example**  
```js
onEvent.on('eventType', (event) => {});
```
<a name="checkValid"></a>

## checkValid() ⇒ <code>boolean</code>
检查当前jsbridge是否可用

**Kind**: global function  
<a name="postMessage"></a>

## postMessage(message, callback) ⇒ <code>string</code> \| <code>false</code>
向app端发送消息

**Kind**: global function  
**Returns**: <code>string</code> \| <code>false</code> - 发送的消息id，为false则说明发送未成功  

| Param | Type | Description |
| --- | --- | --- |
| message | <code>object</code> | 发送的消息对象 |
| message.type | <code>string</code> | 消息类型，必填项 |
| message.id | <code>string</code> \| <code>undefined</code> | 消息id，不填则由程序自动生成 |
| message.body | <code>object</code> \| <code>undefined</code> | 消息体内容对象 |
| callback | [<code>MessageCallback</code>](#MessageCallback) \| <code>undefined</code> | 消息回调函数，如果消息有回应，则通过该函数回调，可不填 |

<a name="callMethod"></a>

## callMethod(method, params, callback) ⇒ <code>string</code> \| <code>false</code>
调用app端方法

**Kind**: global function  
**Returns**: <code>string</code> \| <code>false</code> - 发送的消息id，为false则说明调用未成功  

| Param | Type | Description |
| --- | --- | --- |
| method | <code>string</code> | 方法名，必填项 |
| params | <code>object</code> \| <code>undefined</code> | 调用参数，键值对形式，可不填 |
| callback | [<code>MethodCallback</code>](#MethodCallback) \| <code>undefined</code> | 回调函数，回应该方法的调用结果，可不填 |

<a name="MessageCallback"></a>

## MessageCallback : <code>function</code>
消息回调通知

**Kind**: global typedef  

| Param | Type | Description |
| --- | --- | --- |
| response | <code>object</code> | 响应内容对象 |

<a name="ResultData"></a>

## ResultData : <code>object</code>
方法执行结果对象定义

**Kind**: global typedef  
**Properties**

| Name | Type | Description |
| --- | --- | --- |
| success | <code>boolean</code> | 是否执行成功 |
| error | <code>object</code> | 执行失败的错误信息 |
| data | <code>object</code> | 执行成功的返回数据 |

<a name="MethodCallback"></a>

## MethodCallback : <code>function</code>
方法回调通知

**Kind**: global typedef  

| Param | Type | Description |
| --- | --- | --- |
| result | [<code>ResultData</code>](#ResultData) | 执行结果对象 |

<!--- API END --->

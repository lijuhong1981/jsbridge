import { v4 as uuidv4 } from 'uuid';
import Check from '@lijuhong1981/jscheck/src/Check.js';
import Event from '@lijuhong1981/jsevents/src/EventSubscriber.js';

const methodCallbacks = {};
/**
 * 接收android端发送过来的消息事件
 * @type {Event} 
*/
const onMessage = new Event();

/**
 * 向android端发送消息
 * @param {string} type 消息类型，必填项
 * @param {object|undefined} body 消息体内容，会转换为JSON字符串发送
 * @param {string|undefined} id 消息id，不填则由程序自动生成 
 * @returns {string} 发送的消息id，为false则说明发送未成功
 */
function postMessageToNavive(type, body = {}, id = uuidv4()) {
    Check.typeOf.string('type', type);
    if (!window.jsbridgeInterface) {
        console.warn('not found window.jsbridgeInterface object');
        return false;
    }
    if (typeof window.jsbridgeInterface.onBridgeMessage !== 'function') {
        console.warn('window.jsbridgeInterface.onBridgeMessage is not function', window.jsbridgeInterface.onBridgeMessage);
        return false;
    }
    const message = {
        type,
        body: JSON.stringify(body),
        id
    };
    const json = JSON.stringify(message);
    console.log('postMessageToNavive:', message);
    window.jsbridgeInterface.onBridgeMessage(json);
    return message.id;
}

/**
 * 调用android端方法
 * @param {string} method 方法名，必填项
 * @param {object|undefined} params 调用参数，键值对形式
 * @param {Function|undefined} callback 回调函数
 * @returns {boolean} 调用是否成功
 */
function callMethod(method, params = {}, callback) {
    Check.typeOf.string('method', method);
    const id = postMessageToNavive("callMethod", {
        method,
        params,
    });
    if (id) {
        if (typeof callback === 'function')
            methodCallbacks[id] = callback;
        return true;
    }
    return false;
}

/**
 * 接收android端向web端发送的消息
 * @param {string} json
 * @returns {void}
 */
function onReceiveMessageFromNative(json) {
    const message = JSON.parse(json);
    console.log("onReceiveMessageFromNative:", message);
    if (message.type === 'methodCallback') {
        const callback = methodCallbacks[message.id];
        if (typeof callback === 'function')
            callback(message.body);
        delete methodCallbacks[message.id];
    } else {
        onMessage.raiseEvent(message);
    }
}

window.onBridgeMessage = onReceiveMessageFromNative;

export {
    onMessage,
    postMessageToNavive as postMessage,
    callMethod,
}
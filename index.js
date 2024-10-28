import { v4 as uuidv4 } from 'uuid';
import Check from '@lijuhong1981/jscheck/src/Check.js';
import Event from '@lijuhong1981/jsevents/src/EventSubscriber.js';

const messageCallbacks = {};
/**
 * 接收android端发送过来的消息事件
 * @type {Event} 
*/
const onReceiveMessage = new Event();

/**
 * 向android端发送消息
 * @param {object} message 发送的消息对象
 * @param {string} message.type 消息类型，必填项
 * @param {string|undefined} message.id 消息id，不填则由程序自动生成
 * @param {object|undefined} message.body 消息体内容对象
 * @param {Function|undefined} message.callback 消息回调函数，如果消息有回应，则通过该函数回调，可不填
 * @returns {string|false} 发送的消息id，为false则说明发送未成功
 */
function postMessageToApp(message) {
    Check.defined('message', message);
    Check.typeOf.string('type', message.type);
    if (!window.jsbridgeInterface) {
        console.warn('not found window.jsbridgeInterface object');
        return false;
    }
    if (typeof window.jsbridgeInterface.onBridgeMessage !== 'function') {
        console.warn('window.jsbridgeInterface.onBridgeMessage is not function', window.jsbridgeInterface.onBridgeMessage);
        return false;
    }
    if (!message.id)
        message.id = uuidv4();
    if (!message.body)
        message.body = {};
    if (typeof message.callback === 'function')
        messageCallbacks[message.id] = message.callback;
    const json = JSON.stringify({
        type: message.type,
        id: message.id,
        body: message.body,
    });
    console.log('postMessageToApp:', json);
    window.jsbridgeInterface.onBridgeMessage(json);
    return message.id;
}

/**
 * 调用android端方法
 * @param {string} method 方法名，必填项
 * @param {object|undefined} params 调用参数，键值对形式
 * @param {Function|undefined} callback 回调函数，可回应该方法的调用结果
 * @returns {string|false} 发送的消息id，为false则说明调用未成功
 */
function callAppMethod(method, params = {}, callback) {
    Check.typeOf.string('method', method);
    return postMessageToApp({
        type: 'callMethod',
        body: {
            method,
            params,
        },
        callback,
    });
}

/**
 * 接收android端向web端发送的消息
 * @param {string} json
 * @returns {void}
 */
function onReceiveMessageFromApp(json) {
    console.log("onReceiveMessageFromApp:", json);
    const message = JSON.parse(json);
    if (message.type === 'messageCallback' || message.type === 'methodCallback') {
        const callback = messageCallbacks[message.id];
        if (typeof callback === 'function' && message.body)
            callback(message.body);
        delete messageCallbacks[message.id];
    } else {
        onReceiveMessage.raiseEvent(message);
    }
}

window.onBridgeMessage = onReceiveMessageFromApp;

export {
    onReceiveMessage as onMessage,
    postMessageToApp as postMessage,
    callAppMethod as callMethod,
}
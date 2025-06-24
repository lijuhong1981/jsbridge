import Check from '@lijuhong1981/jscheck/src/Check.js';
import EventEmitter from '@lijuhong1981/jsevents/src/EventEmitter.js';
import EventSubscriber from '@lijuhong1981/jsevents/src/EventSubscriber.js';
import { v4 as uuidv4 } from 'uuid';

const messageCallbacks = {};

/**
 * 接收app端发送过来的消息
 * @type {EventSubscriber}
 * 
 * @example
 * onMessage.addEventListener((message) => {});
*/
const onMessage = new EventSubscriber();

/**
 * 接收app端发送过来的事件，与onMessage的区别在于onEvent将Message对象的type属性作为事件类型，body属性作为事件对象发送
 * @type {EventEmitter}
 * 
 * @example
 * onEvent.on('eventType', (event) => {});
*/
const onEvent = new EventEmitter();

/**
 * 消息回调通知
 * @callback MessageCallback
 * @param {object} response 响应内容对象
*/

/**
 * 方法执行结果对象定义
 * @typedef {object} ResultData
 * @property {boolean} success 是否执行成功
 * @property {object} error 执行失败的错误信息 
 * @property {object} data 执行成功的返回数据
 */

/**
 * 方法回调通知
 * @callback MethodCallback
 * @param {ResultData} result 执行结果对象
*/

const Priorities = Object.freeze({
    VERBOSE: 2,
    DEBUG: 3,
    INFO: 4,
    WARN: 5,
    ERROR: 6,
    ASSERT: 7,
});

/**
 * 检查当前jsbridge是否可用
 * @returns {boolean}
 */
function checkValid() {
    if (!window.jsbridgeInterface) {
        console.warn('not found the window.jsbridgeInterface object.');
        return false;
    }
    if (typeof window.jsbridgeInterface.onBridgeMessage !== 'function') {
        console.warn('the window.jsbridgeInterface.onBridgeMessage is not function.', window.jsbridgeInterface);
        return false;
    }
    return true;
}

/**
 * 向app端发送消息
 * @param {object} message 发送的消息对象
 * @param {string} message.type 消息类型，必填项
 * @param {string|undefined} message.id 消息id，不填则由程序自动生成
 * @param {object|undefined} message.body 消息体内容对象
 * @param {MessageCallback|undefined} callback 消息回调函数，如果消息有回应，则通过该函数回调，可不填
 * @returns {string|false} 发送的消息id，为false则说明发送未成功
 */
function postMessage(message, callback) {
    Check.defined('message', message);
    Check.typeOf.string('type', message.type);
    if (!checkValid())
        return false;
    if (!message.id)
        message.id = uuidv4();
    if (!message.body)
        message.body = {};
    if (typeof callback === 'function')
        messageCallbacks[message.id] = callback;
    const json = JSON.stringify(message);
    console.log('postMessageToApp:', json);
    window.jsbridgeInterface.onBridgeMessage(json);
    return message.id;
}

/**
 * 调用app端方法
 * @param {string} method 方法名，必填项
 * @param {object|undefined} params 调用参数，键值对形式，可不填
 * @param {MethodCallback|undefined} callback 回调函数，回应该方法的调用结果，可不填
 * @returns {string|false} 发送的消息id，为false则说明调用未成功
 */
function callMethod(method, params = {}, callback) {
    Check.typeOf.string('method', method);
    return postMessage({
        type: 'callMethod',
        body: {
            method,
            params,
        }
    }, callback);
}

/**
 * 接收android端向web端发送的消息
 * @param {string} json
 * @returns {void}
 * @private
 */
function onReceiveMessage(json) {
    console.log("onReceiveMessageFromApp:", json);
    const message = JSON.parse(json);
    if (message.type === 'messageCallback' || message.type === 'methodCallback') {
        const callback = messageCallbacks[message.id];
        if (typeof callback === 'function' && message.body)
            callback(message.body);
        if (!message.persistCallback)
            delete messageCallbacks[message.id];
    } else if (message.type === 'logMessage') {
        const priority = message.body.priority;
        switch (priority) {
            case Priorities.DEBUG:
                message.body.error ? console.debug(message.body.tag, message.body.msg, message.body.error) : console.log(message.body.tag, message.body.msg);
                break;
            case Priorities.INFO:
                message.body.error ? console.info(message.body.tag, message.body.msg, message.body.error) : console.log(message.body.tag, message.body.msg);
                break;
            case Priorities.WARN:
                message.body.error ? console.warn(message.body.tag, message.body.msg, message.body.error) : console.log(message.body.tag, message.body.msg);
                break;
            case Priorities.ERROR:
                message.body.error ? console.error(message.body.tag, message.body.msg, message.body.error) : console.log(message.body.tag, message.body.msg);
                break;
            default:
                message.body.error ? console.log(message.body.tag, message.body.msg, message.body.error) : console.log(message.body.tag, message.body.msg);
                break;
        }
    } else {
        onMessage.raiseEvent(message);
        onEvent.emit(message.type, message.body);
    }
}

window.onBridgeMessage = onReceiveMessage;

export {
    callMethod, checkValid, onEvent, onMessage, postMessage
};

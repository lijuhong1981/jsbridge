(function (global, factory) {
  typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
  typeof define === 'function' && define.amd ? define(['exports'], factory) :
  (global = typeof globalThis !== 'undefined' ? globalThis : global || self, factory(global.jsbridge = {}));
})(this, (function (exports) { 'use strict';

  /**
   * Convert array of 16 byte values to UUID string format of the form:
   * XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
   */
  var byteToHex = [];
  for (var i = 0; i < 256; ++i) {
    byteToHex.push((i + 0x100).toString(16).slice(1));
  }
  function unsafeStringify(arr, offset = 0) {
    // Note: Be careful editing this code!  It's been tuned for performance
    // and works in ways you may not expect. See https://github.com/uuidjs/uuid/pull/434
    //
    // Note to future-self: No, you can't remove the `toLowerCase()` call.
    // REF: https://github.com/uuidjs/uuid/pull/677#issuecomment-1757351351
    return (byteToHex[arr[offset + 0]] + byteToHex[arr[offset + 1]] + byteToHex[arr[offset + 2]] + byteToHex[arr[offset + 3]] + '-' + byteToHex[arr[offset + 4]] + byteToHex[arr[offset + 5]] + '-' + byteToHex[arr[offset + 6]] + byteToHex[arr[offset + 7]] + '-' + byteToHex[arr[offset + 8]] + byteToHex[arr[offset + 9]] + '-' + byteToHex[arr[offset + 10]] + byteToHex[arr[offset + 11]] + byteToHex[arr[offset + 12]] + byteToHex[arr[offset + 13]] + byteToHex[arr[offset + 14]] + byteToHex[arr[offset + 15]]).toLowerCase();
  }

  // Unique ID creation requires a high quality random # generator. In the browser we therefore
  // require the crypto API and do not support built-in fallback to lower quality random number
  // generators (like Math.random()).

  var getRandomValues;
  var rnds8 = new Uint8Array(16);
  function rng() {
    // lazy load so that environments that need to polyfill have a chance to do so
    if (!getRandomValues) {
      // getRandomValues needs to be invoked in a context where "this" is a Crypto implementation.
      getRandomValues = typeof crypto !== 'undefined' && crypto.getRandomValues && crypto.getRandomValues.bind(crypto);
      if (!getRandomValues) {
        throw new Error('crypto.getRandomValues() not supported. See https://github.com/uuidjs/uuid#getrandomvalues-not-supported');
      }
    }
    return getRandomValues(rnds8);
  }

  var randomUUID = typeof crypto !== 'undefined' && crypto.randomUUID && crypto.randomUUID.bind(crypto);
  var native = {
    randomUUID
  };

  function v4(options, buf, offset) {
    if (native.randomUUID && !buf && !options) {
      return native.randomUUID();
    }
    options = options || {};
    var rnds = options.random || (options.rng || rng)();

    // Per 4.4, set bits for version and `clock_seq_hi_and_reserved`
    rnds[6] = rnds[6] & 0x0f | 0x40;
    rnds[8] = rnds[8] & 0x3f | 0x80;
    return unsafeStringify(rnds);
  }

  function isDefined(value) {
      return value !== undefined && value !== null;
  }

  function isValid(value) {
      if (value === undefined || value === null || (typeof value === 'number' && isNaN(value)))
          return false;
      return true;
  }

  const Check = {};

  Check.typeOf = {};

  function getUndefinedErrorMessage(name) {
      return name + " is required, actual value was undefined.";
  }

  function getInvalidErrorMessage(name) {
      return name + " is required, actual value was invalid.";
  }

  function getFailedTypeErrorMessage(actual, expected, name) {
      return (
          "Expected " +
          name +
          " to be typeof " +
          expected +
          ", actual typeof was " +
          actual
      );
  }

  Check.defined = function (name, test) {
      if (!isDefined(test)) {
          throw new Error(getUndefinedErrorMessage(name));
      }
  };

  Check.valid = function (name, test) {
      if (!isValid(test)) {
          throw new Error(getInvalidErrorMessage(name));
      }
  };

  Check.typeOf.func = function (name, test) {
      if (typeof test !== "function") {
          throw new Error(
              getFailedTypeErrorMessage(typeof test, "function", name)
          );
      }
  };

  Check.typeOf.string = function (name, test) {
      if (typeof test !== "string") {
          throw new Error(
              getFailedTypeErrorMessage(typeof test, "string", name)
          );
      }
  };

  Check.typeOf.number = function (name, test) {
      if (typeof test !== "number") {
          throw new Error(
              getFailedTypeErrorMessage(typeof test, "number", name)
          );
      }
  };

  Check.typeOf.number.lessThan = function (name, test, limit) {
      Check.typeOf.number(name, test);
      if (test >= limit) {
          throw new Error(
              "Expected " +
              name +
              " to be less than " +
              limit +
              ", actual value was " +
              test
          );
      }
  };

  Check.typeOf.number.lessThanOrEquals = function (name, test, limit) {
      Check.typeOf.number(name, test);
      if (test > limit) {
          throw new Error(
              "Expected " +
              name +
              " to be less than or equal to " +
              limit +
              ", actual value was " +
              test
          );
      }
  };

  Check.typeOf.number.greaterThan = function (name, test, limit) {
      Check.typeOf.number(name, test);
      if (test <= limit) {
          throw new Error(
              "Expected " +
              name +
              " to be greater than " +
              limit +
              ", actual value was " +
              test
          );
      }
  };

  Check.typeOf.number.greaterThanOrEquals = function (name, test, limit) {
      Check.typeOf.number(name, test);
      if (test < limit) {
          throw new Error(
              "Expected " +
              name +
              " to be greater than or equal to" +
              limit +
              ", actual value was " +
              test
          );
      }
  };

  Check.typeOf.number.equals = function (name1, name2, test1, test2) {
      Check.typeOf.number(name1, test1);
      Check.typeOf.number(name2, test2);
      if (test1 !== test2) {
          throw new Error(
              name1 +
              " must be equal to " +
              name2 +
              ", the actual values are " +
              test1 +
              " and " +
              test2
          );
      }
  };

  Check.typeOf.object = function (name, test) {
      if (typeof test !== "object") {
          throw new Error(
              getFailedTypeErrorMessage(typeof test, "object", name)
          );
      }
  };

  Check.typeOf.bool = function (name, test) {
      if (typeof test !== "boolean") {
          throw new Error(
              getFailedTypeErrorMessage(typeof test, "boolean", name)
          );
      }
  };

  Check.typeOf.array = function (name, test) {
      if (Array.isArray(test) === false) {
          throw new Error(getFailedTypeErrorMessage(typeof test, 'array', name));
      }
  };

  Check.typeOf.integer = function (name, test) {
      if (Number.isSafeInteger(test) === false) {
          throw new Error(getFailedTypeErrorMessage(typeof test, 'integer', name));
      }
  };

  Check.typeOf.integer.lessThan = function (name, test, limit) {
      Check.typeOf.integer(name, test);
      if (test >= limit) {
          throw new Error('Expected ' +
              name +
              ' to be less than ' +
              limit +
              ', actual value was ' +
              test);
      }
  };

  Check.typeOf.integer.lessThanOrEquals = function (name, test, limit) {
      Check.typeOf.integer(name, test);
      if (test > limit) {
          throw new Error('Expected ' +
              name +
              ' to be less than or equal to ' +
              limit +
              ', actual value was ' +
              test);
      }
  };

  Check.typeOf.integer.greaterThan = function (name, test, limit) {
      Check.typeOf.integer(name, test);
      if (test <= limit) {
          throw new Error('Expected ' +
              name +
              ' to be greater than ' +
              limit +
              ', actual value was ' +
              test);
      }
  };

  Check.typeOf.integer.greaterThanOrEquals = function (name, test, limit) {
      Check.typeOf.integer(name, test);
      if (test < limit) {
          throw new Error('Expected ' +
              name +
              ' to be greater than or equal to' +
              limit +
              ', actual value was ' +
              test);
      }
  };

  Check.typeOf.integer.equals = function (name1, name2, test1, test2) {
      Check.typeOf.integer(name1, test1);
      Check.typeOf.integer(name2, test2);
      if (test1 !== test2) {
          throw new Error(
              name1 +
              " must be equal to " +
              name2 +
              ", the actual values are " +
              test1 +
              " and " +
              test2
          );
      }
  };

  Check.instanceOf = function (name, test, target) {
      if (test instanceof target === false) {
          throw new Error(getFailedTypeErrorMessage(false, target.name, name));
      }
  };

  Check.equals = function (name, test, target) {
      Check.isValid(test);
      Check.isValid(target);
      if (test !== target) {
          throw new Error('Expected ' +
              name +
              ' to be equal ' +
              target +
              ', actual value was ' +
              test);
      }
  };

  function destroyHTMLElementImpl(element, deepChildren) {
      if (element.parentNode)
          element.parentNode.removeChild(element);
      if (element instanceof HTMLVideoElement) {
          if (element.hlsPlayer) {
              element.hlsPlayer.destroy();
              delete element.hlsPlayer;
          }
          if (element.flvPlayer) {
              try {
                  element.flvPlayer.unload();
                  element.flvPlayer.detachMediaElement();
              } catch (error) {
                  console.error(error);
              }
              element.flvPlayer.destroy();
              delete element.flvPlayer;
          }
          try {
              element.pause();
              element.loop = false;
              element.removeAttribute('src');
              element.load();
          } catch (error) {
              console.error(error);
          }
      } else if (element instanceof HTMLImageElement) {
          //指向一张空白图片以释放之前的图片
          element.src = "data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=";
      } else if (element instanceof HTMLCanvasElement) {
          //移除apng动画
          if (element.apngPlayer) {
              element.apngPlayer.stop();
              delete element.apngPlayer;
          }
          //移除gif动画
          if (element.gifPlayer) {
              element.gifPlayer.stop();
              delete element.gifPlayer;
          }
          //修改canvas尺寸为0可以释放之前的绘制结果
          element.width = element.height = 0;
      }
  }

  /**
   * 销毁HTMLElement对象
   * @param {HTMLElement} element HTMLElement对象
   * @param {Boolean} deepChildren 是否向下执行深度销毁
   * @returns {void}
   */
  function destroyHTMLElement(element, deepChildren) {
      if (element instanceof HTMLElement === false) {
          console.warn('The element must be instanceof HTMLElement.');
          return;
      }
      destroyHTMLElementImpl(element);
  }

  /**
   * 判断对象是否已被销毁过
   * @param {object} object
   * @returns {Boolean}
   */
  function isDestroyed(object) {
      if (typeof object.isDestroyed !== 'undefined') {
          if (typeof object.isDestroyed === 'function')
              return object.isDestroyed();
          else
              return object.isDestroyed;
      }
      return false;
  }

  function returnTrue() {
      return true;
  }

  /**
   * 销毁一个对象下所有属性和方法
   * 销毁完成后会设置object.isDestroyed = function() { return true; };
   * @param {object} object 销毁对象
   * @param {object} config 销毁配置
   * @param {boolean} config.deleteProperty 是否删除对象属性，默认true
   * @param {Array<string>} config.ignoreProperties 需要忽略的属性数组
   * @param {boolean} config.overwriteFunction 是否覆盖对象方法，默认true
   * @param {boolean} config.releaseArray 是否释放数组内容，默认true，为true时会执行array.length = 0
   * @param {boolean} config.destroyHTMLElement 是否销毁HTMLElement，默认true
   * @param {boolean} config.deep 是否向下执行深度销毁，默认false
   * @returns {void}
   */
  function destroyObject(object, config) {
      if (isDestroyed(object) || object.isDestroying) {
          console.warn('The object isDestroyed or isDestroying, repeated call destroyObject function are not required.', object);
          return;
      }

      config = Object.assign({
          deleteProperty: true,
          ignoreProperties: [],
          overwriteFunction: true,
          releaseArray: true,
          destroyHTMLElement: true,
      }, config);

      config.ignoreProperties.push('isDestroying', 'isDestroyed', 'destroy', 'undeletable');

      //标记正在执行销毁
      object.isDestroying = true;

      function warnOnDestroyed() {
          console.warn('The object isDestroyed, call function is invalid.', object);
      }

      for (const key in object) {
          //过滤属性
          if (config.ignoreProperties) {
              if (config.ignoreProperties.indexOf(key) !== -1)
                  continue;
          }
          try {
              const value = object[key];
              if (value) {
                  //有缓存或受保护标记，不执行销毁
                  if (value.isCached || value.isProtected)
                      continue;
                  if (typeof value === 'function' && config.overwriteFunction === true)
                      object[key] = warnOnDestroyed;
                  else if (Array.isArray(value) && config.releaseArray === true)
                      value.length = 0;
                  else if (value instanceof HTMLElement && config.destroyHTMLElement === true)
                      destroyHTMLElement(object[key]);
                  //如果value是对象且deep为true
                  else if (typeof value === 'object' && config.deep === true) {
                      if (typeof value.destroy === 'function')
                          value.destroy();
                      else
                          destroyObject(value, deep);
                  }
                  if (value.undeletable) //有不可删除标记，不执行delete操作
                      continue;
              }
          } catch (error) {
              console.warn(error);
          }
          if (config.deleteProperty === true)
              delete object[key];
      }

      object.isDestroyed = returnTrue;

      delete object.isDestroying;

      return object;
  }

  /**
   * Destroyable是一个包含了destroy相关Property的class，可用于继承
   * 
   * @abstract
   * @class
  */
  class Destroyable {
      isDestroyed() {
          return false;
      }

      /**
       * 执行销毁，由子类实现
       * @abstract
       */
      onDestroy(...args) {
          // console.warn('onDestroy must be overwrited by subclass.');
      }

      /**
       * 销毁自身
       * @returns {this}
       */
      destroy(...args) {
          if (this.isDestroyed()) {
              console.warn('This object was destroyed.', this);
          } else if (this.isDestroying) {
              console.warn('This object is destroying.', this);
          } else {
              this.onDestroy(...args);
              destroyObject(this);
          }
          return this;
      }
  }

  /**
   * A generic utility class for managing subscribers for a particular event. This class is usually instantiated inside of a container class and exposed as a property for others to subscribe to.
  */
  class EventSubscriber extends Destroyable {
      constructor() {
          super();
          this._listenersMap = new Map();
      }

      /**
       * 事件监听器数量
       * @returns {Number}
       */
      get numberOfListeners() {
          return this._listenersMap.size;
      }

      /**
       * 获取所有的事件监听器
       * @returns {Array<Function>}
       */
      get listeners() {
          const result = [];
          const listeners = this._listenersMap.values();
          for (const listener of listeners) {
              result.push(listener.callback);
          }
          return result;
      }

      /**
       * 判断事件监听器是否已存在
       * @param {Function} callback 事件监听回调函数
       * @returns {boolean}
       */
      hasEventListener(callback) {
          return this._listenersMap.has(callback);
      }

      /**
       * 添加事件监听
       * @param {Function} callback 回调函数
       * @param {object} options 事件配置项，可不填
       * @param {object} options.scope 回调函数<code>this</code>指针对象，可不填
       * @param {boolean} options.once 是否单次事件，可不填
       * @returns {Function} 移除函数，调用该函数可直接移除事件监听
       */
      addEventListener(callback, options = {}) {
          Check.typeOf.func('callback', callback);

          let listener = this._listenersMap.get(callback);

          if (!listener) {
              listener = {
                  callback: callback,
                  options: options,
                  removeFunc: () => {
                      this.removeEventListener(callback);
                  }
              };
              this._listenersMap.set(callback, listener);
          } else {
              listener.options = options;
          }

          return listener.removeFunc;
      }

      /**
       * 移除事件监听
       * @param {Function} callback 回调函数
       * @returns {boolean}
       */
      removeEventListener(callback) {
          Check.typeOf.func('callback', callback);

          return this._listenersMap.delete(callback);
      }

      /**
       * @see addEventListener
      */
      on(callback, options) {
          return this.addEventListener(callback, options);
      }

      /**
       * @see removeEventListener
      */
      off(callback) {
          return this.removeEventListener(callback);
      }

      /**
       * 添加一次事件监听
       * @param {Function} callback 回调函数
       * @param {object} scope 回调函数<code>this</code>指针对象，可不填
       * @returns {Function} 移除函数，调用该函数可直接移除事件监听
       * 
       * @see addEventListener
       */
      once(callback, scope) {
          return this.addEventListener(callback, {
              once: true,
              scope: scope,
          });
      }

      /**
       * 清除所有事件监听
       * @returns {this}
       */
      clear() {
          this._listenersMap.clear();
          return this;
      }

      /**
       * 发送事件
       * @param {...any} ...args 事件参数
       * @returns {this}
       */
      raiseEvent(...args) {
          const listeners = this._listenersMap.values();
          for (const listener of listeners) {
              const options = listener.options;
              const callback = listener.callback;
              callback.apply(options.scope, arguments);
              if (options.once) {
                  this._listenersMap.delete(callback);
              }
          }

          return this;
      }

      /**
       * 执行销毁
       */
      onDestroy() {
          this.clear();
      }
  }

  const messageCallbacks = {};
  /**
   * 接收android端发送过来的消息事件
   * @type {Event} 
  */
  const onReceiveMessage = new EventSubscriber();

  /**
   * 向android端发送消息
   * @param {object} message 发送的消息对象
   * @param {string} message.type 消息类型，必填项
   * @param {string|undefined} message.id 消息id，不填则由程序自动生成
   * @param {object|undefined} message.body 消息体内容对象
   * @param {Function|undefined} message.callback 消息回调函数，如果该消息有回应，则通过该函数回调，可不填
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
          message.id = v4();
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

  exports.callMethod = callAppMethod;
  exports.onMessage = onReceiveMessage;
  exports.postMessage = postMessageToApp;

}));
//# sourceMappingURL=jsbridge.js.map

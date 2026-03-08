
/**
 * debounce.js
 * 
 * 提供防抖功能，用于限制函数在指定时间内的执行频率。
 */

/**
 * 创建一个防抖函数，该函数会从上一次被调用后，延迟 wait 毫秒后调用 func 方法
 *
 * @param {Function} func 要防抖的函数
 * @param {number} wait 需要延迟的毫秒数
 * @param {boolean} immediate 是否立即执行
 * @returns {Function} 返回一个防抖函数
 */
export function debounce(func, wait, immediate = false) {
  let timeout, result;

  const debounced = function(...args) {
    const context = this;
    if (timeout) clearTimeout(timeout);

    if (immediate) {
      const callNow = !timeout;
      timeout = setTimeout(() => {
        timeout = null;
      }, wait);
      if (callNow) result = func.apply(context, args);
    } else {
      timeout = setTimeout(() => {
        func.apply(context, args);
      }, wait);
    }
    return result;
  };

  debounced.cancel = function() {
    clearTimeout(timeout);
    timeout = null;
  };

  return debounced;
}

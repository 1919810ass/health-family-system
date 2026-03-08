/**
 * 前端模块：particles.js
 * 说明：一个用于创建点击粒子效果的 Vue 指令。
 * 通过对象池模式进行了性能优化，以实现流畅的动画效果。
 */

// 定义粒子颜色数组
const colors = ['#7B61FF', '#00D2BA', '#FFB84C', '#FF5C5C', '#5D8BF4'];
// 定义粒子池和池的大小
const particlePool = [];
const poolSize = 30;

/**
 * 预填充粒子池，创建可复用的粒子元素。
 */
function prefillParticlePool() {
  for (let i = 0; i < poolSize; i++) {
    const particle = document.createElement('span');
    particle.style.position = 'fixed';
    particle.style.width = '8px';
    particle.style.height = '8px';
    particle.style.borderRadius = '50%';
    particle.style.pointerEvents = 'none';
    particle.style.zIndex = '9999';
    particle.style.display = 'none'; // 初始时隐藏
    document.body.appendChild(particle);
    particlePool.push({ element: particle, inUse: false });
  }
}

/**
 * 激活一个粒子，设置其初始状态并启动动画。
 * @param {number} x - 粒子的初始 x 坐标。
 * @param {number} y - 粒子的初始 y 坐标。
 */
function activateParticle(x, y) {
  // 从池中查找一个未被使用的粒子
  const particleWrapper = particlePool.find(p => !p.inUse);
  if (!particleWrapper) return; // 如果池中没有可用粒子，则不执行任何操作

  particleWrapper.inUse = true;
  const particle = particleWrapper.element;

  // 重置粒子样式
  particle.style.display = 'block';
  particle.style.left = `${x}px`;
  particle.style.top = `${y}px`;
  particle.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
  particle.style.opacity = 1;
  particle.style.transform = 'scale(1)';

  // 为粒子设置随机的运动轨迹
  const angle = Math.random() * Math.PI * 2;
  const velocity = 2 + Math.random() * 4;
  const vx = Math.cos(angle) * velocity;
  const vy = Math.sin(angle) * velocity;

  let life = 0;
  const maxLife = 40 + Math.random() * 20; // 粒子的生命周期

  function animate() {
    if (!particleWrapper.inUse) return; // 如果粒子已被回收，则停止动画

    life++;
    const progress = life / maxLife;
    const currentOpacity = 1 - progress;
    const currentScale = 1 - progress;

    // 更新粒子的位置和样式
    const currentX = x + vx * life;
    const currentY = y + vy * life;
    particle.style.opacity = currentOpacity;
    particle.style.transform = `translate(${currentX - x}px, ${currentY - y}px) scale(${currentScale})`;

    // 如果粒子生命周期未结束，则继续下一帧动画
    if (life < maxLife) {
      requestAnimationFrame(animate);
    } else {
      // 动画结束，将粒子回收至池中
      particle.style.display = 'none';
      particleWrapper.inUse = false;
    }
  }

  requestAnimationFrame(animate);
}

// 预填充粒子池
prefillParticlePool();

export default {
  mounted(el) {
    // 点击时激活多个粒子
    el.addEventListener('click', (e) => {
      const x = e.clientX;
      const y = e.clientY;
      for (let i = 0; i < 12; i++) {
        activateParticle(x, y);
      }
    });
  },
  // 在组件卸载时清理事件监听器（可选，但推荐）
  unmounted(el) {
    // 如果需要，可以在这里移除事件监听器，但由于我们没有存储监听器引用，
    // 这一步在此实现中被省略。对于现代浏览器，当元素被移除时，
    // 垃圾回收机制通常能处理好内存释放。
  }
};

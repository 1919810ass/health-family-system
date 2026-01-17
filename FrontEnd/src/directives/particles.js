const colors = ['#7B61FF', '#00D2BA', '#FFB84C', '#FF5C5C', '#5D8BF4'];

export default {
  mounted(el) {
    el.addEventListener('click', (e) => {
      const rect = el.getBoundingClientRect();
      // Use cursor position if available, otherwise center of element
      const x = e.clientX;
      const y = e.clientY;

      for (let i = 0; i < 12; i++) {
        createParticle(x, y);
      }
    });
  }
};

function createParticle(x, y) {
  const particle = document.createElement('span');
  particle.style.position = 'fixed';
  particle.style.left = x + 'px';
  particle.style.top = y + 'px';
  particle.style.width = '8px';
  particle.style.height = '8px';
  particle.style.borderRadius = '50%';
  particle.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
  particle.style.pointerEvents = 'none';
  particle.style.zIndex = '9999';
  
  // Random velocity
  const angle = Math.random() * Math.PI * 2;
  const velocity = 2 + Math.random() * 4;
  const vx = Math.cos(angle) * velocity;
  const vy = Math.sin(angle) * velocity;
  
  document.body.appendChild(particle);

  let opacity = 1;
  let posX = x;
  let posY = y;

  const animate = () => {
    opacity -= 0.02;
    posX += vx;
    posY += vy;

    particle.style.opacity = opacity;
    particle.style.transform = `translate(${posX - x}px, ${posY - y}px) scale(${opacity})`;

    if (opacity > 0) {
      requestAnimationFrame(animate);
    } else {
      particle.remove();
    }
  };

  requestAnimationFrame(animate);
}

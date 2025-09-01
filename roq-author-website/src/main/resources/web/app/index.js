import './style.scss';

// Mobile navbar burger toggle
document.addEventListener('DOMContentLoaded', () => {
  const burgers = Array.from(document.querySelectorAll('.navbar-burger'));
  burgers.forEach(burger => {
    burger.addEventListener('click', () => {
      const targetId = burger.dataset.target;
      const target = document.getElementById(targetId);
      burger.classList.toggle('is-active');
      if (target) target.classList.toggle('is-active');
    });
  });
});

console.log('Roq Author Website loaded');
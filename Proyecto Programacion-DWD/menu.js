document.addEventListener('DOMContentLoaded', () => {
    const toggle = document.querySelector('.menu-toggle');
    const nav = document.getElementById('mainNav');

    if (toggle && nav) {
        toggle.addEventListener('click', () => {
            const isOpen = nav.classList.toggle('open');
            toggle.classList.toggle('is-open', isOpen);
            toggle.setAttribute('aria-expanded', String(isOpen));
        });
    }

    document.addEventListener('keydown', (event) => {
        const activeElement = document.activeElement;
        const isTyping = ['INPUT', 'SELECT', 'TEXTAREA'].includes(activeElement?.tagName) || activeElement?.isContentEditable;
        const arrows = ['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'];

        if (isTyping || !arrows.includes(event.key)) {
            return;
        }

        const distance = 90;
        const top = event.key === 'ArrowUp' ? -distance : event.key === 'ArrowDown' ? distance : 0;
        const left = event.key === 'ArrowLeft' ? -distance : event.key === 'ArrowRight' ? distance : 0;

        event.preventDefault();
        window.scrollBy({ top, left, behavior: 'smooth' });
    });
});

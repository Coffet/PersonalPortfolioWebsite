/* Desk login — password visibility toggle + "where's my password" dialog.
   Kept separate from cms.js so the public /sign-in page loads nothing extra. */
(function () {
    'use strict';

    function init() {
        var toggles = document.querySelectorAll('[data-pw-toggle]');
        Array.prototype.forEach.call(toggles, function (btn) {
            btn.addEventListener('click', function () {
                var input = document.getElementById(btn.getAttribute('data-pw-toggle'));
                if (!input) {
                    return;
                }
                var show = input.type === 'password';
                input.type = show ? 'text' : 'password';
                btn.setAttribute('aria-pressed', String(show));
                btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
                btn.classList.toggle('is-on', show);
                if (document.activeElement === btn) {
                    input.focus();
                }
            });
        });

        var dialog = document.getElementById('forgot-pw-dialog');
        var trigger = document.getElementById('forgot-pw-trigger');
        if (dialog && trigger) {
            trigger.addEventListener('click', function () {
                if (typeof dialog.showModal === 'function') {
                    dialog.showModal();
                } else {
                    dialog.setAttribute('open', '');
                }
            });

            Array.prototype.forEach.call(dialog.querySelectorAll('[data-dialog-close]'), function (btn) {
                btn.addEventListener('click', function () {
                    dialog.close();
                });
            });

            dialog.addEventListener('click', function (event) {
                if (event.target === dialog) {
                    dialog.close();
                }
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

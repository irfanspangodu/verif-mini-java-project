/**
 * ==========================================================================
 * VERIF - Application Client Scripts & Screen Loader Controller (< 3 KB)
 * ==========================================================================
 * BUGFIX: the full-page loader used to appear for EVERY Ajax request,
 * including the 6s/8s background polling on status.xhtml and
 * admin/dashboard.xhtml, which flashed a blocking overlay over the UI and
 * made the page momentarily unclickable. Background polls are now executed
 * through verifPoll(), which marks the request as silent so the overlay is
 * only shown for user-initiated actions.
 * ==========================================================================
 */

(function () {
  'use strict';

  // Number of in-flight *silent* (background) Ajax requests
  var silentAjaxDepth = 0;

  // Retrieve screen loader DOM element
  function getLoader() {
    return document.getElementById('verif-screen-loader');
  }

  // Display the full-page screen loader
  window.showLoader = function (customMessage) {
    var loader = getLoader();
    if (loader) {
      if (customMessage) {
        var msgEl = loader.querySelector('.loader-status-text');
        if (msgEl) msgEl.textContent = customMessage;
      }
      loader.classList.add('active');
    }
  };

  // Hide the full-page screen loader
  window.hideLoader = function () {
    var loader = getLoader();
    if (loader) {
      loader.classList.remove('active');
    }
  };

  /**
   * Mobile / Tablet navigation drawer toggler.
   * Activates the hamburger-style nav drawer that is visible only at
   * screen widths of 1024 px or below (tablets + handheld sizes).
   *
   * @param {HTMLElement} toggleBtn the <button class="nav-toggle"> that was clicked
   */
  window.toggleNav = function (toggleBtn) {
    var drawerId = toggleBtn.getAttribute('aria-controls');
    var drawer = drawerId ? document.getElementById(drawerId)
                          : document.getElementById('navDrawer');
    if (!drawer) return;

    var isOpen = drawer.classList.toggle('nav-open');
    toggleBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');

    if (isOpen) {
      // Auto-focus the first nav link when the drawer opens for accessibility
      var firstLink = drawer.querySelector('.nav-link');
      if (firstLink) {
        setTimeout(function () { firstLink.focus({ preventScroll: true }); }, 80);
      }

      // Close drawer when clicking outside the navbar (mobile UX)
      document.addEventListener('click', closeNavOnClickOutside, false);

      // Close drawer on Escape key press
      document.addEventListener('keydown', closeNavOnEscape, false);
    } else {
      detachNavCloseHandlers();
    }
  };

  // --- private helper functions for nav-drawer closing -----------

  function detachNavCloseHandlers() {
    document.removeEventListener('click', closeNavOnClickOutside, false);
    document.removeEventListener('keydown', closeNavOnEscape, false);
  }

  function closeNavOnClickOutside(evt) {
    var navbar = evt.target.closest('.navbar');
    if (!navbar) {
      var drawer = document.getElementById('navDrawer');
      var toggle = document.querySelector('.nav-toggle');
      if (drawer && drawer.classList.contains('nav-open') && toggle) {
        drawer.classList.remove('nav-open');
        toggle.setAttribute('aria-expanded', 'false');
      }
      detachNavCloseHandlers();
    }
  }

  function closeNavOnEscape(evt) {
    if (evt.key === 'Escape' || evt.keyCode === 27) {
      var drawer = document.getElementById('navDrawer');
      var toggle = document.querySelector('.nav-toggle');
      if (drawer && drawer.classList.contains('nav-open') && toggle) {
        drawer.classList.remove('nav-open');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.focus({ preventScroll: true });
      }
      detachNavCloseHandlers();
    }
  }

  /**
   * Triggers a background (silent) Ajax poll. The screen loader stays hidden
   * and polling is skipped entirely while the browser tab is not visible.
   *
   * @param {string} clientId JSF client id of the hidden poll button
   */
  window.verifPoll = function (clientId) {
    var btn = document.getElementById(clientId);
    if (!btn || document.hidden) {
      return;
    }
    silentAjaxDepth++;
    btn.click();
  };

  // Register JSF Ajax lifecycle event listener
  function registerJsfAjaxListener() {
    if (typeof jsf !== 'undefined' && jsf.ajax && jsf.ajax.addOnEvent) {
      jsf.ajax.addOnEvent(function (data) {
        if (data.status === 'begin') {
          // Only surface the loader for user-initiated requests
          if (silentAjaxDepth === 0) {
            window.showLoader('Synchronizing verification state...');
          }
        } else if (data.status === 'complete') {
          // 'complete' fires exactly once per request (success or error)
          if (silentAjaxDepth > 0) {
            silentAjaxDepth--;
          }
          if (silentAjaxDepth === 0) {
            setTimeout(function () {
              window.hideLoader();
            }, 180);
          }
        }
      });
    }
  }

  // Initialize loader hooks on DOM load
  document.addEventListener('DOMContentLoaded', function () {
    registerJsfAjaxListener();

    // Attach to standard (non-Ajax) form submissions only
    var forms = document.querySelectorAll('form:not([data-no-loader])');
    forms.forEach(function (form) {
      if (form.offsetParent === null) {
        // Hidden helper forms must never trigger the overlay
        return;
      }
      form.addEventListener('submit', function () {
        window.showLoader('Securing application data...');
      });
    });

    // Make sure nav drawer is closed initially on all sizes
    var drawer = document.getElementById('navDrawer');
    var toggle = document.querySelector('.nav-toggle');
    if (drawer) drawer.classList.remove('nav-open');
    if (toggle) toggle.setAttribute('aria-expanded', 'false');

    // Ensure loader is hidden initially
    window.hideLoader();
  });

  // Re-check after window fully loads
  window.addEventListener('load', function () {
    window.hideLoader();
  });

  // Safety net: if an Ajax cycle is interrupted, never leave the overlay stuck
  window.addEventListener('pageshow', function () {
    silentAjaxDepth = 0;
    window.hideLoader();
  });

})();

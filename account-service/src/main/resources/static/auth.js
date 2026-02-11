(function () {
  var TOKEN_KEY = 'banking_token';
  var ACCOUNT_API = '';

  window.getToken = function () {
    return localStorage.getItem(TOKEN_KEY);
  };

  window.setToken = function (token) {
    localStorage.setItem(TOKEN_KEY, token);
  };

  window.clearToken = function () {
    localStorage.removeItem(TOKEN_KEY);
    window.currentUser = null;
  };

  window.getAuthHeaders = function () {
    var t = getToken();
    if (!t) return {};
    return { 'Authorization': 'Bearer ' + t };
  };

  window.authFetch = function (url, options) {
    options = options || {};
    options.headers = Object.assign({}, options.headers || {}, getAuthHeaders());
    return fetch(url, options);
  };

  window.showApp = function (user) {
    window.currentUser = user;
    document.getElementById('auth-screen').style.display = 'none';
    document.getElementById('app-screen').style.display = 'block';
    var info = document.getElementById('user-info');
    if (info) {
      info.textContent = user.customerName || user.username;
      if (user.role === 'ADMIN') info.textContent += ' (Admin)';
    }
    if (typeof window.onAuthReady === 'function') window.onAuthReady(user);
  };

  window.showLogin = function () {
    window.currentUser = null;
    document.getElementById('auth-screen').style.display = 'block';
    document.getElementById('app-screen').style.display = 'none';
    document.getElementById('form-activate').style.display = 'none';
    document.getElementById('form-login').style.display = 'block';
  };

  window.checkAuth = function () {
    var token = getToken();
    if (!token) {
      showLogin();
      return Promise.resolve(null);
    }
    return authFetch(ACCOUNT_API + '/api/auth/me')
      .then(function (r) {
        if (r.ok) return r.json();
        clearToken();
        showLogin();
        return null;
      })
      .then(function (user) {
        if (user) showApp(user);
        return user;
      })
      .catch(function () {
        showLogin();
        return null;
      });
  };

  document.getElementById('form-login').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var btn = this.querySelector('button[type=submit]');
    btn.disabled = true;
    fetch(ACCOUNT_API + '/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: fd.get('username'), password: fd.get('password') })
    })
      .then(function (r) {
        if (!r.ok) return r.json().then(function (d) { throw new Error(d.message || d.error || 'Login failed'); }).catch(function () { throw new Error('Invalid username or password'); });
        return r.json();
      })
      .then(function (data) {
        setToken(data.token);
        return authFetch(ACCOUNT_API + '/api/auth/me').then(function (r) { return r.json(); });
      })
      .then(function (user) { showApp(user); })
      .catch(function (err) {
        alert(err.message || 'Login failed');
      })
      .finally(function () { btn.disabled = false; });
  });

  document.getElementById('btn-show-activate').addEventListener('click', function () {
    document.getElementById('form-login').style.display = 'none';
    document.getElementById('form-activate').style.display = 'block';
  });

  document.getElementById('btn-back-login').addEventListener('click', function () {
    document.getElementById('form-activate').style.display = 'none';
    document.getElementById('form-login').style.display = 'block';
  });

  document.getElementById('form-activate').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var btn = this.querySelector('button[type=submit]');
    btn.disabled = true;
    fetch(ACCOUNT_API + '/api/auth/activate', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerId: parseInt(fd.get('customerId'), 10),
        username: fd.get('username').trim(),
        password: fd.get('password')
      })
    })
      .then(function (r) {
        if (!r.ok) return r.json().then(function (d) { throw new Error(d.message || d.error || 'Activation failed'); }).catch(function () { throw new Error('Activation failed'); });
        return r.json();
      })
      .then(function (data) {
        setToken(data.token);
        return authFetch(ACCOUNT_API + '/api/auth/me').then(function (r) { return r.json(); });
      })
      .then(function (user) { showApp(user); })
      .catch(function (err) {
        alert(err.message || 'Activation failed');
      })
      .finally(function () { btn.disabled = false; });
  });

  document.getElementById('btn-logout').addEventListener('click', function () {
    clearToken();
    showLogin();
  });

  checkAuth();
})();

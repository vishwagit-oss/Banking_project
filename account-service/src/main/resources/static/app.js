(function () {
  var ACCOUNT_API = '';
  // Use same origin so account-service proxies to transaction-service (avoids "Failed to fetch" from direct 8082)
  var TX_API = '';
  var apiFetch = function (url, opts) { return (window.authFetch || fetch)(url, opts); };

  window.onAuthReady = function (user) {
    var modeSwitch = document.querySelector('.mode-switch');
    var phaseCustomer = document.getElementById('phase-customer');
    var phaseAdmin = document.getElementById('phase-admin');
    if (user.role === 'ADMIN') {
      modeSwitch.style.display = 'flex';
      phaseAdmin.classList.add('active');
      phaseCustomer.classList.remove('active');
      document.querySelector('.mode-btn[data-mode="admin"]').classList.add('active');
      document.querySelector('.mode-btn[data-mode="customer"]').classList.remove('active');
      loadCustomers();
      loadCustomerOptions();
    } else {
      modeSwitch.style.display = 'none';
      phaseCustomer.classList.add('active');
      phaseAdmin.classList.remove('active');
      document.querySelector('.customer-login').style.display = 'none';
      var listEl = document.getElementById('customer-account-list');
      var block = document.getElementById('customer-my-accounts');
      var tabs = document.querySelector('.customer-tabs');
      var panels = document.querySelector('.customer-panels');
      block.style.display = 'block';
      if (user.customerId) {
        (window.authFetch || fetch)(ACCOUNT_API + '/api/customers/' + user.customerId + '/accounts')
          .then(function (r) { if (!r.ok) throw new Error(); return r.json(); })
          .then(function (accounts) {
            if (!accounts || accounts.length === 0) {
              listEl.innerHTML = '<p class="list-empty">No accounts yet.</p>';
            } else {
              listEl.innerHTML = accounts.map(function (a) {
                var num = (a.accountNumber != null && String(a.accountNumber).trim() !== '') ? a.accountNumber : (a.id != null ? 'ID ' + a.id : '—');
                return '<div class="list-item"><strong>Account number</strong>: <code>' + escapeHtml(num) + '</code><br>Balance: ' + a.balance + ' ' + (a.currency || 'USD') + ' · ' + (a.type || '') + ' · ' + (a.status || '') + '</div>';
              }).join('');
            }
            if (accounts && accounts.length > 0) {
              tabs.style.display = 'flex';
              panels.style.display = 'block';
            }
          })
          .catch(function () {
            listEl.innerHTML = '<p class="list-empty">Could not load accounts.</p>';
          });
      } else {
        listEl.innerHTML = '<p class="list-empty">No customer linked. Contact support.</p>';
      }
    }
  };

  function showToast(message, type) {
    var el = document.getElementById('toast');
    el.textContent = message;
    el.className = 'toast show ' + (type || 'success');
    clearTimeout(window._toastId);
    window._toastId = setTimeout(function () { el.classList.remove('show'); }, 3500);
  }

  function escapeHtml(s) {
    if (s == null) return '';
    var div = document.createElement('div');
    div.textContent = s;
    return div.innerHTML;
  }

  // ---------- Mode switch: Customer vs Admin ----------
  document.querySelectorAll('.mode-btn').forEach(function (btn) {
    btn.addEventListener('click', function () {
      document.querySelectorAll('.mode-btn').forEach(function (b) { b.classList.remove('active'); });
      this.classList.add('active');
      var mode = this.dataset.mode;
      document.querySelectorAll('.phase').forEach(function (p) { p.classList.remove('active'); });
      document.getElementById('phase-' + mode).classList.add('active');
      if (mode === 'admin') {
        loadCustomers();
        loadCustomerOptions();
      }
    });
  });

  // ---------- CUSTOMER PHASE ----------
  document.getElementById('form-customer-login').addEventListener('submit', function (e) {
    e.preventDefault();
    var customerId = document.querySelector('#form-customer-login input[name="customerId"]').value.trim();
    if (!customerId) return;
    (window.authFetch || fetch)(ACCOUNT_API + '/api/customers/' + customerId + '/accounts')
      .then(function (r) {
        if (!r.ok) throw new Error('Customer not found or no accounts');
        return r.json();
      })
      .then(function (accounts) {
        var listEl = document.getElementById('customer-account-list');
        var block = document.getElementById('customer-my-accounts');
        var tabs = document.querySelector('.customer-tabs');
        var panels = document.querySelector('.customer-panels');
        if (!accounts || accounts.length === 0) {
          listEl.innerHTML = '<p class="list-empty">No accounts found for this Customer ID. Ask the bank to open an account.</p>';
        } else {
          listEl.innerHTML = accounts.map(function (a) {
            var num = (a.accountNumber != null && String(a.accountNumber).trim() !== '') ? a.accountNumber : (a.id != null ? 'ID ' + a.id : '—');
            return '<div class="list-item"><strong>Account number</strong>: <code>' + escapeHtml(num) + '</code><br>' +
              'Balance: ' + a.balance + ' ' + (a.currency || 'USD') + ' · ' + (a.type || '') + ' · ' + (a.status || '') + '</div>';
          }).join('');
        }
        block.style.display = 'block';
        if (accounts && accounts.length > 0) {
          tabs.style.display = 'flex';
          panels.style.display = 'block';
        }
      })
      .catch(function (err) {
        showToast(err.message || 'Customer not found', 'error');
      });
  });

  function customerTabSwitch(tabName) {
    document.querySelectorAll('.customer-tabs .tab').forEach(function (t) {
      t.classList.toggle('active', t.dataset.tab === tabName);
    });
    document.querySelectorAll('.customer-panels .panel').forEach(function (p) {
      p.classList.toggle('active', p.id === 'panel-' + tabName);
    });
  }
  document.querySelectorAll('.customer-tabs .tab').forEach(function (t) {
    t.addEventListener('click', function () { customerTabSwitch(t.dataset.tab); });
  });

  function getErrorMessage(d, r) {
    if (!d) return r.statusText || ('Error ' + r.status);
    var msg = d.message || d.error || d.detail || (typeof d === 'string' ? d : null);
    if (msg) return String(msg);
    if (d.errors && Array.isArray(d.errors) && d.errors.length > 0) {
      var first = d.errors[0];
      msg = (first.defaultMessage || first.message || first.field) ? (first.field + ': ' + (first.defaultMessage || first.message)) : JSON.stringify(first);
      if (msg) return msg;
    }
    return r.statusText || ('Error ' + r.status);
  }

  document.getElementById('form-c-transfer').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var fromAcc = (fd.get('fromAccountNumber') || '').trim();
    var toAcc = (fd.get('toAccountNumber') || '').trim();
    var amountNum = parseFloat(fd.get('amount'));
    if (!fromAcc || !toAcc) { showToast('Please enter from and to account numbers.', 'error'); return; }
    if (!Number.isFinite(amountNum) || amountNum < 0.01) { showToast('Please enter a valid amount (at least 0.01).', 'error'); return; }
    var payload = { fromAccountNumber: fromAcc, toAccountNumber: toAcc, amount: amountNum, reference: (fd.get('reference') || '').trim() || null };
    apiFetch(TX_API + '/api/transactions/transfer', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      .then(function (r) {
        if (!r.ok) {
          return r.text().then(function (text) {
            var d = null;
            if (text) { try { d = JSON.parse(text); } catch (e) {} }
            throw new Error(getErrorMessage(d, r));
          });
        }
        return r.json();
      })
      .then(function () { showToast('Transfer completed.'); this.reset(); }.bind(this))
      .catch(function (err) { showToast('Transfer failed: ' + (err.message || 'Check account numbers and balance.'), 'error'); });
  });

  document.getElementById('form-c-deposit').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var action = (e.submitter && e.submitter.value) || fd.get('action') || 'deposit';
    var accNum = (fd.get('accountNumber') || '').trim();
    var amountNum = parseFloat(fd.get('amount'));
    if (!accNum) { showToast('Please enter account number.', 'error'); return; }
    if (!Number.isFinite(amountNum) || amountNum < 0.01) { showToast('Please enter a valid amount (at least 0.01).', 'error'); return; }
    var url = TX_API + '/api/transactions/' + action + '?accountNumber=' + encodeURIComponent(accNum) + '&amount=' + amountNum + (fd.get('reference') ? '&reference=' + encodeURIComponent(fd.get('reference')) : '');
    apiFetch(url, { method: 'POST' })
      .then(function (r) {
        if (!r.ok) return r.json().then(function (d) { throw new Error(getErrorMessage(d, r)); }).catch(function () { throw new Error(getErrorMessage(null, r)); });
        return r.json();
      })
      .then(function () { showToast(action === 'deposit' ? 'Deposit completed.' : 'Withdrawal completed.'); this.reset(); }.bind(this))
      .catch(function (err) { showToast(err.message || (action === 'withdraw' ? 'Withdraw failed.' : 'Deposit failed.'), 'error'); });
  });

  document.getElementById('form-c-history').addEventListener('submit', function (e) {
    e.preventDefault();
    var accountNumber = document.querySelector('#form-c-history input[name="accountNumber"]').value.trim();
    if (!accountNumber) return;
    var list = document.getElementById('c-history-list');
    list.innerHTML = 'Loading…';
    apiFetch(TX_API + '/api/transactions/history/' + encodeURIComponent(accountNumber))
      .then(function (r) {
        if (!r.ok) return r.json().then(function (d) { throw new Error(getErrorMessage(d, r)); }).catch(function () { throw new Error(getErrorMessage(null, r)); });
        return r.json();
      })
      .then(function (data) {
        if (!data || !Array.isArray(data)) { list.innerHTML = '<p class="list-empty">' + (data && data.message ? data.message : 'No transactions.') + '</p>'; return; }
        if (data.length === 0) { list.innerHTML = '<p class="list-empty">No transactions.</p>'; return; }
        list.innerHTML = data.map(function (t) {
          var from = t.fromAccountNumber || '—', to = t.toAccountNumber || '—';
          return '<div class="list-item"><strong>' + t.type + '</strong> ' + t.amount + (t.reference ? ' · ' + escapeHtml(t.reference) : '') + '<br><small>' + from + ' → ' + to + ' · ' + (t.createdAt ? new Date(t.createdAt).toLocaleString() : '') + ' · ' + t.status + '</small></div>';
        }).join('');
      })
      .catch(function (err) { list.innerHTML = '<p class="list-empty">' + escapeHtml(err.message) + '</p>'; });
  });

  // ---------- ADMIN: Find customer's accounts (account numbers) ----------
  document.getElementById('form-admin-customer-accounts').addEventListener('submit', function (e) {
    e.preventDefault();
    var customerId = document.querySelector('#form-admin-customer-accounts input[name="customerId"]').value.trim();
    if (!customerId) return;
    var list = document.getElementById('admin-customer-accounts-list');
    list.innerHTML = 'Loading…';
    (window.authFetch || fetch)(ACCOUNT_API + '/api/customers/' + customerId + '/accounts')
      .then(function (r) {
        if (!r.ok) throw new Error('Customer not found');
        return r.json();
      })
      .then(function (accounts) {
        if (!accounts || accounts.length === 0) {
          list.innerHTML = '<p class="list-empty">No accounts for this customer.</p>';
        } else {
          list.innerHTML = accounts.map(function (a) {
            var num = (a.accountNumber != null && String(a.accountNumber).trim() !== '') ? a.accountNumber : (a.id != null ? 'ID ' + a.id : '—');
            return '<div class="list-item"><strong>Account number</strong>: <code>' + escapeHtml(num) + '</code><br>Balance: ' + a.balance + ' ' + (a.currency || 'USD') + ' · ' + (a.type || '') + ' · ' + (a.status || '') + '</div>';
          }).join('');
        }
      })
      .catch(function (err) { list.innerHTML = '<p class="list-empty">' + err.message + '</p>'; });
  });

  // ---------- ADMIN: Tabs ----------
  function tabSwitch() {
    var tab = this;
    document.querySelectorAll('#phase-admin .tab').forEach(function (t) { t.classList.toggle('active', t === tab); });
    document.querySelectorAll('#phase-admin .panel').forEach(function (p) { p.classList.toggle('active', p.id === 'panel-' + tab.dataset.tab); });
  }
  document.querySelectorAll('#phase-admin .tab').forEach(function (t) {
    t.addEventListener('click', tabSwitch.bind(t));
  });

  // ---------- ADMIN: Customers ----------
  function loadCustomers() {
    (window.authFetch || fetch)(ACCOUNT_API + '/api/customers')
      .then(function (r) { return r.json(); })
      .then(function (data) {
        var list = document.getElementById('customer-list');
        if (!data || data.length === 0) { list.innerHTML = '<p class="list-empty">No customers yet.</p>'; return; }
        list.innerHTML = data.map(function (c) {
          return '<div class="list-item"><strong>' + escapeHtml(c.name) + '</strong><br>' + escapeHtml(c.email) + ' · ' + escapeHtml(c.phone) + ' <small>(Customer ID: ' + c.id + ')</small></div>';
        }).join('');
      })
      .catch(function () { document.getElementById('customer-list').innerHTML = '<p class="list-empty">Could not load. Is Account Service running?</p>'; });
  }

  function loadCustomerOptions() {
    (window.authFetch || fetch)(ACCOUNT_API + '/api/customers')
      .then(function (r) { return r.json(); })
      .then(function (data) {
        var sel = document.querySelector('#form-account select[name="customerId"]');
        if (!sel) return;
        var cur = sel.value;
        sel.innerHTML = '<option value="">Select customer</option>' + (data || []).map(function (c) {
          return '<option value="' + c.id + '">' + escapeHtml(c.name) + ' (ID ' + c.id + ')</option>';
        }).join('');
        if (cur) sel.value = cur;
      })
      .catch(function () {});
  }

  document.getElementById('form-customer').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    (window.authFetch || fetch)(ACCOUNT_API + '/api/customers', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name: fd.get('name'), email: fd.get('email'), phone: fd.get('phone') }) })
      .then(function (r) {
        if (!r.ok) {
          return r.json().catch(function () { return r.text(); }).then(function (body) {
            var msg = (body && body.message) ? body.message : (typeof body === 'string' ? body : 'Failed');
            throw new Error(msg);
          });
        }
        return r.json();
      })
      .then(function () { showToast('Customer created.'); this.reset(); loadCustomers(); loadCustomerOptions(); }.bind(this))
      .catch(function (err) {
        showToast(err.message || 'Failed', 'error');
        loadCustomers();
        loadCustomerOptions();
      });
  });

  // ---------- ADMIN: Accounts ----------
  document.getElementById('form-lookup-account').addEventListener('submit', function (e) {
    e.preventDefault();
    var num = document.querySelector('#form-lookup-account input[name="accountNumber"]').value.trim();
    if (!num) return;
    var list = document.getElementById('account-list');
    list.innerHTML = 'Loading…';
    (window.authFetch || fetch)(ACCOUNT_API + '/api/accounts/number/' + encodeURIComponent(num))
      .then(function (r) { if (!r.ok) throw new Error('Account not found'); return r.json(); })
      .then(function (acc) {
        var num = (acc.accountNumber != null && String(acc.accountNumber).trim() !== '') ? acc.accountNumber : (acc.id != null ? 'ID ' + acc.id : '—');
        list.innerHTML = '<div class="list-item"><strong>' + escapeHtml(num) + '</strong><br>Balance: ' + acc.balance + ' ' + (acc.currency || 'USD') + '<br>Type: ' + (acc.type || '') + ' · Status: ' + (acc.status || '') + '<br><small>Customer ID: ' + acc.customerId + '</small></div>';
      })
      .catch(function () { list.innerHTML = '<p class="list-empty">Account not found.</p>'; });
  });

  document.getElementById('form-account').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var payload = { customerId: parseInt(fd.get('customerId'), 10), type: fd.get('type'), initialBalance: parseFloat(fd.get('initialBalance')) || 0, currency: (fd.get('currency') || 'USD').trim().toUpperCase().slice(0, 3) };
    (window.authFetch || fetch)(ACCOUNT_API + '/api/accounts', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      .then(function (r) {
        if (!r.ok) return r.json().then(function (d) { throw new Error(d && d.message ? d.message : 'Failed'); }).catch(function () { return r.text().then(function (t) { throw new Error(t || 'Failed'); }); });
        return r.json();
      })
      .then(function (acc) { showToast('Account created: ' + acc.accountNumber); loadCustomerOptions(); })
      .catch(function (err) { showToast(err.message || 'Failed', 'error'); });
  });

  // ---------- ADMIN: Transfer & Deposit ----------
  document.getElementById('form-transfer').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var fromAcc = (fd.get('fromAccountNumber') || '').trim();
    var toAcc = (fd.get('toAccountNumber') || '').trim();
    var amountNum = parseFloat(fd.get('amount'));
    if (!fromAcc || !toAcc) { showToast('Please enter from and to account numbers.', 'error'); return; }
    if (!Number.isFinite(amountNum) || amountNum < 0.01) { showToast('Please enter a valid amount (at least 0.01).', 'error'); return; }
    var payload = { fromAccountNumber: fromAcc, toAccountNumber: toAcc, amount: amountNum, reference: (fd.get('reference') || '').trim() || null };
    apiFetch(TX_API + '/api/transactions/transfer', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
      .then(function (r) {
        if (!r.ok) {
          return r.text().then(function (text) {
            var d = null;
            if (text) { try { d = JSON.parse(text); } catch (e) {} }
            throw new Error(getErrorMessage(d, r));
          });
        }
        return r.json();
      })
      .then(function () { showToast('Transfer completed.'); this.reset(); }.bind(this))
      .catch(function (err) { showToast('Transfer failed: ' + (err.message || 'Check account numbers and balance.'), 'error'); });
  });

  document.getElementById('form-deposit').addEventListener('submit', function (e) {
    e.preventDefault();
    var fd = new FormData(this);
    var action = (e.submitter && e.submitter.value) || fd.get('action') || 'deposit';
    var accNum = (fd.get('accountNumber') || '').trim();
    var amountNum = parseFloat(fd.get('amount'));
    if (!accNum) { showToast('Please enter account number.', 'error'); return; }
    if (!Number.isFinite(amountNum) || amountNum < 0.01) { showToast('Please enter a valid amount (at least 0.01).', 'error'); return; }
    var url = TX_API + '/api/transactions/' + action + '?accountNumber=' + encodeURIComponent(accNum) + '&amount=' + amountNum + (fd.get('reference') ? '&reference=' + encodeURIComponent(fd.get('reference')) : '');
    apiFetch(url, { method: 'POST' })
      .then(function (r) { if (!r.ok) return r.json().then(function (d) { throw new Error(getErrorMessage(d, r)); }).catch(function () { throw new Error(getErrorMessage(null, r)); }); return r.json(); })
      .then(function () { showToast(action === 'deposit' ? 'Done.' : 'Done.'); this.reset(); }.bind(this))
      .catch(function (err) { showToast(err.message || (action === 'withdraw' ? 'Withdraw failed.' : 'Deposit failed.'), 'error'); });
  });

  document.getElementById('form-history').addEventListener('submit', function (e) {
    e.preventDefault();
    var accountNumber = document.querySelector('#form-history input[name="accountNumber"]').value.trim();
    if (!accountNumber) return;
    var list = document.getElementById('history-list');
    list.innerHTML = 'Loading…';
    apiFetch(TX_API + '/api/transactions/history/' + encodeURIComponent(accountNumber))
      .then(function (r) { if (!r.ok) return r.json().then(function (d) { throw new Error(getErrorMessage(d, r)); }).catch(function () { throw new Error(getErrorMessage(null, r)); }); return r.json(); })
      .then(function (data) {
        if (!data || !Array.isArray(data)) { list.innerHTML = '<p class="list-empty">' + (data && data.message ? data.message : 'No transactions.') + '</p>'; return; }
        if (data.length === 0) { list.innerHTML = '<p class="list-empty">No transactions.</p>'; return; }
        list.innerHTML = data.map(function (t) {
          var from = t.fromAccountNumber || '—', to = t.toAccountNumber || '—';
          return '<div class="list-item"><strong>' + t.type + '</strong> ' + t.amount + (t.reference ? ' · ' + escapeHtml(t.reference) : '') + '<br><small>' + from + ' → ' + to + ' · ' + (t.createdAt ? new Date(t.createdAt).toLocaleString() : '') + ' · ' + t.status + '</small></div>';
        }).join('');
      })
      .catch(function (err) { list.innerHTML = '<p class="list-empty">' + escapeHtml(err.message) + '</p>'; });
  });
})();

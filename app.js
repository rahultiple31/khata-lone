const seedCustomers = [
  { id: 1, name: 'Rohan Mehta', phone: '98765 12098', balance: 4250, updated: 'Today, 10:42 AM', color: '#6869d9' },
  { id: 2, name: 'Priya Sharma', phone: '98122 43610', balance: -1800, updated: 'Yesterday, 6:20 PM', color: '#ec806d' },
  { id: 3, name: 'Sameer Khan', phone: '99876 54321', balance: 3120, updated: 'Yesterday, 2:15 PM', color: '#39aa83' },
  { id: 4, name: 'Neha Provision Store', phone: '97654 21890', balance: 2350, updated: '22 Jun, 11:30 AM', color: '#5b91d2' },
  { id: 5, name: 'Vikram Singh', phone: '98230 77442', balance: 1000, updated: '20 Jun, 4:05 PM', color: '#bf72c8' },
  { id: 6, name: 'Meera Joshi', phone: '99001 88221', balance: -650, updated: '18 Jun, 9:16 AM', color: '#df9b51' }
];

const seedTransactions = [
  { id: 1, customerId: 1, type: 'credit', amount: 750, note: 'Grocery purchase', date: 'Today, 10:42 AM' },
  { id: 2, customerId: 3, type: 'payment', amount: 1200, note: 'Part payment', date: 'Yesterday, 2:15 PM' },
  { id: 3, customerId: 2, type: 'credit', amount: 800, note: 'Stock supplies', date: 'Yesterday, 11:20 AM' },
  { id: 4, customerId: 4, type: 'payment', amount: 500, note: 'Cash received', date: '22 Jun, 11:30 AM' }
];

const translations = {
  en: {
    home: 'Home',
    customers: 'Customers',
    reports: 'Reports',
    reminders: 'Reminders',
    settings: 'Settings',
    goodMorning: 'Good morning',
    overviewCopy: 'Here’s how your business is doing today.',
    addCustomer: 'Add customer',
    youWillReceive: 'You will receive',
    youWillPay: 'You will pay',
    netBalance: 'Net balance',
    recentCustomers: 'Recent customers',
    viewAll: 'View all',
    quickActions: 'Quick actions',
    addEntry: 'Add entry',
    sendReminder: 'Send reminder',
    viewReports: 'View reports',
    backupData: 'Backup data'
  },
  hi: {
    home: 'होम',
    customers: 'ग्राहक',
    reports: 'रिपोर्ट',
    reminders: 'रिमाइंडर',
    settings: 'सेटिंग्स',
    goodMorning: 'सुप्रभात',
    overviewCopy: 'आज आपके व्यवसाय का हाल यहाँ है।',
    addCustomer: 'ग्राहक जोड़ें',
    youWillReceive: 'आपको मिलेगा',
    youWillPay: 'आपको देना है',
    netBalance: 'कुल बैलेंस',
    recentCustomers: 'हाल के ग्राहक',
    viewAll: 'सभी देखें',
    quickActions: 'त्वरित कार्य',
    addEntry: 'एंट्री जोड़ें',
    sendReminder: 'रिमाइंडर भेजें',
    viewReports: 'रिपोर्ट देखें',
    backupData: 'बैकअप करें'
  },
  mr: {
    home: 'होम',
    customers: 'ग्राहक',
    reports: 'अहवाल',
    reminders: 'आठवण',
    settings: 'सेटिंग्ज',
    goodMorning: 'शुभ सकाळ',
    overviewCopy: 'आज तुमचा व्यवसाय कसा चालला आहे ते पहा.',
    addCustomer: 'ग्राहक जोडा',
    youWillReceive: 'तुम्हाला मिळेल',
    youWillPay: 'तुम्हाला द्यायचे',
    netBalance: 'एकूण शिल्लक',
    recentCustomers: 'अलीकडील ग्राहक',
    viewAll: 'सर्व पहा',
    quickActions: 'जलद कृती',
    addEntry: 'नोंद जोडा',
    sendReminder: 'आठवण पाठवा',
    viewReports: 'अहवाल पहा',
    backupData: 'बॅकअप घ्या'
  }
};

const defaultState = {
  customers: seedCustomers,
  transactions: seedTransactions,
  language: 'en',
  dark: false,
  shopName: 'Aarav Stores'
};

let customerFilter = 'all';
let entryType = 'credit';
let reminderChannel = 'WhatsApp';
let state = loadState();
let toastTimer;
let currentUser = JSON.parse(localStorage.getItem('hisably-user') || 'null');

const $ = selector => document.querySelector(selector);
const $$ = selector => [...document.querySelectorAll(selector)];
const money = amount => `${amount < 0 ? '-' : ''}₹${Math.abs(Number(amount) || 0).toLocaleString('en-IN')}`;
const save = () => localStorage.setItem('hisably-state', JSON.stringify(state));

function loadState(){
  try{
    const saved = JSON.parse(localStorage.getItem('hisably-state') || 'null');
    return {
      ...defaultState,
      ...(saved || {}),
      customers: Array.isArray(saved?.customers) ? saved.customers.map(normalizeCustomer) : seedCustomers,
      transactions: Array.isArray(saved?.transactions) ? saved.transactions.map(normalizeTransaction) : seedTransactions,
      language: translations[saved?.language] ? saved.language : 'en',
      dark: Boolean(saved?.dark),
      shopName: saved?.shopName || 'Aarav Stores'
    };
  }catch(error){
    console.warn('Could not load saved ledger data, starting fresh.', error);
    return { ...defaultState };
  }
}

function normalizeCustomer(customer, index){
  return {
    id: Number(customer.id) || Date.now() + index,
    name: String(customer.name || 'Unnamed Customer'),
    phone: String(customer.phone || '').replace(/\D/g, '').replace(/(\d{5})(\d{0,5})/, '$1 $2').trim(),
    balance: Number(customer.balance) || 0,
    updated: String(customer.updated || 'Just now'),
    color: customer.color || ['#6869d9', '#ec806d', '#39aa83', '#5b91d2', '#bf72c8'][index % 5]
  };
}

function normalizeTransaction(transaction, index){
  return {
    id: Number(transaction.id) || Date.now() + index,
    customerId: Number(transaction.customerId),
    type: transaction.type === 'payment' ? 'payment' : 'credit',
    amount: Number(transaction.amount) || 0,
    note: String(transaction.note || ''),
    date: String(transaction.date || 'Just now')
  };
}

function escapeHtml(value = ''){
  return String(value).replace(/[&<>"']/g, char => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[char]));
}

function initials(name = ''){
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map(part => part[0] || '')
    .join('')
    .toUpperCase() || 'NA';
}

function render(){
  const receive = state.customers.filter(customer => customer.balance > 0).reduce((total, customer) => total + customer.balance, 0);
  const pay = Math.abs(state.customers.filter(customer => customer.balance < 0).reduce((total, customer) => total + customer.balance, 0));

  $('#receiveTotal').textContent = money(receive);
  $('#payTotal').textContent = money(pay);
  $('#netTotal').textContent = money(receive - pay);
  $('#receiveCustomers').textContent = `From ${state.customers.filter(customer => customer.balance > 0).length} customers`;
  $('#payCustomers').textContent = `To ${state.customers.filter(customer => customer.balance < 0).length} customers`;

  renderCustomers();
  renderTransactions();
  renderReminders();
  renderReports();
  populateSelects();
  applyLanguage();
  renderAuth();

  document.body.classList.toggle('dark', state.dark);
  ['#sideShopName', '#topShopName', '#settingsShopName'].forEach(selector => {
    $(selector).textContent = state.shopName;
  });
  $('#shopNameInput').value = state.shopName;
}

function renderAuth(){
  const authArea = $('#authArea');
  const profileTop = $('#profileTopBtn');
  const signInBtn = $('#signInBtn');
  const signUpBtn = $('#signUpBtn');
  if(currentUser){
    signInBtn.style.display = 'none';
    signUpBtn.style.display = 'none';
    profileTop.style.display = 'flex';
    $('#userAvatar').textContent = initials(currentUser.name || (currentUser.id || '').toString().slice(0,2));
    $('#userRole').textContent = currentUser.role || 'Member';
    // Enforce supervisor date lock in entry modal
    const entryDateEl = $('#entryDate');
    if(entryDateEl){
      const today = new Date().toISOString().slice(0,10);
      entryDateEl.value = today;
      entryDateEl.disabled = (currentUser.role === 'Supervisor');
    }
  } else {
    signInBtn.style.display = '';
    signUpBtn.style.display = '';
    profileTop.style.display = 'none';
    const entryDateEl = $('#entryDate'); if(entryDateEl){ entryDateEl.disabled = false; }
  }
}

function signIn(user){
  currentUser = user;
  localStorage.setItem('hisably-user', JSON.stringify(currentUser));
  closeModals();
  toast(`Signed in as ${currentUser.role}`);
  render();
}

function signOut(){
  currentUser = null;
  localStorage.removeItem('hisably-user');
  toast('Signed out');
  render();
}

function signUp(account){
  // basic local signup (no backend)
  signIn(account);
}

function openCalendarModal(){
  openModal('calendarModal');
  $('#historyDate').value = new Date().toISOString().slice(0,10);
  renderHistory();
}

function renderHistory(){
  const date = $('#historyDate').value;
  if(!date){ $('#historyList').innerHTML = '<p>Select a date to view transactions.</p>'; return; }
  const items = state.transactions.filter(t => (t.dateFull || '').startsWith(date) || (t.createdAt && new Date(t.createdAt).toISOString().slice(0,10) === date));
  if(!items.length) { $('#historyList').innerHTML = '<div class="empty-state inline"><span>✓</span><h3>No transactions on this date</h3></div>'; return; }
  $('#historyList').innerHTML = items.map(t => {
    const c = state.customers.find(cu => cu.id === t.customerId) || { name: 'Unknown' };
    return `<div style="padding:8px 0;border-bottom:1px solid var(--line)"><strong>${escapeHtml(c.name)}</strong><div style="font-size:12px;color:var(--muted)">${escapeHtml(t.note || '')} • ${escapeHtml(t.date || '')}</div><div style="margin-top:6px">${t.type === 'credit' ? '+' : '−'} ${money(t.amount)}</div></div>`;
  }).join('');
}

function downloadHistoryCSV(){
  const date = $('#historyDate').value;
  const items = state.transactions.filter(t => (t.dateFull || '').startsWith(date) || (t.createdAt && new Date(t.createdAt).toISOString().slice(0,10) === date));
  const rows = [['Customer','Type','Amount','Note','Date']];
  items.forEach(t => {
    const c = state.customers.find(cu => cu.id === t.customerId) || { name: 'Unknown' };
    rows.push([c.name, t.type, t.amount, (t.note || '').replace(/\n/g,' '), t.date || '']);
  });
  const csv = rows.map(r => r.map(cell => '"'+String(cell).replace(/"/g,'""')+'"').join(',')).join('\n');
  const blob = new Blob([csv], { type: 'text/csv' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = `transactions-${date}.csv`;
  a.click();
  URL.revokeObjectURL(a.href);
  toast('CSV download prepared');
}

function customerRow(customer){
  const type = customer.balance >= 0 ? 'receive' : 'pay';
  return `
    <div class="customer-row" data-customer-id="${customer.id}">
      <span class="customer-avatar" style="background:${customer.color}">${initials(customer.name)}</span>
      <span class="customer-info">
        <strong>${escapeHtml(customer.name)}</strong>
        <small>+91 ${escapeHtml(customer.phone)} • ${escapeHtml(customer.updated)}</small>
      </span>
      <span class="customer-amount ${type}">
        <strong>${money(customer.balance)}</strong>
        <small>${type === 'receive' ? 'You will receive' : 'You will pay'}</small>
      </span>
      <span class="row-arrow">›</span>
    </div>`;
}

function renderCustomers(){
  $('#recentCustomerList').innerHTML = state.customers.slice(0, 4).map(customerRow).join('');

  const query = (($('#customerSearch')?.value || $('#globalSearch')?.value || '')).toLowerCase();
  const filtered = state.customers.filter(customer => {
    const matchesSearch = customer.name.toLowerCase().includes(query) || customer.phone.includes(query);
    const matchesFilter = customerFilter === 'all' || (customerFilter === 'receive' ? customer.balance >= 0 : customer.balance < 0);
    return matchesSearch && matchesFilter;
  });

  $('#allCustomerList').innerHTML = filtered.map(customerRow).join('');
  $('#emptyCustomers').style.display = filtered.length ? 'none' : 'block';
}

function renderTransactions(){
  $('#transactionList').innerHTML = state.transactions
    .slice()
    .reverse()
    .slice(0, 8)
    .map(transaction => {
      const customer = state.customers.find(item => item.id === transaction.customerId);
      if(!customer) return '';
      const symbol = transaction.type === 'credit' ? '↗' : '↙';
      const sign = transaction.type === 'credit' ? '+' : '−';
      return `
        <div class="transaction-row ${transaction.type}">
          <span>${symbol}</span>
          <div>
            <strong>${escapeHtml(customer.name)}</strong>
            <small>${escapeHtml(transaction.note || 'Ledger entry')} • ${escapeHtml(transaction.date)}</small>
          </div>
          <b>${sign}${money(transaction.amount)}</b>
        </div>`;
    })
    .join('');
}

function renderReports(){
  const credit = state.transactions.filter(transaction => transaction.type === 'credit').reduce((total, transaction) => total + transaction.amount, 0);
  const collected = state.transactions.filter(transaction => transaction.type === 'payment').reduce((total, transaction) => total + transaction.amount, 0);

  $('#reportCollected').textContent = money(collected);
  $('#reportCredit').textContent = money(credit);
  $('#reportNet').textContent = money(collected - credit);

  const values = [[40, 62], [58, 48], [47, 76], [75, 55], [65, 88], [80, 71], [48, 79]];
  const days = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];
  $('#reportChart').innerHTML = values.map((value, index) => `
    <div class="chart-group">
      <i class="credit-bar" style="height:${value[0]}%"></i>
      <i class="payment-bar" style="height:${value[1]}%"></i>
      <small>${days[index]}</small>
    </div>`).join('');
}

function renderReminders(){
  const dueCustomers = state.customers.filter(customer => customer.balance > 0);
  $('#pendingReminderCount').textContent = dueCustomers.length;
  $('#reminderCount').textContent = Math.min(dueCustomers.length, 9);

  $('#reminderList').innerHTML = dueCustomers.length
    ? dueCustomers.map(customer => `
      <div class="reminder-row">
        <span class="customer-avatar" style="background:${customer.color}">${initials(customer.name)}</span>
        <span><strong>${escapeHtml(customer.name)}</strong><small>+91 ${escapeHtml(customer.phone)}</small></span>
        <b>${money(customer.balance)} due</b>
        <button class="remind-btn" data-remind-id="${customer.id}">Send reminder</button>
      </div>`).join('')
    : '<div class="empty-state inline"><span>✓</span><h3>No pending dues</h3><p>There are no reminder-ready customers right now.</p></div>';
}

function populateSelects(){
  $('#entryCustomer').innerHTML = state.customers
    .map(customer => `<option value="${customer.id}">${escapeHtml(customer.name)} — ${money(customer.balance)}</option>`)
    .join('');

  const reminderOptions = state.customers
    .filter(customer => customer.balance > 0)
    .map(customer => `<option value="${customer.id}">${escapeHtml(customer.name)} — ${money(customer.balance)} due</option>`)
    .join('');

  $('#reminderCustomer').innerHTML = reminderOptions;
  $('#reminderCustomer').disabled = !reminderOptions;
  $('#reminderForm button[type="submit"]').disabled = !reminderOptions;
  updateReminderMessage();
}

function applyLanguage(){
  const text = translations[state.language] || translations.en;
  $$('[data-i18n]').forEach(element => {
    if(text[element.dataset.i18n]) element.textContent = text[element.dataset.i18n];
  });
  $('#currentLanguage').textContent = { en: 'English', hi: 'हिन्दी', mr: 'मराठी' }[state.language] || 'English';
  $$('[data-lang]').forEach(button => {
    button.querySelector('i').textContent = button.dataset.lang === state.language ? '✓' : '';
  });
}

function navigate(page){
  const target = $(`#${page}Page`);
  if(!target) return;
  $$('.page').forEach(section => section.classList.remove('active'));
  target.classList.add('active');
  $$('[data-page]').forEach(button => button.classList.toggle('active', button.dataset.page === page));
  window.scrollTo({ top: 0, behavior: 'smooth' });
  if(innerWidth < 721) $('#searchOverlay').classList.remove('open');
}

function openModal(id){
  const modal = $(`#${id}`);
  if(!modal) return;
  $('#modalBackdrop').classList.add('open');
  modal.classList.add('open');
  setTimeout(() => modal.querySelector('input, select, textarea')?.focus(), 150);
}

function openReminderModal(){
  if(!state.customers.some(customer => customer.balance > 0)){
    toast('No pending dues to remind right now');
    return;
  }
  openModal('reminderModal');
}

function closeModals(){
  $('#modalBackdrop').classList.remove('open');
  $$('.modal').forEach(modal => modal.classList.remove('open'));
}

function toast(message){
  clearTimeout(toastTimer);
  $('#toast p').textContent = message;
  $('#toast').classList.add('show');
  toastTimer = setTimeout(() => $('#toast').classList.remove('show'), 2600);
}

function updateReminderMessage(){
  const customer = state.customers.find(item => item.id === Number($('#reminderCustomer').value));
  $('#reminderMessage').value = customer
    ? `Namaste ${customer.name}, a friendly reminder that ${money(customer.balance)} is pending at ${state.shopName}. Please pay when convenient. Thank you!`
    : 'No pending payment reminders right now.';
}

function backup(){
  const blob = new Blob([JSON.stringify(state, null, 2)], { type: 'application/json' });
  const anchor = document.createElement('a');
  anchor.href = URL.createObjectURL(blob);
  anchor.download = `hisably-backup-${new Date().toISOString().slice(0, 10)}.json`;
  anchor.click();
  URL.revokeObjectURL(anchor.href);
  toast('Your ledger backup is ready');
}

function pdf(){
  window.print();
  toast('Print dialog opened — choose “Save as PDF”');
}

$$('[data-page]').forEach(button => button.addEventListener('click', () => navigate(button.dataset.page)));

$$('[data-action]').forEach(button => button.addEventListener('click', () => {
  const action = button.dataset.action;
  if(action === 'add-customer') openModal('customerModal');
  if(action === 'add-entry') openModal('entryModal');
  if(action === 'send-reminder') openReminderModal();
  if(action === 'backup') backup();
  if(action === 'pdf') pdf();
  if(action === 'edit-profile') openModal('profileModal');
  if(action === 'otp') toast('OTP security is enabled for your mobile');
}));

$$('[data-close]').forEach(button => button.addEventListener('click', closeModals));
$('#modalBackdrop').addEventListener('click', closeModals);
document.addEventListener('keydown', event => {
  if(event.key === 'Escape') closeModals();
});

$('#customerForm').addEventListener('submit', event => {
  event.preventDefault();
  const name = $('#customerName').value.trim();
  const phone = $('#customerPhone').value.replace(/\D/g, '');
  if(!name || phone.length !== 10){
    toast('Enter a valid customer name and 10-digit mobile number');
    return;
  }

  const colors = ['#6869d9', '#ec806d', '#39aa83', '#5b91d2', '#bf72c8'];
  state.customers.unshift({
    id: Date.now(),
    name,
    phone: phone.replace(/(\d{5})(\d{5})/, '$1 $2'),
    balance: 0,
    updated: 'Just now',
    color: colors[state.customers.length % colors.length]
  });

  save();
  render();
  closeModals();
  event.target.reset();
  toast('Customer account created');
});

$$('[data-entry-type]').forEach(button => button.addEventListener('click', () => {
  $$('[data-entry-type]').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  entryType = button.dataset.entryType;
}));

$('#entryForm').addEventListener('submit', event => {
  event.preventDefault();
  const id = Number($('#entryCustomer').value);
  const amount = Number($('#entryAmount').value);
  const customer = state.customers.find(item => item.id === id);
  const dateVal = ($('#entryDate').value || new Date().toISOString().slice(0,10));

  if(!customer || !amount || amount < 1){
    toast('Choose a customer and enter a valid amount');
    return;
  }

  // Supervisor restriction: only current date updates
  if(currentUser && currentUser.role === 'Supervisor'){
    const today = new Date().toISOString().slice(0,10);
    if(dateVal !== today){
      toast('Supervisor can only record entries for the current date');
      return;
    }
  }

  if(entryType === 'credit') customer.balance += amount;
  else customer.balance -= amount;
  customer.updated = 'Just now';

  const createdAt = Date.now();
  const dateIso = dateVal;
  state.transactions.push({
    id: createdAt,
    customerId: id,
    type: entryType,
    amount,
    note: $('#entryNote').value.trim(),
    date: new Date(createdAt).toLocaleString(),
    dateFull: dateIso,
    createdAt
  });

  save();
  render();
  closeModals();
  event.target.reset();
  toast(`Entry saved for ${customer.name}`);
});

$$('[data-channel]').forEach(button => button.addEventListener('click', () => {
  $$('[data-channel]').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  reminderChannel = button.dataset.channel;
}));

$('#reminderCustomer').addEventListener('change', updateReminderMessage);

$('#reminderForm').addEventListener('submit', event => {
  event.preventDefault();
  const customer = state.customers.find(item => item.id === Number($('#reminderCustomer').value));
  if(!customer){
    toast('No pending dues to remind right now');
    return;
  }
  closeModals();
  toast(`${reminderChannel} reminder prepared for ${customer.name}`);
});

$('#reminderList').addEventListener('click', event => {
  const button = event.target.closest('[data-remind-id]');
  if(!button) return;
  $('#reminderCustomer').value = button.dataset.remindId;
  updateReminderMessage();
  openModal('reminderModal');
});

$('#customerSearch').addEventListener('input', renderCustomers);
$$('[data-filter]').forEach(button => button.addEventListener('click', () => {
  $$('[data-filter]').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  customerFilter = button.dataset.filter;
  renderCustomers();
}));

$('#searchToggle').addEventListener('click', () => {
  $('#searchOverlay').classList.add('open');
  $('#globalSearch').focus();
});

$('#closeSearch').addEventListener('click', () => {
  $('#searchOverlay').classList.remove('open');
  $('#globalSearch').value = '';
  renderCustomers();
});

$('#globalSearch').addEventListener('input', () => {
  navigate('customers');
  renderCustomers();
});

$('#themeToggle').addEventListener('click', () => {
  state.dark = !state.dark;
  save();
  render();
  toast(state.dark ? 'Dark mode on' : 'Light mode on');
});

$('[data-setting="language"]').addEventListener('click', () => openModal('languageModal'));

$$('[data-lang]').forEach(button => button.addEventListener('click', () => {
  state.language = button.dataset.lang;
  save();
  render();
  closeModals();
  toast('Language updated');
}));

$('#profileForm').addEventListener('submit', event => {
  event.preventDefault();
  state.shopName = $('#shopNameInput').value.trim() || 'My Shop';
  save();
  render();
  closeModals();
  toast('Shop profile updated');
});

$$('[data-period]').forEach(button => button.addEventListener('click', () => {
  $$('[data-period]').forEach(item => item.classList.remove('active'));
  button.classList.add('active');
  toast(`Showing ${button.textContent.toLowerCase()}`);
}));

$('#todayLabel').textContent = new Intl.DateTimeFormat('en-IN', {
  weekday: 'long',
  day: 'numeric',
  month: 'long'
}).format(new Date()).toUpperCase();

// Auth and calendar event bindings
// Navigate to dedicated auth pages instead of modals
$('#signInBtn')?.addEventListener('click', () => navigate('signin'));
$('#signUpBtn')?.addEventListener('click', () => navigate('signup'));

// Page form handlers
$('#signInPageForm')?.addEventListener('submit', event => {
  event.preventDefault();
  const id = $('#signinPageId').value.trim();
  const role = $('#signinPageRole').value;
  signIn({ id, name: id, role });
  navigate('home');
});

$('#signUpPageForm')?.addEventListener('submit', event => {
  event.preventDefault();
  const name = $('#signupPageName').value.trim();
  const id = $('#signupPageId').value.trim();
  const role = $('#signupPageRole').value;
  signUp({ id, name, role });
  navigate('home');
});

// Back buttons on pages
$('#backToHomeFromSignIn')?.addEventListener('click', () => navigate('home'));
$('#backToHomeFromSignUp')?.addEventListener('click', () => navigate('home'));

// Log out page actions
$('#confirmLogout')?.addEventListener('click', () => { signOut(); navigate('signin'); });
$('#cancelLogout')?.addEventListener('click', () => navigate('home'));

// Profile top logout icon shows logout page
$('#logoutBtn')?.addEventListener('click', () => {
  if(currentUser){
    $('#logoutUserName').textContent = currentUser.name || currentUser.id || 'User';
  }
  navigate('logout');
});

$('#topShopName')?.addEventListener('click', () => openCalendarModal());
$('#historyView')?.addEventListener('click', event => { event.preventDefault(); renderHistory(); });
$('#historyDownload')?.addEventListener('click', downloadHistoryCSV);

render();

if('serviceWorker' in navigator && location.protocol.startsWith('http')){
  navigator.serviceWorker.register('./sw.js').catch(() => {});
}

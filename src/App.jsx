import { useEffect, useMemo, useState } from 'react';

const initialCustomers = [
  { id: 1, name: 'Sachin Gohane', phone: '9876543210', balance: 0, updated: 'Today' },
  { id: 2, name: 'Vishal Wele', phone: '9823012345', balance: 1550, updated: 'Yesterday' },
  { id: 3, name: 'Shitij Shimpi J', phone: '9765412345', balance: 3080, updated: '2 days ago' },
  { id: 4, name: 'Prashant Kamble', phone: '9900112233', balance: 0, updated: '4 days ago' },
  { id: 5, name: 'Sanjay Bagde CGL', phone: '9123456780', balance: 1360, updated: '1 week ago' }
];

const initialTransactions = [
  { id: 1, customerId: 2, type: 'credit', amount: 1550, note: 'Stock purchase', date: 'Today' },
  { id: 2, customerId: 3, type: 'credit', amount: 3080, note: 'Monthly supplies', date: 'Yesterday' },
  { id: 3, customerId: 5, type: 'payment', amount: 500, note: 'Cash received', date: '2 days ago' },
  { id: 4, customerId: 5, type: 'credit', amount: 1860, note: 'Opening balance', date: '1 week ago' }
];

const loadJson = (key, fallback) => {
  try {
    const value = JSON.parse(localStorage.getItem(key) || 'null');
    return value ?? fallback;
  } catch {
    return fallback;
  }
};

const money = value => `Rs ${Math.abs(Number(value) || 0).toLocaleString('en-IN')}`;
const initials = name => name.split(' ').map(part => part[0]).join('').slice(0, 2).toUpperCase();

function App() {
  const [page, setPage] = useState('parties');
  const [tab, setTab] = useState('customers');
  const [customers, setCustomers] = useState(() => loadJson('hisably-customers', initialCustomers));
  const [transactions, setTransactions] = useState(() => loadJson('hisably-transactions', initialTransactions));
  const [query, setQuery] = useState('');
  const [selectedCustomerId, setSelectedCustomerId] = useState(null);
  const [theme, setTheme] = useState(() => localStorage.getItem('hisably-theme') || 'light');
  const [shop, setShop] = useState(() => localStorage.getItem('hisably-shop') || 'Rajbagh Tea');
  const [notice, setNotice] = useState('');
  const [customerForm, setCustomerForm] = useState({ name: '', phone: '' });
  const [entryForm, setEntryForm] = useState({ customerId: '', type: 'credit', amount: '', note: '' });

  useEffect(() => {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register(`${import.meta.env.BASE_URL}sw.js`, {
        scope: import.meta.env.BASE_URL
      }).catch(error => console.warn('Service worker registration failed.', error));
    }
  }, []);

  const totals = useMemo(() => {
    const give = customers.filter(customer => customer.balance < 0).reduce((sum, customer) => sum + Math.abs(customer.balance), 0);
    const get = customers.filter(customer => customer.balance > 0).reduce((sum, customer) => sum + customer.balance, 0);
    return { give, get, net: get - give };
  }, [customers]);

  const filteredCustomers = customers.filter(customer => {
    const text = `${customer.name} ${customer.phone}`.toLowerCase();
    return text.includes(query.toLowerCase());
  });

  const selectedCustomer = customers.find(customer => customer.id === selectedCustomerId) || customers[0];
  const selectedLedger = transactions.filter(transaction => transaction.customerId === selectedCustomer?.id);

  const persistCustomers = next => {
    setCustomers(next);
    localStorage.setItem('hisably-customers', JSON.stringify(next));
  };

  const persistTransactions = next => {
    setTransactions(next);
    localStorage.setItem('hisably-transactions', JSON.stringify(next));
  };

  const toast = message => {
    setNotice(message);
    window.clearTimeout(window.hisablyToastTimer);
    window.hisablyToastTimer = window.setTimeout(() => setNotice(''), 2500);
  };

  const addCustomer = event => {
    event.preventDefault();
    const phone = customerForm.phone.replace(/\D/g, '');
    if (!customerForm.name.trim() || phone.length !== 10) {
      toast('Enter customer name and valid 10 digit mobile number.');
      return;
    }

    const customer = {
      id: Date.now(),
      name: customerForm.name.trim(),
      phone,
      balance: 0,
      updated: 'Just now'
    };

    persistCustomers([customer, ...customers]);
    setCustomerForm({ name: '', phone: '' });
    setSelectedCustomerId(customer.id);
    setPage('ledger');
    toast('Customer added.');
  };

  const addTransaction = event => {
    event.preventDefault();
    const amount = Number(entryForm.amount);
    const customerId = Number(entryForm.customerId || selectedCustomer?.id);
    if (!customerId || amount <= 0) {
      toast('Select customer and enter amount.');
      return;
    }

    const transaction = {
      id: Date.now(),
      customerId,
      type: entryForm.type,
      amount,
      note: entryForm.note || (entryForm.type === 'credit' ? 'Credit given' : 'Payment received'),
      date: new Date().toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
    };

    persistTransactions([transaction, ...transactions]);
    persistCustomers(customers.map(customer => {
      if (customer.id !== customerId) return customer;
      const delta = entryForm.type === 'credit' ? amount : -amount;
      return { ...customer, balance: customer.balance + delta, updated: 'Just now' };
    }));
    setEntryForm({ customerId: String(customerId), type: 'credit', amount: '', note: '' });
    setSelectedCustomerId(customerId);
    setPage('ledger');
    toast('Transaction saved.');
  };

  const exportBackup = () => {
    const backup = JSON.stringify({ customers, transactions, shop }, null, 2);
    const url = URL.createObjectURL(new Blob([backup], { type: 'application/json' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = 'hisably-backup.json';
    link.click();
    URL.revokeObjectURL(url);
  };

  const saveShop = event => {
    event.preventDefault();
    localStorage.setItem('hisably-shop', shop);
    toast('Shop profile saved.');
  };

  const setAppTheme = value => {
    setTheme(value);
    localStorage.setItem('hisably-theme', value);
  };

  return (
    <div className={`app ${theme === 'dark' ? 'dark' : ''}`}>
      <aside className="side">
        <button className="brand" onClick={() => setPage('parties')}>
          <span>Hi</span>
          <strong>Hisably</strong>
        </button>
        <nav>
          <NavButton active={page === 'parties'} label="Parties" icon="P" onClick={() => setPage('parties')} />
          <NavButton active={page === 'ledger'} label="Ledger" icon="L" onClick={() => setPage('ledger')} />
          <NavButton active={page === 'reports'} label="Reports" icon="R" onClick={() => setPage('reports')} />
          <NavButton active={page === 'more'} label="More" icon="M" onClick={() => setPage('more')} />
        </nav>
        <div className="side-card">
          <b>Business simplified</b>
          <p>Fast customer khata, reports, reminders, and backups.</p>
        </div>
      </aside>

      <main>
        <header className="topbar">
          <div>
            <h1>{shop}</h1>
            <p>Nagpur, Maharashtra</p>
          </div>
          <button className="coin" onClick={() => toast('Reward coins are demo only.')}>10</button>
        </header>

        <section className="hero">
          <div className="tabs" role="tablist">
            <button className={tab === 'customers' ? 'active' : ''} onClick={() => setTab('customers')}>Customers</button>
            <button className={tab === 'suppliers' ? 'active' : ''} onClick={() => setTab('suppliers')}>Suppliers</button>
          </div>
          <div className="balance-strip">
            <Summary label="You will give" value={money(totals.give)} tone="green" />
            <Summary label="You will get" value={money(totals.get)} tone="red" />
            <button onClick={() => setPage('reports')}>View Reports</button>
          </div>
        </section>

        {page === 'parties' && (
          <div className="page-grid">
            <section className="panel wide">
              <div className="search-row">
                <input value={query} onChange={event => setQuery(event.target.value)} placeholder="Search customer" />
                <button onClick={() => toast('Filter coming soon.')}>Filters</button>
              </div>
              {tab === 'suppliers' ? (
                <EmptyState title="Add supplier and manage your purchases" action="Add supplier" />
              ) : (
                <div className="list">
                  {filteredCustomers.map(customer => (
                    <button
                      className="customer-row"
                      key={customer.id}
                      onClick={() => {
                        setSelectedCustomerId(customer.id);
                        setPage('ledger');
                      }}
                    >
                      <span className="avatar">{initials(customer.name)}</span>
                      <span className="grow">
                        <b>{customer.name}</b>
                        <small>{customer.updated} | {customer.phone}</small>
                      </span>
                      <strong className={customer.balance > 0 ? 'debit' : 'neutral'}>{money(customer.balance)}</strong>
                    </button>
                  ))}
                </div>
              )}
            </section>

            <section className="panel form-panel">
              <h2>Add customer</h2>
              <form onSubmit={addCustomer}>
                <label>Customer name<input value={customerForm.name} onChange={event => setCustomerForm({ ...customerForm, name: event.target.value })} /></label>
                <label>Mobile number<input value={customerForm.phone} maxLength="10" inputMode="numeric" onChange={event => setCustomerForm({ ...customerForm, phone: event.target.value })} /></label>
                <button className="primary">Save customer</button>
              </form>
            </section>
          </div>
        )}

        {page === 'ledger' && selectedCustomer && (
          <div className="page-grid">
            <section className="panel wide">
              <div className="ledger-head">
                <div>
                  <span className="avatar large">{initials(selectedCustomer.name)}</span>
                  <h2>{selectedCustomer.name}</h2>
                  <p>{selectedCustomer.phone}</p>
                </div>
                <strong className={selectedCustomer.balance > 0 ? 'debit total' : 'neutral total'}>{money(selectedCustomer.balance)}</strong>
              </div>
              <div className="history">
                {selectedLedger.length === 0 && <p className="muted">No transactions yet.</p>}
                {selectedLedger.map(transaction => (
                  <article key={transaction.id}>
                    <span>{transaction.type === 'credit' ? 'Credit' : 'Payment'}</span>
                    <b className={transaction.type === 'credit' ? 'debit' : 'credit'}>{money(transaction.amount)}</b>
                    <small>{transaction.note} | {transaction.date}</small>
                  </article>
                ))}
              </div>
            </section>

            <section className="panel form-panel">
              <h2>Add transaction</h2>
              <form onSubmit={addTransaction}>
                <label>Customer
                  <select value={entryForm.customerId || selectedCustomer.id} onChange={event => setEntryForm({ ...entryForm, customerId: event.target.value })}>
                    {customers.map(customer => <option key={customer.id} value={customer.id}>{customer.name}</option>)}
                  </select>
                </label>
                <div className="segmented">
                  <button type="button" className={entryForm.type === 'credit' ? 'active' : ''} onClick={() => setEntryForm({ ...entryForm, type: 'credit' })}>Credit</button>
                  <button type="button" className={entryForm.type === 'payment' ? 'active' : ''} onClick={() => setEntryForm({ ...entryForm, type: 'payment' })}>Payment</button>
                </div>
                <label>Amount<input type="number" min="1" value={entryForm.amount} onChange={event => setEntryForm({ ...entryForm, amount: event.target.value })} /></label>
                <label>Note<input value={entryForm.note} onChange={event => setEntryForm({ ...entryForm, note: event.target.value })} /></label>
                <button className="primary">Save entry</button>
              </form>
            </section>
          </div>
        )}

        {page === 'reports' && (
          <div className="page-grid">
            <section className="panel wide">
              <h2>Reports</h2>
              <div className="report-bars">
                {[42, 65, 48, 78, 56, 88, 62].map((height, index) => <i key={index} style={{ height: `${height}%` }} />)}
              </div>
              <div className="recent">
                {transactions.slice(0, 5).map(transaction => {
                  const customer = customers.find(item => item.id === transaction.customerId);
                  return <p key={transaction.id}>{customer?.name || 'Customer'} - {transaction.type} - {money(transaction.amount)}</p>;
                })}
              </div>
            </section>
            <section className="panel report-card">
              <Summary label="Payments collected" value={money(transactions.filter(item => item.type === 'payment').reduce((sum, item) => sum + item.amount, 0))} tone="green" />
              <Summary label="Credit given" value={money(transactions.filter(item => item.type === 'credit').reduce((sum, item) => sum + item.amount, 0))} tone="red" />
              <Summary label="Net movement" value={money(totals.net)} tone="blue" />
            </section>
          </div>
        )}

        {page === 'more' && (
          <div className="page-grid">
            <section className="panel wide">
              <h2>More tools</h2>
              <div className="tool-grid">
                {['Loans', 'Cashbook', 'Bills', 'Items', 'Staff', 'Collection', 'Settings', 'Help'].map(item => <button key={item}>{item}</button>)}
              </div>
            </section>
            <section className="panel form-panel">
              <h2>Shop settings</h2>
              <form onSubmit={saveShop}>
                <label>Shop name<input value={shop} onChange={event => setShop(event.target.value)} /></label>
                <div className="segmented">
                  <button type="button" className={theme === 'light' ? 'active' : ''} onClick={() => setAppTheme('light')}>Light</button>
                  <button type="button" className={theme === 'dark' ? 'active' : ''} onClick={() => setAppTheme('dark')}>Dark</button>
                </div>
                <button className="primary">Save settings</button>
                <button type="button" onClick={exportBackup}>Download backup</button>
              </form>
            </section>
          </div>
        )}
      </main>

      <button className="floating" onClick={() => setPage(page === 'ledger' ? 'ledger' : 'parties')}>+ Add</button>

      <nav className="bottom">
        <NavButton active={page === 'parties'} label="Parties" icon="P" onClick={() => setPage('parties')} />
        <NavButton active={page === 'ledger'} label="Ledger" icon="L" onClick={() => setPage('ledger')} />
        <NavButton active={page === 'reports'} label="Reports" icon="R" onClick={() => setPage('reports')} />
        <NavButton active={page === 'more'} label="More" icon="M" onClick={() => setPage('more')} />
      </nav>

      {notice && <div className="toast">{notice}</div>}
    </div>
  );
}

function NavButton({ active, label, icon, onClick }) {
  return <button className={active ? 'active' : ''} onClick={onClick}><span>{icon}</span>{label}</button>;
}

function Summary({ label, value, tone }) {
  return <div className={`summary ${tone}`}><small>{label}</small><strong>{value}</strong></div>;
}

function EmptyState({ title, action }) {
  return (
    <div className="empty">
      <div className="empty-art">Rs</div>
      <h2>{title}</h2>
      <button className="primary">{action}</button>
    </div>
  );
}

export default App;

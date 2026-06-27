import fs from 'node:fs';

const requiredFiles = [
  'index.html',
  'src/main.jsx',
  'src/App.jsx',
  'src/App.css',
  'public/manifest.webmanifest',
  'public/sw.js',
  'public/icon.svg',
  'public/assets/icons/icon-192.png',
  'public/assets/icons/icon-512.png'
];

for (const file of requiredFiles) {
  if (!fs.existsSync(file)) {
    console.error(`Missing required web file: ${file}`);
    process.exit(1);
  }
}

const appSource = fs.readFileSync('src/App.jsx', 'utf8');
for (const keyword of ['useState', 'useMemo', 'localStorage', 'addCustomer', 'addTransaction']) {
  if (!appSource.includes(keyword)) {
    console.error(`React app source is missing expected ledger logic: ${keyword}`);
    process.exit(1);
  }
}

const manifest = JSON.parse(fs.readFileSync('public/manifest.webmanifest', 'utf8'));
if (manifest.display !== 'standalone') {
  console.error('PWA manifest display must be standalone.');
  process.exit(1);
}

console.log('React web source validation passed.');

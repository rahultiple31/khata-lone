import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  base: '/khata-lone/',
  plugins: [react()],
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
});

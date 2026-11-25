import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const proxy = {
  '/api': { target: process.env.TWM_API_URL || 'http://127.0.0.1:8080' },
};

export default defineConfig({
  plugins: [react()],
  server: { host: '127.0.0.1', proxy },
  preview: { host: '127.0.0.1', proxy },
});

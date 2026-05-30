import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => ({
  plugins: [react()],
  base: mode === 'production' ? '/mytracks-ui/' : '/',
  server: {
    proxy: {
      '/mytracks': 'http://localhost:8080',
      '/v1/traces': 'http://localhost:4318',
    },
  },
}))

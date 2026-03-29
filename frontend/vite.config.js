import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react-swc'

// https://vite.dev/config/
export default defineConfig(() => ({

  plugins: [react()],
  server: {
      host: true, // vagy '127.0.0.1'
      port: 5173,
    // Proxy API and deezer backend endpoints to avoid cross-origin issues during dev
    proxy: {
      '/api': {
        target: 'http://localhost:3002',
        changeOrigin: true,
        secure: false,
      },
      '/deezer': {
        target: 'http://localhost:3002',
        changeOrigin: true,
        secure: false,
      }
    }
  }
}))

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  define: {
    global: 'globalThis',
  },
  server: {
    proxy: {
      '/event-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/community-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/chat-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/calendar-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/user-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/operation-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/favorite-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/notification-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ai-service': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8080',
        ws: true,
      },
    },
  },
})

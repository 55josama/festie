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
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/community-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/chat-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
        ws: true,
      },
      '/calendar-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/user-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/operation-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/favorite-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/notification-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
      '/ai-service': {
        target: 'http://festie-alb-1266745276.ap-northeast-2.elb.amazonaws.com',
        changeOrigin: true,
      },
    },
  },
})

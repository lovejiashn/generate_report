import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/report': {
        target: 'http://localhost:8090',
        changeOrigin: true
      }
    }
  }
})

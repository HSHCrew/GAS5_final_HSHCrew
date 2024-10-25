import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3030,
        strictPort: true,
        proxy: {
            // /api로 시작하는 모든 요청을 백엔드 서버로 프록시
            '/api': {
                target: 'http://localhost:8080', // 백엔드 서버의 주소
                changeOrigin: true,
                secure: false,
                // 필요 시 경로 재작성
                // rewrite: (path) => path.replace(/^\/api/, ''),
            },
        },
    },
});

import { defineConfig, type Plugin } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';

// Im Build übernehmen die Seiten-Assets die Adresse /datenschutz; im Dev-Server braucht es
// dafür einen kleinen Rewrite, sonst wäre die Seite lokal nur unter /datenschutz/ erreichbar.
function datenschutzDevRoute(): Plugin {
  return {
    name: 'datenschutz-dev-route',
    configureServer(server) {
      server.middlewares.use((req, _res, next) => {
        const path = req.url?.split('?')[0];
        if (path === '/datenschutz') req.url = '/datenschutz/index.html';
        next();
      });
    },
  };
}

export default defineConfig({
  appType: 'mpa',
  plugins: [react(), datenschutzDevRoute()],
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      input: {
        main: fileURLToPath(new URL('./index.html', import.meta.url)),
        datenschutz: fileURLToPath(new URL('./datenschutz/index.html', import.meta.url)),
      },
    },
  },
});

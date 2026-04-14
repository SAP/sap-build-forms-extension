import path from 'node:path'
import { createRequire } from 'node:module'

import { defineConfig, normalizePath } from "vite"
import { viteStaticCopy } from 'vite-plugin-static-copy'
import react from "@vitejs/plugin-react"

const require = createRequire(import.meta.url)
const pdfjsDistPath = path.dirname(require.resolve('pdfjs-dist/package.json'))
const cMapsDir = normalizePath(path.join(pdfjsDistPath, 'cmaps'))

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        react(),
        viteStaticCopy({
            targets: [
                {
                    src: cMapsDir,
                    dest: ''
                }
            ]
        })
    ],
    server: {
        port: 3000,
        proxy: {
            "/api": {
                target: "http://localhost:8080",
                changeOrigin: true,
                secure: false,
                // rewrite: (path) => path.replace(/^\/api/, ""),
            },
        },
    },
})

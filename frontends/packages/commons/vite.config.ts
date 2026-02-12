import { resolve } from "path"

import { defineConfig } from "vite"
import react from "@vitejs/plugin-react"
import dts from "vite-plugin-dts"

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react(),
        dts({ include: ["lib"], rollupTypes: true, tsconfigPath: "./tsconfig.app.json" }),
    ],
    build: {
        copyPublicDir: false,
        lib: {
            entry: resolve(__dirname, "lib/index.ts"),
            formats: ["es"],
        },
        rollupOptions: {
            external: [
                "react",
                "react/jsx-runtime",
                "@ui5/webcomponents",
                "@ui5/webcomponents-base",
                "@ui5/webcomponents-fiori",
                "@ui5/webcomponents-icons",
                "@ui5/webcomponents-icons-business-suite",
                "@ui5/webcomponents-tnt",
                "@ui5/webcomponents-localization",
                "@ui5/webcomponents-react",
                "@ui5/webcomponents-react-base",
                "@ui5/webcomponents-theming",
            ],
        },
        sourcemap: true,
    },
})

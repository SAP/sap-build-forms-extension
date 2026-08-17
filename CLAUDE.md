# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SAP Build Forms Extension is a framework for extended forms in SAP's Build Process Automation. It is a hybrid Maven/Node.js monorepo: Java Spring Boot modules provide the backend framework, and React/TypeScript frontend apps are built separately and embedded as static assets into the Java modules.

## Build Commands

### Full Build (frontends + Java)

```bash
make
```

This runs: `build_frontends` → `copy_frontends` → `mvn clean install`

### Frontend Only (from `frontends/`)

```bash
cd frontends
yarn install
yarn link
yarn workspace commons build   # must build commons first
yarn workspaces foreach -A run build
```

### Individual frontend app (from `frontends/`)

```bash
yarn workspace <app-name> build   # e.g., yarn workspace framework build
```

### Maven (Java only)

```bash
mvn clean install
```

### Frontend dev servers

All frontend apps run on port 3000 with `yarn dev`:

```bash
cd frontends && yarn workspace <app-name> dev
```

- **framework** dev server: proxies `/api` → `http://localhost:8080`
- **devserver** dev server: proxies `/api` → `http://localhost:8090`

### Lint (commons package only)

```bash
cd frontends && yarn workspace commons lint
```

There is no test runner configured in any package — no jest/vitest setup exists.

## Architecture

### Monorepo Structure

```
/
├── Makefile                    # Orchestrates full build
├── pom.xml                     # Maven parent (version 0.2.0-SNAPSHOT, Java 21)
├── cockpit/                    # Cockpit Java module (admin UI backend)
├── core/                       # Core framework Java modules
│   ├── core-common/            # Base Spring Boot configs, security, cloud
│   ├── core-framework/         # Main framework (Redis, OpenAPI, gRPC)
│   ├── core-maven/             # Maven plugin utilities + devserver static assets
│   ├── core-btp/               # SAP BTP integration
│   └── core-workflow-sbpa/     # SBPA workflow integration
├── p13n/                       # Personalization Java modules
├── valuehelp/                  # Value help Java modules
├── integration/sbpa-taskui/    # MTA deployment for Cloud Foundry / SAP BTP
└── frontends/                  # Yarn workspace monorepo (Node.js)
    ├── packages/commons/       # Shared library (must build before apps)
    └── apps/
        ├── cockpit/            # Admin/management UI → cockpit/framework/src/main/resources/frontend
        ├── framework/          # Main forms renderer   → core/framework/src/main/resources/frontend
        ├── devserver/          # Dev tooling UI        → core/maven/src/main/resources/devserver
        ├── p13n/               # Personalization UI    → p13n/framework/src/main/resources/frontend
        └── valuehelp/          # Value help UI         → valuehelp/framework/src/main/resources/frontend
```

### Frontend → Java Asset Pipeline

After `yarn build`, `make copy_frontends` copies each app's `dist/assets/` into the corresponding Java module's `src/main/resources/frontend/` directory. The Java modules then serve these as static resources.

### Frontend Stack

- **React 19** + **TypeScript** + **Vite 7**
- **SAP UI5 Web Components** (`@ui5/webcomponents`) for all UI elements; theme is `sap_horizon`
- **React Router 7** for routing; `ROUTER_BASE_NAME` global variable sets the base path at runtime
- **Redux Toolkit** + **Zustand** for state management (varies by app)
- **React Hook Form** for form handling
- **react-intl** for i18n (locale files in `src/i18n/en.ts`, `src/i18n/de.ts`)
- **React-JSS** for styling
- **pdfjs-dist** in the framework app (cMaps copied as static assets by Vite)
- **dexie** (IndexedDB) for local storage in framework/devserver
- **axios** via the `Backend` class in commons for HTTP communication

### `commons` Package

The shared library (`frontends/packages/commons`) is consumed by all apps. Key exports:

- `Backend` class — wraps axios with request queuing
- `ChangableIntlProvider` / `ChangableIntlContext` — runtime locale switching
- `MessagesContext` / `useMessages` — toast/notification system
- `PageContext` / `usePage` — page title/header management
- `BaseApp`, `Card2`, `Page`, `Placeholder`, `SeverityIcon` — common UI components
- `dateutils`, `languageutils`, `stateutils`, `valuestate` — shared utilities

### Java / Spring Boot

- Spring Boot 3.2.6, Java 21
- OAuth2 / XSUAA authentication (SAP BTP)
- PostgreSQL + Redis
- gRPC for inter-service communication
- OpenAPI for REST documentation
- Consumers integrate the framework by adding `core-framework` or `core-workflow-sbpa` as a Maven dependency and providing a metadata file that describes form structure and event handlers.

### Deployment

The `integration/sbpa-taskui/` module packages the app as an MTA (Multi-Target Application) for Cloud Foundry on SAP BTP, binding to xsuaa, HTML5 repository, and destination services.

## Coding Instructions

- Act as a senior typescript and react developer
- documentation for "SAP UI5 Web Components" is located at https://ui5.github.io/webcomponents-react/v2/?path=/docs/getting-started--docs

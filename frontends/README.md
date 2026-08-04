# Workspace Layout

```
frontends/
├── packages/commons/     # shared library — must be built before any app
└── apps/
    ├── framework/        # forms renderer (Create / Task / Show pages)
    ├── cockpit/          # process monitoring & management UI
    ├── devserver/        # form structure editor (design-time tool)
    ├── p13n/             # personalization settings (user + admin)
    └── valuehelp/        # value help (dropdown list) definition editor
```

# Commands (run from `frontends/`)

```bash
yarn install
yarn workspace commons build          # always build commons first
yarn workspace <app> build            # build one app: commons, cockpit, devserver, framework, p13n, valuehelp
yarn workspace <app> dev              # dev server on port 3000
yarn workspace commons lint           # only commons has a lint script
```

# App-by-App Architecture

## `framework` — the core forms renderer

Three routes → three pages, all follow the same pattern: dispatch `createSession()` thunk → backend returns a full form
definition + values → Redux stores it → `FormPage` renders it.

```
pages/Create.tsx    /create[/:state]       initialize new form
pages/Task.tsx      /task/:task            execute an existing task
pages/Show.tsx      /show/:formsId/:state  view a completed form
```

**Redux store** (`features/store.ts`): three slices — `session`, `valuehelps`, `environment`.

**Session slice** (`features/sessions/`):

- `sessionSlice.ts` — main reducer; holds form definition, current values, attachment state
- `sessionActions.ts` — async thunks (createSession, submitForm, triggerEvent, …)
- `forms.ts` — core data types; `ROOT_ROW` constant is the top-level container
- `definitions.ts` — form structure schemas
- `journal.ts` — change-tracking journal; every user edit is recorded here before submission
- `attachmentActions.ts` — file upload/download thunks

**Control system** (`components/controls/`): `Control.tsx` is a dispatcher that reads the element type and renders one
of 28+ specific controls (Input, Select, Table, Wizard, Dialog, …). Layout is composed via `ControlFlexContainer` and
`ControlGridContainer`.

**Value helps** (`features/valuehelps/`): separate slice; `logic.ts` handles fetching and caching dropdown options.

**Environment slice** (`features/environment/environmentSlice.ts`): tracks screen size for responsive layout.

`ROUTER_BASE_NAME` is a global variable injected at build time that sets the React Router basename for subpath
deployment.

The framework app copies `pdfjs-dist` cMaps as static assets (configured in `vite.config.ts`); this is required for PDF
rendering via `react-pdf`.

## `cockpit` — process monitoring

Uses **Zustand** (not Redux). State is in `state/visual.ts`, `state/processes.ts`, `state/backend.ts`. The UI is a
master/detail: `ProcessListView` → `ProcessDetailsView` with five tabs (Details, Tasks, History, Feeds, Children).

## `devserver` — form structure editor

Design-time tool, not deployed to production. A single `Editor` component with a drag-and-drop tree (`StructureTabTree`,
`DraggableTreeItem`) for building form element hierarchies. State lives in `state/elements.ts`. Proxies `/api` to
`localhost:8090`.

## `p13n` — personalization

Redux Toolkit store. Three routes: `/user` (end-user settings), `/admin` (admin settings), `/admin/values`. Dialogs for
adding users, applications, locales, and settings.

## `valuehelp` — value help editor

Redux Toolkit store. Single `EditorPage` with tabs: Config, CurrentValues. Dialogs for adding definitions, values, and
bulk file upload.

# `commons` Package

Shared library consumed by all apps via the Yarn workspace alias `commons`.

Key exports:

- **`Backend<TResponse>`** — axios wrapper with request queuing, 60 s timeout, CSRF token extraction from cookies. All
  apps use this for API calls.
- **`ChangableIntlProvider` / `ChangableIntlContext`** — runtime locale switching; wraps `react-intl`.
- **`useMessages` / `MessagesContext`** — toast/notification system.
- **`usePage` / `PageContext`** — page title and header state.
- **`BaseApp`** — top-level wrapper that applies UI5 theming and the intl/messages providers.
- **`dateutils`, `languageutils`, `stateutils`, `valuestate`** — shared utilities.

Commons has its own `lib/i18n/en.ts` and `de.ts`; each app also has its own `src/i18n/` for app-specific strings.

# Shared Conventions

- **UI library**: SAP UI5 Web Components React (`@ui5/webcomponents-react`); theme is `sap_horizon`, set once in
  `App.tsx` via `setTheme`.
- **Styling**: React-JSS (`react-jss`) — CSS-in-JS with JSS.
- **i18n**: `react-intl`; every app has `en.ts` and `de.ts` under `src/i18n/`.
- **Routing**: React Router 7; `ROUTER_BASE_NAME` global sets basename.
- **HTTP**: always use the `Backend` class from commons, not raw axios.
- **TypeScript**: all apps; `tsc && vite build` is the build command.
- **Prettier** config is at the workspace root (`prettierrc.json`).

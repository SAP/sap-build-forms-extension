# Coding Instructions

- Act as a senior Typescript and React developer
- Avoid usage of `any` type; prefer precise typings and interfaces
- Use functional components and React Hooks; avoid class components
- Follow the existing architectural patterns in each app (e.g. Redux Toolkit in framework/p13n, Zustand in cockpit)
- Use the `Backend` class from the `commons` package for all API calls; do not use raw axios
- For styling, use React-JSS as configured in the project; avoid adding new CSS libraries
- For i18n, add strings to the appropriate `src/i18n/en.ts` and `src/i18n/de.ts` files and use the `useIntl` hook from
  `react-intl`
- Follow the existing folder structure and component organization in each app; for example, keep shared components in
  `components/` and page-level components in `pages/`
- Write clear, maintainable code with proper error handling and user feedback


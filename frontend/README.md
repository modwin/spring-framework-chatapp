# Modwin Chat frontend

React and TypeScript client for the Modwin Chat authentication and friendship API.

```bash
npm ci
npm run dev
```

Useful checks:

```bash
npm run lint
npm test
npm run build
```

Vite proxies `/api`, `/oauth2`, and `/login` to `http://localhost:8081` in development. Production assets are served by Nginx; see the repository-level `compose.yaml` and README for the full stack.

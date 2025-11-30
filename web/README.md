# PortURL Web

A modern React-based web interface for PortURL, mirroring the functionality of the Android application.

## Tech Stack

- **Framework**: React 19 + TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS v3
- **State Management**: React Query (TanStack Query)
- **Authentication**: OIDC (via `react-oidc-context`)

## Getting Started

### Prerequisites

- Node.js (v18+)
- NPM

### Installation

```bash
npm install
```

### Running Locally

```bash
npm run dev
```

The application will be available at `http://localhost:5173`.

### Configuration

Create a `.env` file (or set environment variables) to configure the backend:

```env
VITE_BACKEND_URL=http://localhost:8080
```

## Testing

```bash
# Run Unit Tests
npm run test

# Run E2E Tests
npm run test:e2e
```

## Building for Production

```bash
npm run build
```

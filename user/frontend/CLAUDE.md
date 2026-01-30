# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Vue 3 frontend application for a board portal system (eBrain Portal). The project is currently in early development stage with a Vue 3 + Vite template setup.

**Tech Stack:**
- Vue 3 (Composition API)
- Vite 7.3.1
- Vue DevTools plugin enabled

**Project Context:**
This is the user-facing frontend portion of a larger board-portal system. The project has detailed planning documents (PRD, technical spec, component structure) that outline the intended architecture for a full-featured board system with authentication, multiple board types, and file management.

## Development Commands

```bash
# Install dependencies
npm install

# Start development server (runs on port 5173 by default)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

**Note on Port Configuration:**
- Current Vite setup runs on default port (5173)
- Planning documents reference port 80 with Vue CLI and proxy config to backend on port 8081
- Vite configuration may need updating to match planned architecture

## Project Structure

```
frontend/
├── src/
│   ├── main.js              # Application entry point
│   ├── App.vue              # Root component
│   ├── assets/              # Static assets (CSS, images)
│   │   └── main.css
│   └── components/          # Vue components
│       ├── HelloWorld.vue   # Template demo component
│       ├── TheWelcome.vue   # Template demo component
│       ├── WelcomeItem.vue  # Template demo component
│       └── icons/           # Icon components
├── public/                  # Public static files
├── index.html               # HTML entry point
├── vite.config.js           # Vite configuration
└── package.json
```

**Current State:**
The project is using the default Vue 3 + Vite template. Most of the planned architecture (router, Vuex store, services, views) is not yet implemented.

## Planned Architecture

The planning documents outline a comprehensive board portal system with the following intended structure:

### Planned Technologies (Not Yet Implemented)
- **Vue Router 4**: Client-side routing with navigation guards for authentication
- **Vuex 4**: State management with vuex-persistedstate for localStorage persistence
- **Vuetify 3**: Material Design component library
- **Axios**: HTTP client with request/response interceptors
- **date-fns / moment**: Date formatting utilities

### Planned Directory Structure
```
src/
├── views/                    # Page components (route targets)
│   ├── Main.vue
│   ├── Login.vue
│   ├── Join.vue
│   ├── board/
│   │   ├── notice/          # Notice board views
│   │   ├── free/            # Free board views
│   │   ├── gallery/         # Gallery board views
│   │   └── inquiry/         # Inquiry board views
│   └── Error.vue
├── components/               # Reusable components
│   ├── layout/
│   │   ├── Navbar.vue
│   │   └── Footer.vue
│   ├── board/
│   │   ├── BoardSearch.vue
│   │   ├── Pagination.vue
│   │   ├── CommentList.vue
│   │   └── FileList.vue
│   └── common/
├── router/
│   └── index.js             # Vue Router configuration
├── store/
│   ├── index.js             # Vuex store root
│   └── modules/
│       └── auth.js          # Authentication state module
├── services/                 # API service layer
│   ├── api.js               # Axios instance with interceptors
│   ├── authService.js       # Authentication API calls
│   ├── boardService.js      # Board CRUD operations
│   ├── commentService.js    # Comment operations
│   └── fileService.js       # File upload/download
└── utils/
    ├── errorHandler.js      # Command pattern error handling
    └── validators.js        # Form validation utilities
```

### Key Architectural Patterns (Planned)

**Authentication Flow:**
- JWT tokens stored in Vuex with localStorage persistence
- Axios request interceptor adds Bearer token to all API calls
- Router navigation guards check authentication for protected routes
- Logout clears Vuex state and redirects to main page

**Error Handling (Command Pattern):**
The technical spec describes a Command pattern for error handling:
- `ErrorCommandFactory` creates specific command objects based on error codes
- Each error code (A001-A014) has a corresponding Command class
- Commands execute appropriate responses (alerts, redirects, console logs)
- Applied via Axios response interceptor

**API Communication:**
- Base URL: Planned to proxy `/api` to `http://localhost:8081` in development
- Production URL: `http://3.35.111.101:8081/api`
- All API calls go through service layer (not direct axios calls in components)
- FormData for file uploads with multipart/form-data headers

**State Management:**
- Auth state persisted to localStorage (accessToken, memberId, memberName, isLoggedIn)
- Navigation guard checks `store.state.auth.isLoggedIn` for route protection
- Computed properties in components access Vuex state

**Board System:**
- Four board types: notice, free, gallery, inquiry
- Generic `boardService.js` handles all board types with type parameter
- Shared components (BoardSearch, Pagination, CommentList, FileList)
- Query string preserves search/filter state

## Path Alias

The Vite config sets up a path alias:
- `@` → `./src`

Use this in imports: `import Navbar from '@/components/layout/Navbar.vue'`

## Reference Documents

The project includes detailed planning documents (in Korean):
- `user-frontend-prd.md` - Product requirements
- `user-frontend-technical-spec.md` - Technical architecture and code examples
- `user-frontend-components.md` - Component structure and implementation details
- `user-frontend-prompt-phase[1-4].md` - Phased implementation prompts

Refer to these documents when implementing planned features to maintain consistency with the intended architecture.

## Backend API Integration

**Planned Backend:**
- Spring Boot backend on port 8081
- REST API with JWT authentication
- Error responses use standardized error codes (A001-A014)

**API Endpoints (Planned):**
- `POST /api/login` - User authentication
- `POST /api/member` - User registration
- `GET /api/member/check-duplicate` - Check username availability
- `GET /api/boards/{boardType}` - List boards with pagination/filtering
- `GET /api/board/{boardType}/{id}` - Board detail
- `POST /api/board/{boardType}` - Create board
- `PUT /api/board/{boardType}/{id}` - Update board
- `DELETE /api/board/{boardType}/{id}` - Delete board
- `PATCH /api/board/{boardType}/{id}/increase-view` - Increment view count

## Important Implementation Notes

**When Implementing Features:**

1. **Use Composition API**: All planning documents use Vue 3 Composition API with `<script setup>` or `setup()` function

2. **Service Layer Pattern**: Keep API logic in service files, not in components

3. **Error Handling**: Implement the Command pattern for error handling as specified in technical spec

4. **File Validation**:
   - Max 5 files per upload
   - Max 2MB per file
   - Allowed extensions: .jpg, .jpeg, .gif, .png, .zip

5. **Form Validation**:
   - Titles: max 99 characters
   - Content: max 3999 characters
   - Passwords: 4-20 characters

6. **Date Formatting**: Use `date-fns` (preferred) or `moment` for consistent date display

7. **Routing**:
   - Use named routes for navigation
   - Preserve query parameters for search/filter state
   - Page numbers are 0-based in backend, 1-based in UI

8. **Authentication Guards**:
   - Check `meta.requiresAuth` in route definitions
   - Redirect to login with alert when not authenticated
   - Writing/modifying boards requires authentication
   - Viewing is public except inquiry board details

## Node Version

Required Node.js version: `^20.19.0 || >=22.12.0`

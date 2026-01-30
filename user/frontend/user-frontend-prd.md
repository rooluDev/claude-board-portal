# User Frontend - Product Requirements Document (PRD)

## 프로젝트 정보
- **프로젝트명**: eBrain Portal - User Frontend
- **프로젝트 경로**: `./board-portal/user/frontend`
- **포트**: 80
- **작성일**: 2026-01-29

---

## 1. 프로젝트 개요
Vue.js 3 기반 SPA(Single Page Application)로 구축된 사용자 웹 인터페이스

**주요 특징**:
- SPA (클라이언트 사이드 라우팅)
- JWT 토큰 기반 인증 (Vuex 영속화)
- Material Design (Vuetify 3)
- 반응형 디자인
- REST API 연동

---

## 2. 기술 스택
- Vue.js 3.2.13 (Composition API)
- Vuetify 3.0.0 (Material Design)
- Vue Router 4.2.5
- Vuex 4.0.2 + vuex-persistedstate
- Axios 1.6.7
- date-fns 3.6.0, moment 2.30.1

---

## 3. 페이지 구성

### 3.1 라우트 맵
| 경로 | 컴포넌트 | 설명 | 인증 필요 |
|------|---------|------|----------|
| `/` | Main.vue | 메인 페이지 | ❌ |
| `/login` | Login.vue | 로그인 | ❌ |
| `/join` | Join.vue | 회원가입 | ❌ |
| `/boards/notice` | NoticeList.vue | 공지사항 목록 | ❌ |
| `/boards/notice/:id` | NoticeView.vue | 공지사항 상세 | ❌ |
| `/boards/free` | FreeList.vue | 자유게시판 목록 | ❌ |
| `/boards/free/:id` | FreeView.vue | 자유게시판 상세 | ❌ |
| `/boards/free/write` | FreeWrite.vue | 자유게시판 작성 | ✅ |
| `/boards/free/modify/:id` | FreeWrite.vue | 자유게시판 수정 | ✅ |
| `/boards/gallery` | GalleryList.vue | 갤러리 목록 | ❌ |
| `/boards/gallery/:id` | GalleryView.vue | 갤러리 상세 | ❌ |
| `/boards/gallery/write` | GalleryWrite.vue | 갤러리 작성 | ✅ |
| `/boards/gallery/modify/:id` | GalleryWrite.vue | 갤러리 수정 | ✅ |
| `/boards/inquiry` | InquiryList.vue | 문의 목록 | ❌ |
| `/boards/inquiry/:id` | InquiryView.vue | 문의 상세 | ✅ |
| `/boards/inquiry/write` | InquiryWrite.vue | 문의 작성 | ✅ |
| `/boards/inquiry/modify/:id` | InquiryWrite.vue | 문의 수정 | ✅ |
| `/error` | Error.vue | 에러 페이지 | ❌ |

---

## 4. 주요 기능

### 4.1 인증
- JWT 토큰 Vuex 저장 (localStorage 영속화)
- Axios Interceptor로 자동 헤더 추가
- Navigation Guard로 접근 제어

### 4.2 게시판
- 목록 조회 (검색, 정렬, 페이징)
- 상세 조회 (조회수 자동 증가)
- CRUD (본인 게시물만)
- 파일 업로드/다운로드
- 댓글 작성/삭제

### 4.3 검색
- 쿼리스트링으로 상태 유지
- 날짜 범위, 카테고리, 키워드

### 4.4 에러 처리
- Command 패턴
- ErrorCommandFactory

---

## 5. 컴포넌트 구조

```
src/
├── views/              # 페이지 컴포넌트
│   ├── Main.vue
│   ├── Login.vue
│   ├── Join.vue
│   ├── board/
│   │   ├── notice/
│   │   ├── free/
│   │   ├── gallery/
│   │   └── inquiry/
│   └── Error.vue
├── components/         # 재사용 컴포넌트
│   ├── layout/
│   │   ├── Navbar.vue
│   │   └── Footer.vue
│   ├── board/
│   │   ├── BoardSearch.vue
│   │   ├── Pagination.vue
│   │   ├── CommentList.vue
│   │   └── FileList.vue
│   └── common/
│       ├── LoadingSpinner.vue
│       └── ErrorAlert.vue
├── router/
│   └── index.js
├── store/
│   └── modules/
│       └── auth.js
├── services/           # API 서비스
│   ├── api.js
│   ├── authService.js
│   ├── boardService.js
│   ├── commentService.js
│   └── fileService.js
├── utils/
│   ├── errorHandler.js
│   └── validators.js
└── assets/
```

---

## 6. 상태 관리 (Vuex)

### auth 모듈
```javascript
{
  state: {
    accessToken: null,
    memberId: null,
    memberName: null,
    isLoggedIn: false
  },
  mutations: {
    SET_AUTH(state, payload),
    CLEAR_AUTH(state)
  },
  actions: {
    login({ commit }, authData),
    logout({ commit })
  }
}
```

---

## 7. API 연동

### Axios 설정
- Base URL: http://3.35.111.101:8081/api
- Request Interceptor: JWT 자동 추가
- Response Interceptor: 에러 처리 (Command 패턴)

---

## 8. UI/UX

### Vuetify 컴포넌트
- v-app, v-app-bar, v-navigation-drawer
- v-card, v-table, v-data-table
- v-form, v-text-field, v-textarea, v-select
- v-btn, v-checkbox, v-file-input
- v-dialog, v-snackbar, v-progress-circular

### 반응형 디자인
- 데스크톱: 1200px+
- 태블릿: 768px-1199px
- 모바일: ~767px

---

## 9. 빌드 및 배포

### 개발
```bash
npm run serve
```

### 프로덕션 빌드
```bash
npm run build
```

### 배포
- Nginx로 정적 파일 서빙
- Port 80

자세한 내용은 technical-spec 참조

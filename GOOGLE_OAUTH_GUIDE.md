# Google OAuth 로그인 구현 가이드

## 백엔드 설정 완료 ✅

### 1. 환경변수 설정 (.env 파일)
```env
GOOGLE_OAUTH_CLIENT_ID=your-google-client-id
GOOGLE_OAUTH_CLIENT_SECRET=your-google-client-secret
```

### 2. Google Cloud Console 설정
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 프로젝트 생성 또는 선택
3. `API 및 서비스` > `사용자 인증 정보` 메뉴
4. `+ 사용자 인증 정보 만들기` > `OAuth 2.0 클라이언트 ID` 선택
5. 애플리케이션 유형: `웹 애플리케이션`
6. **승인된 리디렉션 URI** 추가:
   - `http://localhost:5173/auth/signin` (개발용)
   - `https://yourdomain.com/auth/signin` (프로덕션용)
7. Client ID와 Client Secret 복사하여 환경변수에 설정

---

## 프론트엔드 (Vue) 구현 가이드

### 1. Google OAuth 라이브러리 설치
```bash
npm install @vue-oauth2/google
# 또는
npm install vue3-google-login
```

### 2. Vue 컴포넌트 예제

#### 방법 1: Authorization Code 방식 (권장)

```vue
<template>
  <div>
    <button @click="handleGoogleLogin">
      Google로 로그인
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const GOOGLE_CLIENT_ID = 'YOUR_GOOGLE_CLIENT_ID'
const REDIRECT_URI = 'http://localhost:5173/auth/signin'
const BACKEND_URL = 'http://localhost:8080'

// Google 로그인 버튼 클릭
const handleGoogleLogin = () => {
  const googleAuthUrl = 'https://accounts.google.com/o/oauth2/v2/auth'
  const scope = 'profile email'
  
  const params = new URLSearchParams({
    client_id: GOOGLE_CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    response_type: 'code',
    scope: scope,
    access_type: 'offline',
    prompt: 'consent'
  })
  
  // Google 로그인 페이지로 리다이렉트
  window.location.href = `${googleAuthUrl}?${params.toString()}`
}

// 리다이렉트 후 돌아왔을 때 (페이지 로드 시)
const handleGoogleCallback = async () => {
  const urlParams = new URLSearchParams(window.location.search)
  const code = urlParams.get('code')
  
  if (code) {
    try {
      // 백엔드로 code 전송
      const response = await axios.post(`${BACKEND_URL}/auth/google`, {
        code: code,
        redirectUri: REDIRECT_URI
      })
      
      // 로그인 성공
      const { accessToken, user } = response.data.data
      
      // 토큰 저장 (localStorage 또는 Vuex/Pinia)
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('user', JSON.stringify(user))
      
      // refreshToken은 httpOnly 쿠키로 자동 저장됨
      
      // 메인 페이지로 이동
      window.location.href = '/'
      
    } catch (error) {
      console.error('Google 로그인 실패:', error)
      alert('로그인에 실패했습니다.')
    }
  }
}

// 컴포넌트 마운트 시 콜백 처리
if (window.location.pathname === '/auth/signin') {
  handleGoogleCallback()
}
</script>
```

#### 방법 2: vue3-google-login 라이브러리 사용

```vue
<template>
  <div>
    <GoogleLogin :callback="handleGoogleResponse" />
  </div>
</template>

<script setup>
import { GoogleLogin } from 'vue3-google-login'
import axios from 'axios'

const BACKEND_URL = 'http://localhost:8080'

const handleGoogleResponse = async (response) => {
  try {
    // credential은 ID Token (JWT)
    const { credential } = response
    
    // 백엔드로 credential 전송
    const result = await axios.post(`${BACKEND_URL}/auth/google`, {
      code: credential,  // 또는 다른 방식으로 처리
      redirectUri: window.location.origin + '/auth/signin'
    })
    
    const { accessToken, user } = result.data.data
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('user', JSON.stringify(user))
    
    // 로그인 성공 후 리다이렉트
    window.location.href = '/'
    
  } catch (error) {
    console.error('Google 로그인 실패:', error)
  }
}
</script>
```

### 3. 라우터 설정 (Vue Router)

```javascript
// router/index.js
const routes = [
  {
    path: '/auth/signin',
    name: 'GoogleCallback',
    component: () => import('@/views/auth/GoogleCallback.vue')
  },
  // ... other routes
]
```

### 4. Axios 인터셉터 설정 (토큰 자동 첨부)

```javascript
// api/axios.js
import axios from 'axios'

const apiClient = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true  // 쿠키 전송을 위해 필수!
})

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터 (토큰 만료 시 재발급)
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    
    // 401 에러이고 재시도하지 않은 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      try {
        // 토큰 재발급 요청
        const response = await axios.post(
          'http://localhost:8080/auth/token',
          {},
          { withCredentials: true }
        )
        
        const { accessToken } = response.data.data
        localStorage.setItem('accessToken', accessToken)
        
        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return apiClient(originalRequest)
        
      } catch (refreshError) {
        // 재발급 실패 시 로그아웃
        localStorage.removeItem('accessToken')
        localStorage.removeItem('user')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }
    
    return Promise.reject(error)
  }
)

export default apiClient
```

### 5. 로그아웃 구현

```vue
<template>
  <button @click="handleLogout">로그아웃</button>
</template>

<script setup>
import apiClient from '@/api/axios'

const handleLogout = async () => {
  try {
    await apiClient.post('/logout')
    
    // 로컬 스토리지 클리어
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    
    // 로그인 페이지로 이동
    window.location.href = '/login'
    
  } catch (error) {
    console.error('로그아웃 실패:', error)
  }
}
</script>
```

---

## API 엔드포인트

### Google 로그인
- **URL**: `POST /auth/google`
- **Request Body**:
  ```json
  {
    "code": "4/0AY0e-g7...",
    "redirectUri": "http://localhost:5173/auth/signin"
  }
  ```
- **Response**:
  ```json
  {
    "status": 200,
    "message": "Google 로그인 성공",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "id": 1,
      "email": "user@gmail.com",
      "name": "홍길동",
      "role": "customer"
    }
  }
  ```
- **참고**: `refreshToken`은 httpOnly 쿠키로 자동 저장됩니다.

### 토큰 재발급
- **URL**: `POST /auth/token`
- **Cookie**: `refreshToken` (httpOnly)
- **Response**:
  ```json
  {
    "status": 200,
    "message": "토큰 재발급 성공",
    "data": {
      "accessToken": "새로운_액세스_토큰"
    }
  }
  ```

### 로그아웃
- **URL**: `POST /logout`
- **Cookie**: `refreshToken` (httpOnly)

---

## 주의사항

1. **CORS 설정 확인**: 백엔드에서 프론트엔드 도메인을 허용해야 합니다.
2. **withCredentials**: 쿠키를 사용하려면 axios 설정에 `withCredentials: true` 필수
3. **HTTPS**: 프로덕션 환경에서는 반드시 HTTPS 사용
4. **Redirect URI**: Google Console과 프론트엔드, 백엔드 요청의 URI가 모두 일치해야 함

---

## 플로우 요약

1. 사용자가 "Google로 로그인" 버튼 클릭
2. Google OAuth 페이지로 리다이렉트
3. 사용자가 Google 계정으로 로그인
4. Google이 `redirectUri`로 `code`를 전달하며 리다이렉트
5. 프론트엔드가 `code`와 `redirectUri`를 백엔드로 전송
6. 백엔드가 Google에서 사용자 정보를 가져와 JWT 발급
7. 프론트엔드가 accessToken 저장 및 메인 페이지 이동


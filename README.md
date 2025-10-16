# HotelReservation Backend

깔끔한 설정 가이드를 포함한 백엔드 문서입니다.

## 🏗️ 아키텍처 개요
<img width="3211" height="1580" alt="daewoo hotel architecture diagram drawio" src="https://github.com/user-attachments/assets/029baf01-6bc8-47ba-8216-04b6fdd76ce6" />

- Vue.js SPA + Nginx Reverse Proxy + Spring Boot API
- MariaDB(RDS), Redis, S3(+CloudFront), Firebase FCM, Mail(SMTP)

---

## 🚀 빠른 시작
- 요구사항: Java 21, Maven, Docker(옵션: Redis), MariaDB

1) 의존성 설치 및 빌드
- macOS/Linux
  - mvn -v
  - ./mvnw clean package -DskipTests
- Windows
  - mvnw.cmd -v
  - mvnw.cmd clean package -DskipTests

2) 환경 변수 파일 준비
- 경로: src/main/resources/properties/.env.properties
- 실제 비밀값은 절대 커밋하지 마세요. 아래 템플릿을 복사해 로컬에서만 작성하세요.

3) Firebase 서비스 키 배치
- 경로: config/firebase-service.json (레포 제외/비공개)
- GCP 콘솔에서 Firebase Admin SDK JSON을 내려받아 위 경로에 배치하세요.

4) Redis 실행(Docker)
- 로컬 개발은 Docker로 간단히 실행하는 것을 권장합니다.
- docker run -d --name hotel-redis -p 6379:6379 -e REDIS_PASSWORD=your_strong_password redis:7-alpine --requirepass your_strong_password

5) 애플리케이션 실행
- ./mvnw spring-boot:run
- 기본 포트: http://localhost:8080

---

## ⚙️ 환경 변수 템플릿(.env.properties 예시)
다음은 예시입니다. 실제 키/비밀번호는 본인 값으로 교체하고 커밋 금지(.gitignore 권장).

JWT_SECRET=your_jwt_secret_value
DB_URL=jdbc:mariadb://localhost:3306/hotel_db
DB_USERNAME=hotel_user
DB_PASSWORD=hotel_password
GMAIL_USERNAME=your_gmail_username
GMAIL_PASSWORD=your_gmail_app_password
TOSS_SECRET_KEY=your_toss_secret_key
kakao.api.key=your_kakao_rest_api_key
GOOGLE_OAUTH_CLIENT_ID=your_google_oauth_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_google_oauth_client_secret
KAKAO_OAUTH_CLIENT_ID=your_kakao_oauth_client_id
KAKAO_OAUTH_CLIENT_SECRET=your_kakao_oauth_client_secret
MODE=development
DEPLOY_URL=http://localhost:5173
AWS_S3_BUCKET=your_s3_bucket
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_CLOUDFRONT_DOMAIN=https://your-cloudfront-domain
REDIS_PASSWORD=your_redis_password

참고: application.yml은 위 값을 ${...}로 참조합니다.

---

## 🔐 Firebase 설정
- 파일: config/firebase-service.json
- 보안상 레포에 올리지 마세요. 운영/개발 환경 별로 서버에 직접 배포하세요.
- Firebase Admin SDK가 푸시 알림(FCM) 발송을 위해 사용됩니다.

---

## 🧰 Redis (Docker) 가이드
- 실행: docker run -d --name hotel-redis -p 6379:6379 -e REDIS_PASSWORD=your_strong_password redis:7-alpine --requirepass your_strong_password
- 접속 확인: docker logs -f hotel-redis 또는 redis-cli -a your_strong_password ping
- Spring은 REDIS_PASSWORD, 기타 호스트/포트를 기본값 또는 환경에 맞게 사용합니다.

---

## 📦 파일 업로드(로컬)
- 기본 업로드 경로: 프로젝트 루트의 uploads/ 디렉터리
- 개발 환경에서는 Local File Uploader 사용, 운영 환경에서는 S3(+CloudFront) 사용을 권장합니다.
- 업로드된 파일은 정적 리소스로 서빙되며 이미지 경로가 응답/템플릿에 포함됩니다.

---

## 🔑 OAuth2 (Google/Kakao)
- application.yml에 등록된 ${GOOGLE_OAUTH_CLIENT_ID}, ${GOOGLE_OAUTH_CLIENT_SECRET} 및 Kakao 값 필요
- 리디렉트 URI는 DEPLOY_URL을 기준으로 구성됩니다.
- 상세 가이드는 레포 루트의 GOOGLE_OAUTH_GUIDE.md를 참고하세요.

---

## 📚 기술 스택
- Spring Boot 3.5.x, Java 21
- JPA(Hibernate), MariaDB
- Redis(Jedis), Firebase Admin, Spring Mail
- OAuth2 Client(Google/Kakao)
- AWS SDK v2 (S3), CloudFront
- Springdoc OpenAPI

---

## 🗺️ 요청 흐름(요약)
1) 사용자가 도메인 접속 → Nginx
2) 정적 페이지(Vue) 또는 /api/** → Spring Boot로 프록시
3) Spring Boot → RDS/Redis/S3/FCM 연동
4) 이미지/정적 리소스는 CloudFront 또는 로컬 정적 경로로 전송

---


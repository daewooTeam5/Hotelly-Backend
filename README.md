# HotelResrvation-Backend

# 아키텍쳐    
<img width="3211" height="1580" alt="daewoo hotel architecture diagram drawio" src="https://github.com/user-attachments/assets/029baf01-6bc8-47ba-8216-04b6fdd76ce6" />   


## ⚙️ 개요  
AWS 기반의 웹 서비스 인프라로,  
**Nginx 리버스 프록시**, **Spring Boot 백엔드**, **Vue.js 프론트엔드**를 중심으로  
데이터베이스, 캐시, 스토리지, 알림 시스템을 통합 구성하였습니다.

---

## 🧩 구성 요소

### 🟢 Nginx (EC2)
- `/` → **Vue.js 정적 파일** 서빙  
- `/api/**` → **Spring Boot API 서버**로 리버스 프록시  
- SSL 인증서 적용 및 트래픽 라우팅 담당  

### 💚 Spring Boot (EC2)
- 비즈니스 로직 처리  
- RDS, Redis, S3, FCM 등 외부 서비스와 연동  

### 🐘 AWS RDS (MariaDB)
- EC2와 **내부망(VPC)** 으로 연결  
- 서비스 주요 데이터 저장  

### 🔴 Redis (EC2 내부)
- 세션, 토큰, 캐시 관리  

### 🗂️ AWS S3 + CloudFront
- 이미지 업로드 및 정적 리소스 저장  
- CloudFront를 통한 CDN 캐싱 및 전송 최적화  

### 🔔 Firebase Cloud Messaging (FCM)
- 푸시 알림 송수신 처리  

### 🌐 Gabia DNS
- 사용자 도메인 → EC2 Nginx로 연결  

---

## 🚀 요청 흐름
1. 사용자가 도메인 접속 → Gabia DNS → Nginx로 전달  
2. Nginx  
   - `/index.html` → Vue.js 정적 페이지 제공  
   - `/api/**` → Spring Boot로 프록시  
3. Spring Boot  
   - RDS 데이터 처리  
   - Redis 캐싱  
   - S3 이미지 업로드  
   - FCM 알림 발송  
4. CloudFront를 통해 이미지/정적 리소스 전송  

---

📦 **구성 요약**
| 구성 요소 | 역할 |
|------------|-------|
| Vue.js | 프론트엔드 SPA |
| Nginx | 리버스 프록시 / SSL / 정적 파일 |
| Spring Boot | API 서버 / 비즈니스 로직 |
| AWS RDS (MariaDB) | 데이터 저장소 |
| Redis | 캐시 / 세션 관리 |
| S3 + CloudFront | 이미지 저장 / CDN |
| Firebase FCM | 푸시 알림 |
| Gabia DNS | 도메인 관리 |

---

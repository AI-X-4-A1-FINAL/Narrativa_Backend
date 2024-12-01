![NARRATIVA-TITLE](https://github.com/user-attachments/assets/97538156-f202-4b48-8543-9bbf835fda0e)

# Narrativa Backend

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-v3.3.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-v3.3.5-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![OAuth 2.0](https://img.shields.io/badge/OAuth%202.0-v3.3.5-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-v6.2.4-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4.3%20LTS-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## 🗝️ 프로젝트 소개

`Narrativa_Backend`는 AI 기반 스토리 생성 플랫폼인 Narrativa 프로젝트의 백엔드 모듈입니다.<br />
이 프로젝트는 사용자 입력을 기반으로 스토리를 생성하고 관리하는 기능을 제공합니다.<br />
`Spring Boot`, `JPA`, `MySQL` 등을 활용하여 확장성과 안정성을 고려한 백엔드 시스템을 구축했습니다.<br />

## 🗝️ 설치 가이드

Narrativa_Backend 프로젝트를 로컬 환경에서 클론하고, 빌드 및 실행하는 방법을 설명합니다.

### 1. 프로젝트 클론
```bash
git clone https://github.com/AI-X-4-A1-FINAL/Narrativa_Backend.git
cd narrativa-backend
```

### 2. 빌드 및 설치
```bash
./gradlew clean build
```

### 3. 환경 설정
`src/main/resources/application.yml` 파일을 다음과 같이 설정합니다:

```yaml
# yaml 예시
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/[데이터베이스 이름]?useSSL=false&serverTimezone=Asia/Seoul
    username: [데이터베이스 사용자 이름]
    password: [데이터베이스 비밀번호]
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### 4. 실행
```bash
./gradlew bootRun

# http://localhost:8080
```

## 🗝️ 브랜치 관리 규칙

### 브랜치 구조
1. **메인 브랜치 (main)**
    - 프로덕션 배포용 안정 브랜치
    - PR을 통해서만 병합 가능

2. **개발 브랜치 (dev)**
    - 개발 중인 기능 통합 브랜치
    - 배포 전 최종 테스트 진행

3. **기능 브랜치 (feat/)**
    - 새로운 기능 개발용
    - 명명규칙: `feat/{기능명}`
    - 예: `feat/social-login`

4. **긴급 수정 브랜치 (hotfix/)**
    - 프로덕션 긴급 버그 수정용
    - 명명규칙: `hotfix/{이슈번호}`
    - 예: `hotfix/critical-bug`

### 브랜치 사용 예시
```bash
# 기능 브랜치 생성
git checkout -b feat/social-login

# 긴급 수정 브랜치 생성
git checkout -b hotfix/critical-bug
```

## 🗝️ API 설계 규칙

### RESTful API 표준

#### HTTP 메서드
- `GET`: 데이터 조회
- `POST`: 데이터 생성
- `PUT`: 데이터 수정
- `DELETE`: 데이터 삭제

#### 상태 코드
- `200`: 요청 성공
- `201`: 생성 성공
- `204`: 성공 (응답 데이터 없음)
- `400`: 잘못된 요청
- `401`: 인증 실패
- `403`: 권한 없음
- `404`: 리소스 없음
- `409`: 데이터 충돌
- `500`: 서버 오류

### 엔드포인트 규칙
- 소문자 및 케밥 케이스 사용
- 복수형 리소스 명사 사용
- 예시:
    - `/users/{userId}`
    - `/games/{gameId}/sessions`

### 파라미터 규칙
- 쿼리: 카멜 케이스
    - `?startDate=2024-11-14`
- 경로: 케밥 케이스
    - `/users/{user-id}`

### 데이터베이스 설계
- 테이블명: 복수형
- 컬럼명: 스네이크 케이스
- 기본 컬럼:
    - `id` (PK)
    - `created_at`
    - `updated_at`

## 🗝️ 디렉토리 구조

```
Narrativa_Backend/
├── .github/
│   └── workflows/          # CI/CD 설정
├── config/                 # 서브모듈 설정
├── src/
│   └── main/
│       └── java/com/nova/narrativa/
│           ├── common/     # 공통 모듈
│           └── domain/     # 도메인별 모듈
│               ├── admin/
│               ├── game/
│               ├── llm/
│               ├── notice/
│               ├── tti/
│               ├── ttm/
│               └── user/
└── resources/
```

각 도메인 디렉토리는 다음 구조를 따릅니다:
- `controller/`: API 엔드포인트
- `dto/`: 데이터 전송 객체
- `entity/`: 데이터베이스 모델
- `repository/`: 데이터 접근 계층
- `service/`: 비즈니스 로직

## 🗝️ 팀 정보

### **Part Leader**
  <img src="https://github.com/user-attachments/assets/6e4a6035-db22-414a-b051-b59fd646d9cd" 
       alt="hs" 
       width="200" 
       height="auto" 
       style="max-width: 100%; height: auto;">

### **Team Member**
  <img src="https://github.com/user-attachments/assets/bb285012-1e08-4bd7-9c63-d6f73c80f713" 
       alt="st" 
       width="200" 
       height="auto" 
       style="max-width: 100%; height: auto;">
  <img src="https://github.com/user-attachments/assets/b07709bc-bd82-4401-a5cd-9177e4ee44e6" 
       alt="hy" 
       width="200" 
       height="auto" 
       style="max-width: 100%; height: auto;">

## 🗝️ 문의 및 기여

프로젝트에 대한 문의사항이나 개선 제안은 이슈 탭에 등록해주세요.<br />
기여를 원하시는 분은 Fork & Pull Request를 통해 참여해주시면 감사하겠습니다.

## 🗝️ 라이선스

이 프로젝트는 [MIT 라이선스](LICENSE)를 따릅니다.

<br /><br />
![footer](https://github.com/user-attachments/assets/c30abbd9-8e89-4a4e-8823-33fe0cf843c9)

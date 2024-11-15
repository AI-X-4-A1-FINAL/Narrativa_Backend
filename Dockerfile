# 1단계: 빌드 이미지 (Gradle 사용)
FROM gradle:8.4.0-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트의 모든 파일을 컨테이너로 복사
COPY . .

# 서브모듈 초기화 및 업데이트
RUN git submodule init && git submodule update

# 빌드 실행 (테스트 제외)
RUN gradle clean build -x test

# 2단계: 실행 이미지
FROM openjdk:21-slim

# 타임존 설정 (필요한 경우)
ENV TZ=Asia/Seoul

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 .jar 파일을 app 디렉토리로 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# `config/application.yml` 파일을 실행 이미지로 복사
COPY --from=builder /app/config/application.yml /app/config/application.yml

# Spring Boot가 application.yml을 인식하도록 환경 변수 설정
ENV SPRING_CONFIG_LOCATION=/app/config/application.yml

# 포트 노출 (스프링 부트 기본 포트)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

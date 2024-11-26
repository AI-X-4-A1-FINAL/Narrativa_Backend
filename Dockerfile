# 1단계: 빌드 이미지 (Gradle 사용)
FROM gradle:8.4.0-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사 및 Gradle 빌드 실행
COPY . ./
RUN gradle clean build -x test

# 2단계: 실행 이미지
FROM openjdk:21-slim

# 작업 디렉토리 설정
WORKDIR /app

# AWS CLI 설치
RUN apt-get update && apt-get install -y curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && ./aws/install && \
    rm -rf awscliv2.zip ./aws && apt-get clean

# 빌드 시 전달받을 환경 변수 정의
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG S3_BUCKET_NAME
ARG S3_FILE_KEY

# AWS 환경 변수 설정
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
ENV AWS_DEFAULT_REGION=${AWS_REGION}

# 필수 환경 변수 검증
RUN : "${AWS_ACCESS_KEY_ID:?ERROR: AWS_ACCESS_KEY_ID is not set!}" && \
    : "${AWS_SECRET_ACCESS_KEY:?ERROR: AWS_SECRET_ACCESS_KEY is not set!}" && \
    : "${AWS_DEFAULT_REGION:?ERROR: AWS_DEFAULT_REGION is not set!}" && \
    : "${S3_BUCKET_NAME:?ERROR: S3_BUCKET_NAME is not set!}" && \
    : "${S3_FILE_KEY:?ERROR: S3_FILE_KEY is not set!}"

# S3에서 application.yml 다운로드 및 설정
RUN mkdir -p /app/config && \
    aws s3 cp s3://${S3_BUCKET_NAME}/${S3_FILE_KEY} /app/config/application.yml --region ${AWS_DEFAULT_REGION}

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Spring Boot가 application.yml을 인식하도록 환경 변수 설정
ENV SPRING_CONFIG_LOCATION=/app/config/application.yml

# 기본 포트 노출
EXPOSE 8080

# Spring Boot 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

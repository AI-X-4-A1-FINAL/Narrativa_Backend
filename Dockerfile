# 1단계: 빌드 이미지
FROM gradle:8.4.0-jdk21 AS builder

WORKDIR /app
COPY . ./
RUN gradle clean build -x test

# 2단계: 실행 이미지
FROM openjdk:21-slim

WORKDIR /app

# AWS CLI 설치
RUN apt-get update && \
    apt-get install -y curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && ./aws/install && \
    rm -rf awscliv2.zip ./aws && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 빌드 시 전달받을 환경 변수
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG S3_BUCKET_NAME
ARG S3_FILE_KEY

# 환경 변수 설정
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
ENV AWS_DEFAULT_REGION=${AWS_REGION}
ENV S3_BUCKET_NAME=${S3_BUCKET_NAME}
ENV S3_FILE_KEY=${S3_FILE_KEY}

# 환경 변수 검증
RUN if [ -z "${AWS_ACCESS_KEY_ID}" ]; then \
         echo "ERROR: AWS_ACCESS_KEY_ID is not set!"; exit 1; \
       fi && \
       if [ -z "${AWS_SECRET_ACCESS_KEY}" ]; then \
         echo "ERROR: AWS_SECRET_ACCESS_KEY is not set!"; exit 1; \
       fi && \
       if [ -z "${AWS_DEFAULT_REGION}" ]; then \
         echo "ERROR: AWS_DEFAULT_REGION is not set!"; exit 1; \
       fi && \
       if [ -z "${S3_BUCKET_NAME}" ]; then \
         echo "ERROR: S3_BUCKET_NAME is not set!"; exit 1; \
       fi && \
       if [ -z "${S3_FILE_KEY}" ]; then \
         echo "ERROR: S3_FILE_KEY is not set!"; exit 1; \
       fi

# S3에서 설정 파일 다운로드
RUN mkdir -p /app/config && \
    echo "Downloading configuration file: ${S3_FILE_KEY}" && \
    if ! aws s3 cp s3://${S3_BUCKET_NAME}/${S3_FILE_KEY} /app/config/application.yml --region ${AWS_DEFAULT_REGION}; then \
        echo "ERROR: Failed to download ${S3_FILE_KEY} from S3"; exit 1; \
    fi

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Spring Boot가 application.yml을 인식하도록 설정
ENV SPRING_CONFIG_LOCATION=/app/config/application.yml

# 기본 포트 설정
EXPOSE 8080

# 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
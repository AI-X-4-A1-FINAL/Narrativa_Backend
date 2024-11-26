# 기본 이미지
FROM amazoncorretto:21-alpine as builder

# glibc 및 AWS CLI 설치
RUN apk add --no-cache curl unzip bash binutils && \
    curl -Lo /etc/apk/keys/sgerrand.rsa.pub https://alpine-pkgs.sgerrand.com/sgerrand.rsa.pub && \
    curl -Lo glibc.apk https://github.com/sgerrand/alpine-pkg-glibc/releases/download/2.35-r0/glibc-2.35-r0.apk && \
    apk add --no-cache ./glibc.apk && \
    rm -f glibc.apk && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli && \
    rm -rf awscliv2.zip aws

# AWS CLI 설치 확인
RUN which aws && aws --version


# 빌드 환경 설정
WORKDIR /app

# 소스 복사
COPY . /app

# Docker Compose로 전달받은 빌드 인수
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG S3_BUCKET_NAME
ARG S3_FILE_KEY

# 환경 변수로 설정
ENV AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
ENV AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
ENV AWS_REGION=$AWS_REGION
ENV S3_BUCKET_NAME=$S3_BUCKET_NAME
ENV S3_FILE_KEY=$S3_FILE_KEY

# S3에서 application.yml 다운로드
RUN if [ -n "$S3_BUCKET_NAME" ] && [ -n "$S3_FILE_KEY" ]; then \
        aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID && \
        aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY && \
        aws configure set region $AWS_REGION && \
        mkdir -p src/main/resources && \
        aws s3 cp s3://$S3_BUCKET_NAME/$S3_FILE_KEY src/main/resources/application.yml; \
    else \
        echo "S3_BUCKET_NAME or S3_FILE_KEY is not set, skipping S3 download."; \
    fi

# Gradle 빌드
RUN ./gradlew clean build -x test

# 실행 이미지 준비
FROM amazoncorretto:21-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 결과 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 로그 디렉토리 생성
VOLUME /app/logs

# 컨테이너 실행 시 기본 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

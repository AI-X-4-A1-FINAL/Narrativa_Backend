# 기본 이미지
FROM amazoncorretto:21-alpine as builder

# AWS CLI 설치
RUN apk add --no-cache curl unzip && \
    curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install --bin-dir /usr/local/bin --install-dir /usr/local/aws-cli && \
    ln -sf /usr/local/aws-cli/v2/current/bin/aws /usr/local/bin/aws && \
    rm -rf awscliv2.zip ./aws

# AWS CLI 설치 확인
RUN echo $PATH && aws --version

# 빌드 환경 설정
WORKDIR /app

# 소스 복사
COPY . /app

# S3에서 application.yml 다운로드
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY
ARG AWS_REGION
ARG S3_BUCKET_NAME
ARG S3_FILE_KEY

RUN aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID && \
    aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY && \
    aws configure set region $AWS_REGION && \
    aws s3 cp s3://$S3_BUCKET_NAME/$S3_FILE_KEY src/main/resources/application.yml

# Gradle 빌드
RUN ./gradlew clean build -x test

# 실행 이미지 준비
FROM amazoncorretto:21-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# 로그 디렉토리 생성
VOLUME /app/logs

ENTRYPOINT ["java", "-jar", "app.jar"]

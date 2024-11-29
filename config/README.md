![NARRATIVA-TITLE](https://github.com/user-attachments/assets/97538156-f202-4b48-8543-9bbf835fda0e)
# NARRATIVA_Secrets

### 별도의 프라이빗 리포지토리를 활용하여 application.yml 파일 관리 및 서브모듈 사용
이 방법은 민감한 설정 파일(application.yml)을 별도의 프라이빗 GitHub 리포지토리에서 관리하고, 이를 서브모듈로 메인 프로젝트에 연결하는 방식입니다. 이를 통해 민감한 정보가 메인 프로젝트 코드베이스에 포함되지 않도록 하면서도, CI/CD 파이프라인에서 안전하게 설정 파일을 사용할 수 있습니다.

### ✅ 왜 이 방법을 사용하는가?
보안 강화: 민감한 설정 파일을 별도로 관리하여 메인 리포지토리에 포함되지 않으므로, 외부 접근 시 유출 위험이 줄어듭니다.
독립적인 관리: 설정 파일을 별도의 리포지토리에서 관리하면 변경 이력 추적이 용이하고, 환경 설정을 프로젝트와 독립적으로 업데이트할 수 있습니다.
CI/CD 통합: GitHub Actions와 서브모듈을 활용해 CI/CD 파이프라인에서 자동으로 설정 파일을 불러올 수 있습니다.

### 🛠️ 구현 단계
+ **Step 1**: 별도의 프라이빗 리포지토리 생성
  GitHub에서 새로운 프라이빗 리포지토리를 생성합니다.

  예: config-repo
  해당 리포지토리에 application.yml 파일을 추가합니다.
  
  application.yml 예시:
  ```yaml
  server:
    port: 8080
  
  spring:
    datasource:
      url: jdbc:mysql://192.168.0.96:3306/Narrativa-DB?useSSL=false&serverTimezone=Asia/Seoul
      username: NARRATIVA
      password: N1A2R3R4T5I6V7A8
      driver-class-name: com.mysql.cj.jdbc.Driver
  
    jpa:
      hibernate:
        ddl-auto: update
      show-sql: true
  ```

+ **Step 2**: 메인 프로젝트에 서브모듈 추가
  메인 프로젝트로 이동한 후, 서브모듈을 추가합니다.
  ```bash
  git submodule add https://github.com/your-username/config-repo.git config
  ```
  
  이 명령어는 config 디렉토리 아래에 서브모듈을 추가합니다.
  
  서브모듈 초기화 및 업데이트:
  ```bash
  git submodule init
  git submodule update
  ```

+ **Step 3**: GitHub Actions 워크플로우 설정
  서브모듈을 활용하여 CI/CD 파이프라인에서 application.yml 파일을 복사하고 사용할 수 있도록 GitHub Actions 워크플로우 파일을 작성합니다.

  .github/workflows/deploy.yml 파일 예시:
  
  ```yaml
  name: CI/CD Pipeline
  
  on:
    push:
      branches:
        - main
  
  jobs:
    build:
      runs-on: ubuntu-latest
  
      steps:
        # 코드 체크아웃
        - name: Check out code
          uses: actions/checkout@v2
          with:
            submodules: true  # 서브모듈 포함하여 체크아웃
  
        # application.yml 복사
        - name: Copy application.yml from submodule
          run: cp config/application.yml src/main/resources/application.yml
  
        # 빌드 및 테스트
        - name: Build project
          run: ./gradlew build
  
        # 배포 (필요에 따라 추가)
        - name: Deploy
          run: ./deploy.sh
  ```
+ **Step 4**: 서브모듈 변경 사항 업데이트
  서브모듈에 변경 사항이 발생하면, 메인 프로젝트에서도 업데이트가 필요합니다.
  ```bash
  cd config
  git pull origin main
  cd ..
  git add config
  git commit -m "Update config submodule"
  git push origin main
  ```

+ **Step 5**: 접근 권한 관리
  프라이빗 리포지토리에 대한 읽기 권한이 있는 GitHub Actions 토큰이 필요합니다.
  
  메인 프로젝트의 Settings > Secrets and variables > Actions에서 ```Personal Access Token (PAT)```을 추가하고, 이를 GitHub Actions에서 사용할 수 있도록 설정합니다.
  
  ```yaml
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - name: Check out code
          uses: actions/checkout@v2
          with:
            token: ${{ secrets.GITHUB_TOKEN }}
            submodules: true
  ```

### 🔍 주의 사항
+ 서브모듈 관리:
  서브모듈을 업데이트할 때마다 git submodule update --remote 명령어를 사용하여 최신 상태로 동기화해야 합니다.
+ 토큰 보안 관리:
  GitHub Actions에서 사용할 토큰은 최소 권한의 읽기 전용 토큰을 사용하는 것이 좋습니다.
+ 서브모듈 제거 시:
  ```bash
  git submodule deinit -f config
  git rm -f config
  rm -rf .git/modules/config
  ```
  
<br /><br />
![footer](https://github.com/user-attachments/assets/df9a78ea-0367-4899-b77e-bb20175dc1dc)


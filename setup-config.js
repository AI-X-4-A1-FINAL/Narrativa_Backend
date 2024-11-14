const fs = require('fs');
const path = require('path');

const configFile = path.join(__dirname, 'config', 'application.yml');
const targetFile = path.join(__dirname, 'src', 'main', 'resources', 'application.yml');

console.log('🔄 서브모듈 업데이트 중...');
require('child_process').execSync('git submodule update --init --recursive --remote', { stdio: 'inherit' });

if (fs.existsSync(configFile)) {
    console.log('✅ 서브모듈에서 설정 파일을 찾았습니다.');

    // 기존 파일 삭제
    if (fs.existsSync(targetFile)) {
        fs.unlinkSync(targetFile);
        console.log('🗑️ 기존 설정 파일을 삭제했습니다.');
    }

    // 파일 복사
    fs.copyFileSync(configFile, targetFile);
    console.log(`📋 설정 파일이 복사되었습니다: ${targetFile}`);

    console.log('✅ 설정이 완료되었습니다.');
} else {
    console.error('❌ 설정 파일을 찾을 수 없습니다. 서브모듈을 확인하세요.');
    process.exit(1);
}

package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.dto.UserExistenceDto;
import com.nova.narrativa.domain.user.dto.UserProfileInfo;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class SignUpService {

    private final UserRepository userRepository;

    public void register(SignUp signUp) {
        // DB 조회 후, 해당 계정 중복 체크
        if(userRepository.existsByUserIdAndLoginType(signUp.getUser_id(), User.LoginType.valueOf(signUp.getLogin_type()))){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 유저는 이미 회원가입이 완료되었습니다.");
        } else {
            User signUpUser = new User();
            signUpUser.setUserId(signUp.getUser_id());
            signUpUser.setUsername(signUp.getUsername());
            signUpUser.setProfile_url(signUp.getProfile_url());
            signUpUser.setLoginType(User.LoginType.valueOf(signUp.getLogin_type()));

            User savedUser = userRepository.save(signUpUser);
            log.info(String.format("회원 가입 완료: %s", savedUser));
        }
    }

    // 로그인한 유저의 ID와 RequestParam으로 받은 id를 비교하는 로직
    public String isLoggedInUser(Long id) {

        System.out.println("id = " + id);
        Optional<User> loggedInUser = userRepository.findById(id);

        // 로그인한 유저의 ID와 요청된 id를 비교

        if (loggedInUser.isPresent() && loggedInUser.get().getId().equals(id)) {
            updateStatusToInactive(id);
            return loggedInUser.get().getUsername();
        } else {
            return null;
        }
    }

    // 주어진 id에 해당하는 사용자의 status 값을 Inactive로 업데이트하는 메소드
    public void updateStatusToInactive(Long id) {
        // id로 User를 찾기
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(User.Status.INACTIVE);  // 회원탈퇴시 status 변경(ACTIVE -> INACTIVE)

            userRepository.save(user);  // 변경된 user 객체를 저장
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    // 주어진 id에 해당하는 사용자의 status 값을 Active로 업데이트하는 메소드
    public void updateStatusToActive(Long id) {
        // id로 User를 찾기
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setStatus(User.Status.ACTIVE);  // 회원탈퇴시 status 변경(ACTIVE -> INACTIVE)

            userRepository.save(user);  // 변경된 user 객체를 저장
        } else {
            throw new RuntimeException("User not found with id: " + id);
        }
    }

    public ResponseEntity<Object> updateUser(Long userId, UserProfileInfo updateUser) {
        // 기존 사용자 조회
        Optional<User> existingUserOptional = userRepository.findById(userId);
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User existingUser = existingUserOptional.get();
        log.info("existingUser: {}", existingUser);

        // 변경된 필드만 업데이트
        if (updateUser.getNickname() != null && !updateUser.getNickname().equals(existingUser.getUsername())) {
            existingUser.setUsername(updateUser.getNickname());
        }
        if (updateUser.getProfile_url() != null && !updateUser.getProfile_url().equals(existingUser.getProfile_url())) {
            existingUser.setProfile_url(updateUser.getProfile_url());
        }

        userRepository.save(existingUser);
        return ResponseEntity.ok(existingUser);
    }

    public boolean isUserActive(Long userId) {
        // userId가 유효한지 체크
        if (StringUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("유저 ID가 비어있습니다.");
        }

        // userId로 유저를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(String.format("유저아이디가 %d 인 유저는 존재하지 않습니다.", userId)));

        // 유저의 상태가 ACTIVE인지 확인
        return user.getStatus() == User.Status.ACTIVE;// ACTIVE 상태면 true 반환
    }


    public boolean isUserExist(UserExistenceDto userExistenceDto) {
        log.info("userExistenceDto.getUserId(): {}", userExistenceDto.getUserId());
        log.info("userExistenceDto.getLoginType(): {}", userExistenceDto.getLoginType());
        log.info("boolean: {}", userRepository.existsByUserIdAndLoginType(userExistenceDto.getUserId(), userExistenceDto.getLoginType()));
        return userRepository.existsByUserIdAndLoginType(userExistenceDto.getUserId(), userExistenceDto.getLoginType());
    }

    public Optional<User> getUserId(UserExistenceDto userExistenceDto) {
        return userRepository.findIdByUserIdAndLoginType(userExistenceDto.getUserId(), userExistenceDto.getLoginType());
    }

    public Optional<User> getUserProfileInfo(Long userId) {
        return userRepository.findById(userId);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class SignUpService {

    private final UserRepository userRepository;

    public void register(SignUp signUp) {
        // 이메일 중복 체크
        Optional<User> existingUser = userRepository.findByEmail(signUp.getEmail());
        if (existingUser.isPresent()) {
            // 이미 존재하는 이메일이라면 400 에러를 던짐
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 이메일로 가입된 유저가 이미 존재합니다.");
        }

        User signUpUser = new User();
        signUpUser.setEmail(signUp.getEmail());
        signUpUser.setUsername(signUp.getUsername());
        signUpUser.setProfile(signUp.getProfile());
        signUpUser.setProfile_url(signUp.getProfile_url());

        // 이메일이 없으면 새로운 회원가입 진행
        userRepository.save(signUpUser);
    }

    // 로그인한 유저의 ID와 RequestParam으로 받은 id를 비교하는 로직
    public String isLoggedInUser(Long id) {
//        // SecurityContext에서 로그인한 사용자 정보 가져오기
//        UserDetails currentUser = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//        // 현재 로그인한 유저의 username (혹은 이메일 등)을 사용하여 해당 유저를 조회
//        String currentUsername = currentUser.getUsername();
//        Optional<User> loggedInUser = userRepository.findByUsername(currentUsername);
//        log.info("loggedInUser: {}", loggedInUser);

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

    public ResponseEntity<Object> updateUser(Long userId, User updateUser) {
        // 기존 사용자 조회
        Optional<User> existingUserOptional = userRepository.findById(userId);
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User existingUser = existingUserOptional.get();

        // 변경된 필드만 업데이트
        if (updateUser.getUsername() != null && !updateUser.getUsername().equals(existingUser.getUsername())) {
            existingUser.setUsername(updateUser.getUsername());
        }
        if (updateUser.getEmail() != null && !updateUser.getEmail().equals(existingUser.getEmail())) {
            existingUser.setEmail(updateUser.getEmail());
        }
        if (updateUser.getProfile() != null && !updateUser.getProfile().equals(existingUser.getProfile())) {
            existingUser.setProfile(updateUser.getProfile());
        }
        if (updateUser.getProfile_url() != null && !updateUser.getProfile_url().equals(existingUser.getProfile_url())) {
            existingUser.setProfile_url(updateUser.getProfile_url());
        }

        userRepository.save(existingUser);
        return ResponseEntity.ok(existingUser);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

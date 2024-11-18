package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.SignUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SignUpService {

    private final SignUpRepository signUpRepository;

    public void register(SignUp signUp) {
        // 이메일 중복 체크
        Optional<User> existingUser = signUpRepository.findByEmail(signUp.getEmail());
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
        signUpRepository.save(signUpUser);
    }
}

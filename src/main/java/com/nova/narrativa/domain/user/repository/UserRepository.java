package com.nova.narrativa.domain.user.repository;

import com.nova.narrativa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    // userId, loginType으로 존재 여부 확인
    boolean existsByUserIdAndLoginType(String user_id, User.LoginType loginType);

    // login user의 id 얻기
    Optional<User> findIdByUserIdAndLoginType(@Param("userId") String userId, @Param("loginType") User.LoginType loginType);
}
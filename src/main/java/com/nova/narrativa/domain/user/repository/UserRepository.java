package com.nova.narrativa.domain.user.repository;

import com.nova.narrativa.domain.user.entity.LoginType;
import com.nova.narrativa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long id);

    // userId, loginType으로 존재 여부 확인
    boolean existsByUserIdAndLoginType(Long user_id, LoginType loginType);
}

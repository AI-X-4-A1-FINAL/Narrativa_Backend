package com.nova.narrativa.domain.user.repository;

import com.nova.narrativa.domain.user.dto.SignUp;
import com.nova.narrativa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignUpRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}

package com.nova.narrativa.domain.user.service;

import com.nova.narrativa.domain.user.dto.UserDTO;
import com.nova.narrativa.domain.user.entity.User;
import com.nova.narrativa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserDTO> getUsers(int page, int size, String username) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("페이지와 사이즈는 양수여야 합니다.");
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<User> spec = (root, query, cb) -> {
            if (username != null && !username.trim().isEmpty()) {
                return cb.like(cb.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%");
            }
            return null;
        };

        return userRepository.findAll(spec, pageRequest)
                .map(this::convertToDTO);
    }

    public UserDTO updateRole(Long id, User.Role role) {
        User user = findUser(id);
        user.setRole(role);
        return convertToDTO(user);
    }

    public UserDTO updateStatus(Long id, User.Status status) {
        User user = findUser(id);
        user.setStatus(status);
        return convertToDTO(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .profileUrl(user.getProfile_url())
                .role(user.getRole())
                .status(user.getStatus())
                .loginType(user.getLoginType())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
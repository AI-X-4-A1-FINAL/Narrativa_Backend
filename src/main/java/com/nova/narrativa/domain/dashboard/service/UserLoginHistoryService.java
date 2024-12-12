package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.dto.ActiveUsersDTO;
import com.nova.narrativa.domain.dashboard.entity.UserLoginHistory;
import com.nova.narrativa.domain.dashboard.repository.UserLoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class UserLoginHistoryService {
    private final UserLoginHistoryRepository loginHistoryRepository;

    public void recordLogin(String userId) {
        UserLoginHistory loginHistory = UserLoginHistory.createLoginHistory(userId);
        loginHistoryRepository.save(loginHistory);
    }

    @Transactional(readOnly = true)
    public ActiveUsersDTO getActiveUsersStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.minusDays(30);
        return loginHistoryRepository.getActiveUsersStats(today, monthStart);
    }
}

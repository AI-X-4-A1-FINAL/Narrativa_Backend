package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.dto.ActiveUsersDTO;
import com.nova.narrativa.domain.dashboard.entity.UserLoginHistory;
import com.nova.narrativa.domain.dashboard.repository.UserLoginHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        LocalDate now = LocalDate.now();

        // 현재 월의 모든 일자 데이터 조회
        List<UserLoginHistoryRepository.DailyStatsProjection> dauStats =
                loginHistoryRepository.getDailyActiveUsers(now);

        // 현재 연도의 모든 월 데이터 조회
        List<UserLoginHistoryRepository.DailyStatsProjection> mauStats =
                loginHistoryRepository.getMonthlyActiveUsers(now);

        return ActiveUsersDTO.builder()
                .dauStats(convertToDailyStats(dauStats))
                .mauStats(convertToDailyStats(mauStats))
                .build();
    }

    private List<ActiveUsersDTO.DailyStats> convertToDailyStats(
            List<UserLoginHistoryRepository.DailyStatsProjection> projections
    ) {
        return projections.stream()
                .map(p -> ActiveUsersDTO.DailyStats.builder()
                        .date(p.getDate())
                        .count(p.getCount())
                        .build())
                .collect(Collectors.toList());
    }
}

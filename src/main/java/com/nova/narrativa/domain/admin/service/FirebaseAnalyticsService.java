package com.nova.narrativa.domain.admin.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FirebaseAnalyticsService {

    private final FirebaseApp firebaseApp;
    private final RestTemplate restTemplate = new RestTemplate();

    // 장르별 게임 실행 통계
    public Map<String, Object> getGameStatsByGenre() {
        try {
            String projectId = firebaseApp.getOptions().getProjectId();
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            String accessToken = credentials.refreshAccessToken().getTokenValue();

            // genre별 실행 횟수와 평균 시간을 가져오는 쿼리
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dimensions", Arrays.asList("genre"));
            requestBody.put("metrics", Arrays.asList(
                    Map.of("name", "game_start_count"),
                    Map.of("name", "average_game_duration")
            ));

            return fetchAnalyticsData(projectId, accessToken, requestBody);
        } catch (IOException e) {
            log.error("장르별 통계 조회 실패", e);
            return Collections.emptyMap();
        }
    }

    // 실시간 접속자 수
    public int getActiveUsers() {
        try {
            String projectId = firebaseApp.getOptions().getProjectId();
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            String accessToken = credentials.refreshAccessToken().getTokenValue();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("metrics", Arrays.asList(
                    Map.of("name", "activeUsers")
            ));

            Map<String, Object> result = fetchAnalyticsData(projectId, accessToken, requestBody);
            return extractActiveUsersCount(result);
        } catch (IOException e) {
            log.error("실시간 사용자 수 조회 실패", e);
            return 0;
        }
    }

    // 페이지별 방문 통계
    public Map<String, Integer> getPageVisits() {
        try {
            String projectId = firebaseApp.getOptions().getProjectId();
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            String accessToken = credentials.refreshAccessToken().getTokenValue();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dimensions", Arrays.asList("page_path"));
            requestBody.put("metrics", Arrays.asList(
                    Map.of("name", "screenPageViews")
            ));

            return extractPageVisits(fetchAnalyticsData(projectId, accessToken, requestBody));
        } catch (IOException e) {
            log.error("페이지 방문 통계 조회 실패", e);
            return Collections.emptyMap();
        }
    }

    // 시간대별 접속자 통계
    public Map<String, Integer> getHourlyActiveUsers() {
        try {
            String projectId = firebaseApp.getOptions().getProjectId();
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            String accessToken = credentials.refreshAccessToken().getTokenValue();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("dimensions", Arrays.asList("hour"));
            requestBody.put("metrics", Arrays.asList(
                    Map.of("name", "activeUsers")
            ));

            return extractHourlyStats(fetchAnalyticsData(projectId, accessToken, requestBody));
        } catch (IOException e) {
            log.error("시간대별 통계 조회 실패", e);
            return Collections.emptyMap();
        }
    }

    // Firebase Analytics API 호출 메서드
    private Map<String, Object> fetchAnalyticsData(String projectId, String accessToken, Map<String, Object> requestBody) {
        String url = String.format("https://firebase.googleapis.com/v1beta1/projects/%s/analyticsData:runReport", projectId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Firebase Analytics API 호출 실패", e);
            return Collections.emptyMap();
        }
    }

    // 실시간 접속자 수 추출 메서드
    private int extractActiveUsersCount(Map<String, Object> result) {
        try {
            if (result != null && result.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");
                if (!rows.isEmpty()) {
                    Map<String, Object> firstRow = rows.get(0);
                    List<Map<String, Object>> metricValues = (List<Map<String, Object>>) firstRow.get("metricValues");
                    if (!metricValues.isEmpty()) {
                        return Integer.parseInt(metricValues.get(0).get("value").toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Active users count extraction failed", e);
        }
        return 0;
    }

    // 페이지 방문 통계 추출 메서드
    private Map<String, Integer> extractPageVisits(Map<String, Object> result) {
        Map<String, Integer> pageVisits = new HashMap<>();
        try {
            if (result != null && result.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");
                for (Map<String, Object> row : rows) {
                    List<Map<String, Object>> dimensionValues = (List<Map<String, Object>>) row.get("dimensionValues");
                    List<Map<String, Object>> metricValues = (List<Map<String, Object>>) row.get("metricValues");

                    String pagePath = dimensionValues.get(0).get("value").toString();
                    int visits = Integer.parseInt(metricValues.get(0).get("value").toString());
                    pageVisits.put(pagePath, visits);
                }
            }
        } catch (Exception e) {
            log.error("Page visits extraction failed", e);
        }
        return pageVisits;
    }

    // 시간대별 통계 추출 메서드
    private Map<String, Integer> extractHourlyStats(Map<String, Object> result) {
        Map<String, Integer> hourlyStats = new HashMap<>();
        try {
            if (result != null && result.containsKey("rows")) {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");
                for (Map<String, Object> row : rows) {
                    List<Map<String, Object>> dimensionValues = (List<Map<String, Object>>) row.get("dimensionValues");
                    List<Map<String, Object>> metricValues = (List<Map<String, Object>>) row.get("metricValues");

                    String hour = dimensionValues.get(0).get("value").toString();
                    int users = Integer.parseInt(metricValues.get(0).get("value").toString());
                    hourlyStats.put(hour, users);
                }
            }
        } catch (Exception e) {
            log.error("Hourly stats extraction failed", e);
        }
        return hourlyStats;
    }
}
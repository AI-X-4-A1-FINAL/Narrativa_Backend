package com.nova.narrativa.domain.llm.repository;

import com.nova.narrativa.domain.dashboard.dto.GamePlaytimeDTO;
import com.nova.narrativa.domain.dashboard.dto.GenrePlaytimeDTO;
import com.nova.narrativa.domain.llm.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StageRepository extends JpaRepository<Stage, Long> {

    // 특정 게임의 모든 스테이지 조회
    List<Stage> findByGame_GameId(Long gameId);

    // 가장 높은 스테이지 번호를 가진 스테이지 조회 (특정 게임)
    Optional<Stage> findTopByGame_GameIdOrderByStageNumberDesc(Long gameId);

    // gameId와 stageNumber로 Stage 엔터티 찾기
    Optional<Stage> findByGame_GameIdAndStageNumber(Long gameId, int stageNumber);

    @Query(nativeQuery = true, value = """
        SELECT 
            g.game_id as gameId,
            COALESCE(AVG(
                TIMESTAMPDIFF(SECOND, 
                    (SELECT MIN(s2.start_time) 
                     FROM stage s2 
                     WHERE s2.game_id = g.game_id),
                    (SELECT s3.end_time 
                     FROM stage s3 
                     WHERE s3.game_id = g.game_id 
                     AND s3.stage_number = 6
                     AND s3.end_time IS NOT NULL)
                )
            ), 0) as averagePlaytimeInSeconds
        FROM stage s
        JOIN game g ON s.game_id = g.game_id
        WHERE EXISTS (
            SELECT 1 
            FROM stage s4 
            WHERE s4.game_id = g.game_id 
            AND s4.stage_number = 6 
            AND s4.end_time IS NOT NULL
        )
        GROUP BY g.game_id
    """)
    List<GamePlaytimeDTO> getAveragePlaytimePerGame();

    @Query(nativeQuery = true, value = """
    WITH GamePlaytime AS (
        SELECT 
            g.game_id,
            g.genre,
            TIMESTAMPDIFF(SECOND, 
                (SELECT MIN(s2.start_time) 
                 FROM stage s2 
                 WHERE s2.game_id = g.game_id),
                (SELECT s3.end_time 
                 FROM stage s3 
                 WHERE s3.game_id = g.game_id 
                 AND s3.stage_number = 6
                 AND s3.end_time IS NOT NULL)
            ) as playtime
        FROM game g
        WHERE EXISTS (
            SELECT 1 
            FROM stage s4 
            WHERE s4.game_id = g.game_id 
            AND s4.stage_number = 6 
            AND s4.end_time IS NOT NULL
        )
    )
    SELECT 
        gp.genre as genre,
        COALESCE(AVG(gp.playtime), 0) as averagePlaytimeInSeconds
    FROM GamePlaytime gp
    GROUP BY gp.genre
    """)
    List<GenrePlaytimeDTO> getAveragePlaytimePerGenre();

    @Query(nativeQuery = true, value = """
    SELECT COALESCE(
        TIMESTAMPDIFF(SECOND, 
            (SELECT MIN(s2.start_time) 
             FROM stage s2 
             WHERE s2.game_id = :gameId),
            (SELECT s3.end_time 
             FROM stage s3 
             WHERE s3.game_id = :gameId 
             AND s3.stage_number = 6
             AND s3.end_time IS NOT NULL)
        ), 0)
    FROM stage s
    WHERE s.game_id = :gameId
    AND EXISTS (
        SELECT 1 
        FROM stage s4 
        WHERE s4.game_id = :gameId 
        AND s4.stage_number = 6 
        AND s4.end_time IS NOT NULL
    )
    LIMIT 1
""")
    Double getAveragePlaytimeForGame(@Param("gameId") Long gameId);

    // 히스토리 조회
    @Query(nativeQuery = true, value = """
    SELECT 
        g.game_id AS gameId,
        g.genre AS genre,
        MAX(CASE WHEN s.stage_number = 5 THEN s.image_url END) AS imageUrl,
        MAX(CASE WHEN s.stage_number = 6 THEN s.story END) AS story
    FROM game g
    LEFT JOIN stage s ON g.game_id = s.game_id
    WHERE g.user_id = :userId
    GROUP BY g.game_id, g.genre
    HAVING MAX(CASE WHEN s.stage_number = 6 THEN 1 ELSE 0 END) = 1
""")
    List<Map<String, Object>> findGameStagesWithUserId(@Param("userId") Long userId);

}
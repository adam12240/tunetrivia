package com.tunetrivia.backend.repo;

import com.tunetrivia.backend.model.PlayStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayStatRepository extends JpaRepository<PlayStat, Long> {
    List<PlayStat> findByUserId(String userId);

    // Count helpers
    long countByUserId(String userId);
    long countByUserIdAndCorrectTrue(String userId);

    // Compute average skips per song for a user: average of per-track averages (native SQL)
    @Query(value = "SELECT AVG(sub.avg_skips) FROM (SELECT track_id, AVG(skips) AS avg_skips FROM play_stat WHERE user_id = :userId AND skips IS NOT NULL GROUP BY track_id) sub", nativeQuery = true)
    Double findAverageSkipsPerSongByUser(@Param("userId") String userId);
}

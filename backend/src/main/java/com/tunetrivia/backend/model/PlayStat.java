package com.tunetrivia.backend.model;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayStat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId; // Google sub

    private String genre;

    private String trackId;

    private String artist;

    private String title;

    private boolean correct;

    private Instant playedAt;

    // Number of skips the user made for this play (nullable if unknown)
    private Integer skips;
}

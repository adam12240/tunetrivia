package com.tunetrivia.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsSummary {
    private long totalPlays;
    private long totalCorrect;
    private double averageSkipsPerSong; // average skips aggregated per trackId
}

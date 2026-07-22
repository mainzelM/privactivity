package net.privactivity.store.unified;

import net.privactivity.domain.Activity;

public record SummaryDTO(Float trainingStressScore,
                         Float totalTrainingEffect,
                         Float intensityFactor,
                         Byte avgLeftPco,
                         Byte avgRightPco) {
    public static SummaryDTO empty() {
        return new SummaryDTO(null, null, null, null, null);
    }

    public SummaryDTO(Activity.Summary summary) {
        this(summary == null ? null : summary.trainingStressScore().orElse(null),
             summary == null ? null : summary.totalTrainingEffect().orElse(null),
             summary == null ? null : summary.intensityFactor().orElse(null),
             summary == null ? null : summary.avgLeftPco().orElse(null),
             summary == null ? null : summary.avgRightPco().orElse(null));
    }
}
package net.privactivity.domain;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTest {

    @Test
    void returnsDayOfWeekFromStart() {
        Activity testee = Activity.builder()
                .title("title")
                .start(ZonedDateTime.parse("2026-09-24T06:58:51+02:00[Europe/Berlin]"))
                .build();

        assertThat(testee.getDayOfWeek()).isEqualTo(DayOfWeek.THURSDAY);
    }
}
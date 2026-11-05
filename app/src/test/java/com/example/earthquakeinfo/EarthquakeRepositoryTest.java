package com.example.earthquakeinfo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EarthquakeRepositoryTest {

    // 2026-11-05 08:37:12 UTC
    private static final long NOW = 1_793_867_832_000L;

    @Test
    public void startTime_sevenDays_isRoundedToFullHour() {
        assertEquals("2026-10-29T08:00:00", EarthquakeRepository.startTimeFor(7, NOW));
    }

    @Test
    public void startTime_oneDay_isYesterdaySameHour() {
        assertEquals("2026-11-04T08:00:00", EarthquakeRepository.startTimeFor(1, NOW));
    }

    @Test
    public void startTime_thirtyDays_doesNotOverflow() {
        assertEquals("2026-10-06T08:00:00", EarthquakeRepository.startTimeFor(30, NOW));
    }
}

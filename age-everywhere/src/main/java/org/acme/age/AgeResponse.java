package org.acme.age;

import java.util.List;

public record AgeResponse(
        String inputDob,
        String timezone,
        long epochSecondsLived,
        long daysLived,
        String isoPeriod,
        String humanPeriod,
        long ageYearsFloor,
        PlanetaryAge planets,
        CalendarBreakdown calendars,
        List<WorldTime> worldTimes) {
}
package org.acme.age;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.HijrahDate;
import java.time.chrono.JapaneseDate;
import java.time.chrono.MinguoDate;
import java.time.chrono.ThaiBuddhistDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.ibm.icu.util.ChineseCalendar;

public class AgeService {

    private static final double MERCURY = 0.2408467;
    private static final double VENUS = 0.61519726;
    private static final double EARTH = 1.0;
    private static final double JUPITER = 11.862615;
    private static final double SATURN = 29.447498;
    private static final double URANUS = 84.016846;
    private static final double NEPTUNE = 164.79132;

    private static final double MARS_SOL_SECONDS = 88775.244;

    public AgeResponse compute(LocalDate dob, ZoneId zone) {
        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime birth = dob.atStartOfDay(zone);

        long totalSeconds = ChronoUnit.SECONDS.between(birth, now);
        long days = ChronoUnit.DAYS.between(birth, now);
        Period period = Period.between(dob, now.toLocalDate());
        String human = "%d years, %d months, %d days".formatted(
                period.getYears(), period.getMonths(), period.getDays());

        double earthYears = totalSeconds / (365.2425 * 24 * 3600.0);
        PlanetaryAge pa = new PlanetaryAge(
                earthYears / MERCURY,
                earthYears / VENUS,
                earthYears / EARTH,
                (totalSeconds / MARS_SOL_SECONDS) / 668.5991,
                earthYears / JUPITER,
                earthYears / SATURN,
                earthYears / URANUS,
                earthYears / NEPTUNE);

        CalendarBreakdown cb = calendars(dob, zone);
        List<WorldTime> wt = worldTimes(dob, zone);

        return new AgeResponse(
                dob.toString(),
                zone.toString(),
                totalSeconds,
                days,
                period.toString(),
                human,
                period.getYears(),
                pa,
                cb,
                wt);
    }

    private CalendarBreakdown calendars(LocalDate dob, ZoneId zone) {
        DateTimeFormatter f = DateTimeFormatter.ISO_LOCAL_DATE;

        JapaneseDate jp = JapaneseDate.from(dob);
        MinguoDate mg = MinguoDate.from(dob);
        ThaiBuddhistDate th = ThaiBuddhistDate.from(dob);
        HijrahDate hj = HijrahDate.from(dob);

        ZonedDateTime zdt = dob.atStartOfDay(zone);
        long millis = zdt.toInstant().toEpochMilli();

        ChineseCalendar cc = new ChineseCalendar();
        cc.setTimeInMillis(millis);
        int cy = cc.get(ChineseCalendar.EXTENDED_YEAR);
        int cm = cc.get(ChineseCalendar.MONTH) + 1;
        int cd = cc.get(ChineseCalendar.DAY_OF_MONTH);
        boolean leap = cc.get(ChineseCalendar.IS_LEAP_MONTH) == 1;
        String chinese = "Y%04d-%s%02d-%02d".formatted(
                cy, leap ? "L" : "", cm, cd);

        return new CalendarBreakdown(
                dob.format(f),
                jp.toString(),
                mg.toString(),
                th.toString(),
                hj.toString(),
                chinese,
                "Chinese calendar is lunisolar. Leap months may occur.");
    }

    private List<WorldTime> worldTimes(LocalDate dob, ZoneId originalZone) {
        ZonedDateTime birth = dob.atStartOfDay(originalZone);
        Instant instant = birth.toInstant();

        return List.of(
                new WorldTime("Berlin", "Europe/Berlin", instant.atZone(ZoneId.of("Europe/Berlin")).toString()),
                new WorldTime("New York", "America/New_York", instant.atZone(ZoneId.of("America/New_York")).toString()),
                new WorldTime("Tokyo", "Asia/Tokyo", instant.atZone(ZoneId.of("Asia/Tokyo")).toString()),
                new WorldTime("São Paulo", "America/Sao_Paulo",
                        instant.atZone(ZoneId.of("America/Sao_Paulo")).toString()),
                new WorldTime("Sydney", "Australia/Sydney", instant.atZone(ZoneId.of("Australia/Sydney")).toString()));
    }
}
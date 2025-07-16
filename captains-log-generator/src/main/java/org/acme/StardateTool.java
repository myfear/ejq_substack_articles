package org.acme;

import java.time.LocalDate;
import java.time.Month;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StardateTool {

    private static final double BASE_CONSTANT = 58000.00;
    private static final int BASE_YEAR = 2025;

    @Tool("Returns Stardate based on today's date.")
    public String getTodaysStardate() {
        LocalDate today = LocalDate.now();

        int year = today.getYear();
        int day = today.getDayOfMonth();
        boolean isLeap = today.isLeapYear();
        int daysInYear = isLeap ? 366 : 365;

        int m = getAdjustedMonthNumber(today.getMonth(), isLeap);

        double stardate = BASE_CONSTANT
                + (1000.0 * (year - BASE_YEAR))
                + ((1000.0 / daysInYear) * (m + day - 1));
        Log.infof("Stardate: %s", stardate);
        return String.format("Stardate %.2f", stardate);
    }

    public int getAdjustedMonthNumber(Month month, boolean isLeap) {
        return switch (month) {
            case JANUARY -> 0;
            case FEBRUARY -> 31;
            case MARCH -> isLeap ? 60 : 59;
            case APRIL -> isLeap ? 91 : 90;
            case MAY -> isLeap ? 121 : 120;
            case JUNE -> isLeap ? 152 : 151;
            case JULY -> isLeap ? 182 : 181;
            case AUGUST -> isLeap ? 213 : 212;
            case SEPTEMBER -> isLeap ? 244 : 243;
            case OCTOBER -> isLeap ? 274 : 273;
            case NOVEMBER -> isLeap ? 305 : 304;
            case DECEMBER -> isLeap ? 335 : 334;
        };
    }

}

package org.acme;

import java.time.DayOfWeek;
import java.time.LocalDate;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DateTool {

    @Tool("Returns the current weekday mood.")
    public String getWeekdayMood() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        Log.infof("Day of week: %s", day);
        return switch (day) {
            case MONDAY -> "Monday. Morale is low. Systems reboot slowly. Coffee reserves critically low.";
            case TUESDAY -> "Tuesday. The crew has accepted their fate. Productivity stabilizing.";
            case WEDNESDAY -> "Wednesday. Captain calls it 'The Void'. Navigational charts show signs of hope.";
            case THURSDAY -> "Thursday. Spirits rise. Jenkins wears socks with spaceships.";
            case FRIDAY -> "Friday. Celebration imminent. Warp cores warming up for weekend retreat.";
            case SATURDAY -> "Saturday. The ship is quiet. Recreation mode enabled.";
            case SUNDAY -> "Sunday. Maintenance day. AI systems suggest grill routines.";
        };
    }

}
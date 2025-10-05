package org.acme.event;

import java.time.LocalDate;

import org.acme.validation.ChronologicalDates;

@ChronologicalDates
public class Event {
    private LocalDate start;
    private LocalDate end;

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }
}
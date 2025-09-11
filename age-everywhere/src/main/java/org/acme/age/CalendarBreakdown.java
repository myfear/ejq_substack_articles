package org.acme.age;

public record CalendarBreakdown(
        String iso,
        String japanese,
        String minguo,
        String thaiBuddhist,
        String hijrah,
        String chinese,
        String note) {
}

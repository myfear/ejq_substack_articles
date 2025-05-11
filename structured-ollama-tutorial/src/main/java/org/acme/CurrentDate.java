package org.acme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CurrentDate {
    public static final String CURRENT_DATE_STR = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
}

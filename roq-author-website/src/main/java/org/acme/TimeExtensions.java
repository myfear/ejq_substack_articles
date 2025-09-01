package org.acme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.quarkus.qute.TemplateExtension;

@TemplateExtension
public class TimeExtensions {

    /**
     * Return the current date formatted as yyyy-MM-dd
     */
    static String date(Object any) {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * Return the current year
     */
    static int year(Object any) {
        return LocalDate.now().getYear();
    }
}

package org.acme;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import io.quarkus.qute.TemplateGlobal;

@TemplateGlobal
public class GlobalVariables {

    /**
     * Return the current date formatted as yyyy-MM-dd
     */
    static String date() {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * Return the current year
     */
    static int year() {
        return LocalDate.now().getYear();
    }
}
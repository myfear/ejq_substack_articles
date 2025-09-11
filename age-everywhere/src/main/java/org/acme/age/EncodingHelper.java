package org.acme.age;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.inject.Named;

@Named
public class EncodingHelper {


    public String encodeURIComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

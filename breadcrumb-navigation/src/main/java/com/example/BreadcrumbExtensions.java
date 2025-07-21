package com.example;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.qute.TemplateExtension;
import jakarta.ws.rs.core.UriInfo;

@TemplateExtension
public class BreadcrumbExtensions {

    public record Crumb(String label, String url, boolean isLast) {
    }

    public static List<Crumb> breadcrumbs(UriInfo uriInfo) {
        List<Crumb> crumbs = new ArrayList<>();
        String path = uriInfo.getPath();

        String[] segments = path.replaceAll("^/|/$", "").split("/");

        if (segments.length == 1 && segments[0].isEmpty()) {
            crumbs.add(new Crumb("Home", "/", true));
            return crumbs;
        }

        crumbs.add(new Crumb("Home", "/", false));

        // Check if this is a product details page
        boolean isProductDetailsPage = segments.length >= 3 &&
                "products".equals(segments[0]) &&
                "details".equals(segments[segments.length - 1]);

        if (!isProductDetailsPage) {
            // Fallback to original behavior for non-product pages
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if (segment.isEmpty())
                    continue;

                pathBuilder.append("/").append(segment);
                boolean isLast = (i == segments.length - 1);
                String label = capitalize(segment);

                crumbs.add(new Crumb(label, pathBuilder.toString(), isLast));
            }
        } else {
            // Special handling for product details pages
            StringBuilder pathBuilder = new StringBuilder();

            // Process all segments except "details"
            for (int i = 0; i < segments.length - 1; i++) {
                String segment = segments[i];
                if (segment.isEmpty())
                    continue;

                pathBuilder.append("/").append(segment);
                boolean isLast = (i == segments.length - 2); // -2 because we're skipping "details"
                String label = capitalize(segment);

                String url;
                if (i == 0 && "products".equals(segment)) {
                    // Products root - make it non-clickable by setting url to null
                    url = null;
                } else if (i >= 1) {
                    // For product and category segments, create valid detail links
                    url = pathBuilder.toString() + "/details";
                } else {
                    url = pathBuilder.toString();
                }

                crumbs.add(new Crumb(label, url, isLast));
            }
        }

        return crumbs;
    }

    private static String capitalize(String segment) {
        String[] words = segment.replace('-', ' ').split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}
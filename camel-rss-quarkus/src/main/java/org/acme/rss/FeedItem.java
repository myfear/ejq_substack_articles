package org.acme.rss;

import java.time.OffsetDateTime;

public record FeedItem(
        String title,
        String link,
        String author,
        OffsetDateTime published) {
}
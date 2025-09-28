package org.acme.rss;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RssRoute extends RouteBuilder {

    @Inject
    public ObjectMapper mapper;
    
    // Track processed items to avoid duplicate counting
    private final Set<String> processedItems = ConcurrentHashMap.newKeySet();

    @Override
    public void configure() {
        JacksonDataFormat json = new JacksonDataFormat(mapper, FeedItem.class);

        from("{{app.source.uri}}?splitEntries=false&delay=60000")
                .routeId("quarkus-rss-poller")
                .log("Fetched RSS feed: ${body.title}")
                .process(exchange -> {
                    Object body = exchange.getMessage().getBody();

                    if (body instanceof SyndFeed) {
                        SyndFeed feed = (SyndFeed) body;
                        log.info("Processing feed: " + feed.getTitle() + " with " + feed.getEntries().size()
                                + " entries");

                        // Process each entry in the feed
                        int processedCount = 0;
                        int skippedCount = 0;
                        for (SyndEntry entry : feed.getEntries()) {
                            String author = entry.getAuthor() != null ? entry.getAuthor() : "unknown";
                            String month = "unknown";

                            if (entry.getPublishedDate() != null) {
                                OffsetDateTime publishedDate = entry.getPublishedDate().toInstant()
                                        .atOffset(java.time.ZoneOffset.UTC);
                                month = publishedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                            }

                            // Create a unique key for deduplication (title + link + published date)
                            String uniqueKey = entry.getTitle() + "|" + entry.getLink() + "|" + 
                                (entry.getPublishedDate() != null ? entry.getPublishedDate().getTime() : "no-date");
                            
                            // Skip if already processed
                            if (processedItems.contains(uniqueKey)) {
                                log.info("Skipping already processed item: " + entry.getTitle());
                                skippedCount++;
                                continue;
                            }
                            
                            // Mark as processed
                            processedItems.add(uniqueKey);
                            processedCount++;

                            FeedItem item = new FeedItem(
                                    entry.getTitle(),
                                    entry.getLink(),
                                    author,
                                    entry.getPublishedDate() != null
                                            ? entry.getPublishedDate().toInstant().atOffset(java.time.ZoneOffset.UTC)
                                            : null);

                            // Create a new exchange for each entry with metrics headers
                            exchange.getContext().createProducerTemplate().sendBodyAndHeaders("direct:processEntry",
                                    item,
                                    java.util.Map.of("author", author, "month", month));
                        }

                        // Clear the body since we've processed all entries
                        exchange.getMessage().setBody(null);
                        log.info("Feed processing complete: " + processedCount + " new items processed, " + skippedCount + " items skipped");
                    } else {
                        log.warn("Expected SyndFeed but got: " + (body != null ? body.getClass().getName() : "null"));
                        exchange.getMessage().setBody(null);
                    }
                })
                .log("Finished processing feed");

        from("direct:processEntry")
                .marshal(json)
                .convertBodyTo(String.class)
                .setBody(simple("${body}"))
                .transform(body().append("\n"))
                .toD("{{app.sink.uri}}")
                .to("micrometer:counter:rss_items_total?tags=author=${header.author},month=${header.month}")
                .log("Wrote entry to sink");
    }
}
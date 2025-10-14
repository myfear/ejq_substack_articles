package org.acme.rss.processors;

import com.rometools.rome.feed.synd.*;
import jakarta.enterprise.context.*;
import org.acme.rss.*;
import org.apache.camel.*;
import org.slf4j.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.concurrent.*;

@ApplicationScoped
public class RssFeedProcesor implements Processor {
  private final Set<String> processedItems = ConcurrentHashMap.newKeySet();
  private final static Logger log = LoggerFactory.getLogger(RssFeedProcesor.class);

  @Override
  public void process(Exchange exchange)  {
    Object body = exchange.getMessage().getBody();

    if (!(body instanceof SyndFeed feed)) {
      log.warn("Expected SyndFeed but got: {}", body != null ? body.getClass().getName() : "null");
      exchange.getMessage().setBody(null);
      return;
    }

    log.info("Processing feed: {} with {} entries", feed.getTitle(), feed.getEntries().size());

    int[] counts = {0, 0};

    feed.getEntries().stream()
      .filter(entry -> !processedItems.contains(createUniqueKey(entry)))
      .forEach(entry -> {
        processedItems.add(createUniqueKey(entry));
        counts[0]++;

        String author = Optional.ofNullable(entry.getAuthor()).orElse("unknown");
        FeedItem item = new FeedItem(entry.getTitle(), entry.getLink(), author,
          Optional.ofNullable(entry.getPublishedDate())
            .map(date -> date.toInstant().atOffset(ZoneOffset.UTC))
            .orElse(null));

        try (ProducerTemplate template = exchange.getContext().createProducerTemplate()) {
          template.sendBodyAndHeaders("direct:processEntry", item,
            Map.of("author", author, "month", extractMonth(entry)));
        }
        catch (Exception e) {
          log.error("Error processing entry: {}", entry, e);
        }
      });

    counts[1] = feed.getEntries().size() - counts[0];
    exchange.getMessage().setBody(null);
    log.info("Feed processing complete: {} new items processed, {} items skipped", counts[0], counts[1]);
  }

  private String createUniqueKey(SyndEntry entry) {
    return entry.getTitle() + "|" + entry.getLink() + "|" +
      (entry.getPublishedDate() != null ? entry.getPublishedDate().getTime() : "no-date");
  }

  private String extractMonth(SyndEntry entry) {
    String rslt = "unknown";
    if (entry.getPublishedDate() != null) {
      OffsetDateTime publishedDate = entry.getPublishedDate().toInstant()
        .atOffset(java.time.ZoneOffset.UTC);
      rslt = publishedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
    return rslt;
  }
}

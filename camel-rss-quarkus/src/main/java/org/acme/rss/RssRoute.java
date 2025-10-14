package org.acme.rss;

import com.fasterxml.jackson.databind.*;
import jakarta.enterprise.context.*;
import jakarta.inject.*;
import org.acme.rss.processors.*;
import org.apache.camel.builder.*;
import org.apache.camel.component.jackson.*;

@ApplicationScoped
public class RssRoute extends RouteBuilder {
  @Inject
  ObjectMapper mapper;
  @Inject
  RssFeedProcesor rssFeedProcessor;

  @Override
  public void configure()  {
    JacksonDataFormat json = new JacksonDataFormat(mapper, FeedItem.class);

    from("{{app.source.uri}}?splitEntries=false&delay=60000")
      .routeId("quarkus-rss-poller")
      .log("Fetched RSS feed: ${body}")
      .process(rssFeedProcessor)
      .log("Finished processing feed");

    from("direct:processEntry")
      .marshal(json)
      .convertBodyTo(String.class)
      .setBody(simple("${body}"))
      .transform(body()
      .append("\n"))
      .toD("{{app.sink.uri}}")
      .to("micrometer:counter:rss_items_total?tags=author=${header.author},month=${header.month}");
  }
}
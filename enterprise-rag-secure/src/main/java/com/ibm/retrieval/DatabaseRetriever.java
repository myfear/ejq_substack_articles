package com.ibm.retrieval;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.structured.CustomerOrder;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.inject.spi.CDI;

@ApplicationScoped
public class DatabaseRetriever implements ContentRetriever {

    private final SecurityIdentity identity;


    public DatabaseRetriever() {
        this.identity = CDI.current().select(SecurityIdentity.class).get();
    }


    private static final Pattern ORDER_PATTERN = Pattern.compile("ORD-\\d{3}");

    @Override
    @ActivateRequestContext
    public List<Content> retrieve(Query query) {

        try {
            Log.infof("DatabaseRetriever: Processing query: %s", query.text());
            
            if (identity.isAnonymous()) {
                Log.infof("DatabaseRetriever: Anonymous user, returning empty results");
                return Collections.emptyList();
            }

            String currentUser = identity.getPrincipal() != null ? identity.getPrincipal().getName() : "unknown";
            Log.infof("DatabaseRetriever: Authenticated user: %s", currentUser);

            String text = query.text();
            Log.infof("DatabaseRetriever: Searching for order pattern in text: %s", text);
            
            Matcher matcher = ORDER_PATTERN.matcher(text);
            if (matcher.find()) {
                String orderId = matcher.group();
                Log.infof("DatabaseRetriever: Found order ID pattern '%s' for user '%s'", orderId, currentUser);

                CustomerOrder order = CustomerOrder.findByOrderAndUser(orderId, currentUser);

                if (order != null) {
                    Log.infof("DatabaseRetriever: Retrieved order %s (status: %s, total: $%.2f) for user %s", 
                            order.orderNumber, order.status, order.totalAmount, currentUser);
                    TextSegment segment = TextSegment.from(
                            "DATABASE RECORD: Order %s is currently %s. Total: $%.2f."
                                    .formatted(order.orderNumber, order.status, order.totalAmount)
                    );
                    return List.of(Content.from(segment));
                } else {
                    Log.infof("DatabaseRetriever: Order %s not found or not accessible for user %s", orderId, currentUser);
                }
            } else {
                Log.infof("DatabaseRetriever: No order ID pattern found in query: %s", text);
            }
        } catch (Exception e) {
            Log.errorf(e, "DatabaseRetriever: Error during retrieval: %s", e.getMessage());
        }
        return Collections.emptyList();
    }
}

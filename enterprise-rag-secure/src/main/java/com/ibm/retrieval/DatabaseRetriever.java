package com.ibm.retrieval;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.ibm.structured.CustomerOrder;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DatabaseRetriever implements ContentRetriever {

    @Inject
    JsonWebToken idToken;

    private static final Pattern ORDER_PATTERN = Pattern.compile("ORD-\\d{3}");

    @Override
    @Authenticated
    public List<Content> retrieve(Query query) {

        try {
            Log.infof("DatabaseRetriever: Processing query: %s", query.text());

            // Check if token is available
            if (idToken == null || idToken.getName() == null) {
                Log.infof("DatabaseRetriever: No token or user name available, returning empty results");
                return Collections.emptyList();
            }

            // Get username from token - try getName() first, then fallback to upn claim
            String currentUser = idToken.getName();
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = idToken.getClaim("upn");
            }
            if (currentUser == null || currentUser.isEmpty()) {
                currentUser = idToken.getClaim("preferred_username");
            }

            if (currentUser == null || currentUser.isEmpty()) {
                Log.infof("DatabaseRetriever: Could not extract user from token, returning empty results");
                return Collections.emptyList();
            }

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
                                    .formatted(order.orderNumber, order.status, order.totalAmount));
                    return List.of(Content.from(segment));
                } else {
                    Log.infof("DatabaseRetriever: Order %s not found or not accessible for user %s", orderId,
                            currentUser);
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

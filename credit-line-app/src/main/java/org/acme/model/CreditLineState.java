package org.acme.model;

public enum CreditLineState {
    INITIATED,      // Customer requests a credit line
    PENDING_APPROVAL, // After basic info is submitted
    APPROVED,       // Backend approves the credit line
    EMAIL_SENT,     // Welcome email sent to the customer
    REJECTED,       // Backend rejects the credit line
    ERROR           // An error occurred during processing
}

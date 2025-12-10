package com.newyear.dto;

/**
 * Request body for scheduling a greeting.
 * This is sent by the frontend or any client to /api/greetings.
 */
public class GreetingRequest {

    public String senderName;
    public String recipientName;

    /**
     * IANA timezone string, for example:
     * “Asia/Tokyo”, “Europe/London”, “America/New_York”
     */
    public String recipientTimezone;

    public String message;

    /**
     * e.g. “email”, “in-app”, “sms”
     */
    public String deliveryChannel;

    /**
     * e.g. email address, phone number, or username
     */
    public String contactInfo;

    /**
     * Helper flag for the tutorial.
     * If true, we schedule the greeting 5 seconds in the future
     * instead of waiting until the next New Year.
     */
    public boolean testMode;
}

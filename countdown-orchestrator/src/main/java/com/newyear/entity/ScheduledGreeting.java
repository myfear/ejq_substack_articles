package com.newyear.entity;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "scheduled_greetings")
public class ScheduledGreeting extends PanacheEntity {

    @Column(nullable = false)
    public String senderName;

    @Column(nullable = false)
    public String recipientName;

    /**
     * IANA timezone of the recipient, for example:
     * “Asia/Tokyo”, “Europe/Berlin”, “America/New_York”
     */
    @Column(nullable = false)
    public String recipientTimezone;

    @Column(length = 1000)
    public String message;

    /**
     * Target time at which the greeting should be delivered,
     * in the recipient’s timezone.
     */
    @Column(nullable = false)
    public ZonedDateTime targetDeliveryTime;

    /**
     * Quartz job id that will deliver this greeting.
     * Optional but handy for debugging and job management.
     */
    @Column
    public String quartzJobId;

    @Column(nullable = false)
    public boolean delivered = false;

    @Column
    public Instant deliveredAt;

    /**
     * For example: “email”, “in-app”, “sms”.
     * In this tutorial we simulate the channel.
     */
    @Column
    public String deliveryChannel;

    /**
     * Contact details for the chosen delivery channel.
     * Email address, phone number, user id, etc.
     */
    @Column
    public String contactInfo;

    /**
     * Helper method to find pending greetings in a given time range.
     * Not used in the basic flow but useful for catch-up logic.
     */
    public static List<ScheduledGreeting> findPendingByTimeRange(ZonedDateTime start, ZonedDateTime end) {
        return find("delivered = false and targetDeliveryTime between ?1 and ?2", start, end).list();
    }
}
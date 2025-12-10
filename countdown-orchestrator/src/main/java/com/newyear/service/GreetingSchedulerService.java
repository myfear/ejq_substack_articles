package com.newyear.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.newyear.dto.GreetingRequest;
import com.newyear.entity.ScheduledGreeting;
import com.newyear.job.GreetingDeliveryJob;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for scheduling greetings with Quartz.
 */
@ApplicationScoped
public class GreetingSchedulerService {

    @Inject
    Scheduler quartzScheduler;

    @Transactional
    public ScheduledGreeting scheduleGreeting(GreetingRequest request) {
        ZoneId recipientZone = ZoneId.of(request.recipientTimezone);

        // If testMode is true, schedule 5 seconds from now.
        // Otherwise, schedule for next New Year’s midnight in recipient’s timezone.
        ZonedDateTime deliveryTime;

        if (request.testMode) {
            deliveryTime = ZonedDateTime.now(recipientZone).plusSeconds(5);
        } else {
            ZonedDateTime now = ZonedDateTime.now(recipientZone);
            int currentYear = now.getYear();
            int targetYear = now.getMonthValue() > 1 || (now.getMonthValue() == 1 && now.getDayOfMonth() > 1)
                    ? currentYear + 1
                    : currentYear;
            // Midnight (00:00) on January 1st of the target year
            deliveryTime = ZonedDateTime.of(targetYear, 1, 1, 0, 0, 0, 0, recipientZone);
        }

        // 1. Persist entity
        ScheduledGreeting greeting = new ScheduledGreeting();
        greeting.senderName = request.senderName;
        greeting.recipientName = request.recipientName;
        greeting.recipientTimezone = request.recipientTimezone;
        greeting.message = request.message;
        greeting.targetDeliveryTime = deliveryTime;
        greeting.deliveryChannel = request.deliveryChannel;
        greeting.contactInfo = request.contactInfo;

        greeting.persist();
        // At this point greeting.id is set.

        // 2. Schedule the Quartz job
        scheduleQuartzJob(greeting);

        return greeting;
    }

    private void scheduleQuartzJob(ScheduledGreeting greeting) {
        try {
            String jobId = "greeting-" + greeting.id;

            JobDetail job = JobBuilder.newJob(GreetingDeliveryJob.class)
                    .withIdentity(jobId, "greetings")
                    .usingJobData("greetingId", String.valueOf(greeting.id))
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-" + jobId, "greetings")
                    .startAt(Date.from(greeting.targetDeliveryTime.toInstant()))
                    .build();

            quartzScheduler.scheduleJob(job, trigger);

            // Store job id for debugging. No extra persist needed; entity is managed.
            greeting.quartzJobId = jobId;

            System.out.printf("Scheduled greeting %d for %s at %s%n",
                    greeting.id,
                    greeting.recipientName,
                    greeting.targetDeliveryTime);

        } catch (SchedulerException e) {
            throw new RuntimeException("Scheduling failed", e);
        }
    }
}
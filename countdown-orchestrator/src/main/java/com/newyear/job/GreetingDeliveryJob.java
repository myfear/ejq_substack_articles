package com.newyear.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.newyear.service.GreetingDeliveryService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Quartz job that simply delegates to GreetingDeliveryService.
 * A new instance can be created per execution by Quartz, but
 * CDI injection still works because Quarkus integrates them.
 */
@ApplicationScoped
public class GreetingDeliveryJob implements Job {

    @Inject
    GreetingDeliveryService deliveryService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String greetingIdStr = context.getJobDetail()
                .getJobDataMap()
                .getString("greetingId");
        Long greetingId = Long.parseLong(greetingIdStr);

        try {
            deliveryService.deliver(greetingId);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
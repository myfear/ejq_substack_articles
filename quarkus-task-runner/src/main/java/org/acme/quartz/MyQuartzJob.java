package org.acme.quartz;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyQuartzJob {

    @Inject
    org.quartz.Scheduler quartz;

    void onStart(@Observes StartupEvent event) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(MyJob.class)
                .withIdentity("myJob", "myGroup")
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("myTrigger", "myGroup")
                .startNow()
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInSeconds(10)
                                .repeatForever())
                .build();
        quartz.scheduleJob(job, trigger);
    }

    void performTask(String taskName) {
        Log.infof("Executing Quartz Job: %s on thread %s", taskName, Thread.currentThread().getName());
    }

    public static class MyJob implements Job {

        @Inject
        MyQuartzJob jobBean;

        public void execute(JobExecutionContext context) throws JobExecutionException {
            jobBean.performTask(context.getJobDetail().getKey().getName());
        }

    }
}
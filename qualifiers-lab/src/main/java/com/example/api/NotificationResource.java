package com.example.api;

import com.example.notify.EmailQualifier;
import com.example.notify.NotificationService;
import com.example.notify.SmsQualifier;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/notify")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class NotificationResource {

    @Inject
    @EmailQualifier
    NotificationService emailService;

    @Inject
    @SmsQualifier
    NotificationService smsService;

    @POST
    @Path("/email")
    public String email(MessageRequest req) {
        return emailService.send(req.message);
    }

    @POST
    @Path("/sms")
    public String sms(MessageRequest req) {
        return smsService.send(req.message);
    }
}
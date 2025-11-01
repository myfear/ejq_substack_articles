package org.acme.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.ConfigProvider;

@ApplicationScoped
public class DynamicMessageProducer {

    @Produces
    public DynamicMessageService createService() {
        var config = ConfigProvider.getConfig();
        String type = config.getOptionalValue("dynamic.message-type", String.class).orElse("email");
        boolean premium = config.getOptionalValue("dynamic.premium", Boolean.class).orElse(false);

        if ("sms".equalsIgnoreCase(type)) {
            if (premium) {
                return new DynamicSmsService() {
                    @Override
                    public String send(String recipient, String text) {
                        return "Premium " + super.send(recipient, text);
                    }
                };
            }
            return new DynamicSmsService();
        } else {
            return new DynamicEmailService();
        }
    }
}
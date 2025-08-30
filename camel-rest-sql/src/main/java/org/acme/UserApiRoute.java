package org.acme;

import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserApiRoute extends RouteBuilder {

    @Override
    public void configure() {
        from("platform-http:/users?httpMethodRestrict=GET")
                .to("sql:select name, city from users order by name")
                .marshal().json();
    }
}

package org.acme.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(serviceName = "GreetingService")
public class GreetingServiceImpl implements GreetingService {

    @WebMethod
    @Override
    public String greet(String name) {
        return "Hello " + name + ", from Quarkus SOAP!";
    }
}
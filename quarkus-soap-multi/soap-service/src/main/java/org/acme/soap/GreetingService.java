package org.acme.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(name = "GreetingService", serviceName = "GreetingService")
public interface GreetingService {

    @WebMethod
    String greet(String name);
}
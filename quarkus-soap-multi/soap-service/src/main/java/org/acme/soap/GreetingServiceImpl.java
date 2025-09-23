package org.acme.soap;

import jakarta.jws.WebService;

@WebService
public class GreetingServiceImpl implements GreetingService {

    @Override
    public GreetingResponse greet(GreetingRequest request) {
        String name = request.getName();
        String msg = "Hello " + name + ", from Quarkus SOAP!";
        return new GreetingResponse(msg);
    }
}
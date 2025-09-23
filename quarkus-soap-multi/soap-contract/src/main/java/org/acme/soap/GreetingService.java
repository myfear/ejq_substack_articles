package org.acme.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(targetNamespace = "http://soap.acme.org/", name = "GreetingService")
public interface GreetingService {

    @WebMethod
    GreetingResponse greet(GreetingRequest request);
}
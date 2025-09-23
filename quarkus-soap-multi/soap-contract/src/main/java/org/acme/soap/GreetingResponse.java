package org.acme.soap;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "GreetingResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class GreetingResponse {

    @XmlElement(required = true)
    private String message;

    public GreetingResponse() {
    }

    public GreetingResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
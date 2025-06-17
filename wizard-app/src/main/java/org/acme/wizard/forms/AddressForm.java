package org.acme.wizard.forms;

import org.jboss.resteasy.reactive.RestForm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddressForm {
    @RestForm
    @NotBlank(message = "Street is required")
    @Size(min = 3, max = 100, message = "Street must be between 3 and 100 characters")
    public String street;

     @RestForm
    @NotBlank(message = "City is required")
    @Size(min = 2, max = 50, message = "City must be between 2 and 50 characters")
    public String city;

     @RestForm
     @NotBlank(message = "Zip Code is required")
    @Size(min = 5, max = 10, message = "Zip Code must be between 5 and 10 characters")
    public String zipCode;

    // Getters and setters (or public fields for simplicity with Qute)
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
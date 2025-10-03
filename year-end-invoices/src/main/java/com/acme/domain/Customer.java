package com.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Customer extends PanacheEntity {

    @NotBlank
    public String fullName;
    @NotBlank
    public String street;
    @NotBlank
    public String postalCode;
    @NotBlank
    public String city;
    @NotBlank
    public String countryCode;

    @NotBlank
    public String iban;
    @NotBlank
    public String bic;
    @Email
    @NotBlank
    public String email;
}
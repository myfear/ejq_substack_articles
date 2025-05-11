package org.acme.i18n;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class LocalizedMessage {
    @Id
    @GeneratedValue
    public Long id;

    public String messageKey;
    public String langTag;
    public String messageContent;
}
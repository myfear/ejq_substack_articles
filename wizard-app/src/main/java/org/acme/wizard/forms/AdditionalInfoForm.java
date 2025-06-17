package org.acme.wizard.forms;

import org.jboss.resteasy.reactive.RestForm;

import jakarta.validation.constraints.Size;

public class AdditionalInfoForm {
     @RestForm
    @Size(max = 200, message = "Additional comments cannot exceed 200 characters")
    public String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
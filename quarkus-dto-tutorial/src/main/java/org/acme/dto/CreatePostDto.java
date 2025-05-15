package org.acme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostDto {
    @NotBlank
    @Size(max = 255)
    public String title;

    @NotBlank
    public String content;

    @Email
    public String authorEmail;
}


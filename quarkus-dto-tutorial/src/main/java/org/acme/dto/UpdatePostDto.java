package org.acme.dto;

import jakarta.validation.constraints.Size;

public class UpdatePostDto {
    @Size(max = 255)
    public String title;
    public String content;
}

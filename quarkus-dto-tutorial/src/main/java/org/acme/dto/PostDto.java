package org.acme.dto;

import java.time.LocalDateTime;

public class PostDto {
    public Long id;
    public String title;
    public String content;
    public String authorEmail;
    public LocalDateTime creationDate;
    public LocalDateTime lastModifiedDate;
}
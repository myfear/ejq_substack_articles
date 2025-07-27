package com.example.dto;

import java.time.LocalDateTime;

public class DocumentDto {
    public Long id;
    public String title;
    public String content;
    public String ownerUsername;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
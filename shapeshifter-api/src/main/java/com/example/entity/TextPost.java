package com.example.entity;

import com.example.json.Views;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Entity;

@Entity
public class TextPost extends Content {

    @JsonView(Views.Public.class)
    public String body;

    @JsonView(Views.Public.class)
    public int wordCount;
}

package com.example.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

// This is the base class for any UI element.
// Jackson annotations are used to deserialize into the correct subclass based on the "renderHint" field.
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "renderHint",
    visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextElement.class, name = "text"),
        @JsonSubTypes.Type(value = BookElement.class, name = "book"),
        @JsonSubTypes.Type(value = PodcastElement.class, name = "podcast"),
        @JsonSubTypes.Type(value = ListElement.class, name = "list"),
        @JsonSubTypes.Type(value = WebsiteElement.class, name = "website")
})
public abstract class UIElement {
    public String renderHint;
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String templatePath;
}

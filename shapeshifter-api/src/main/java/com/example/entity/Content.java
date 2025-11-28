package com.example.entity;

import java.math.BigDecimal;

import com.example.json.Views;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Transient;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "content_type")
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPost.class, name = "text"),
    @JsonSubTypes.Type(value = VideoPost.class, name = "video")
})
public abstract class Content extends PanacheEntity {

    @JsonView(Views.Public.class)
    public String title;

    @JsonView(Views.Public.class)
    public String author;

    @JsonView(Views.Authenticated.class)
    public int views;

    // Premium-only calculated field
    @JsonView(Views.PremiumFeature.class)
    @JsonProperty("adRevenue")
    @Transient // Not in DB, calculated on the fly
    public BigDecimal getAdRevenue() {
        return BigDecimal.valueOf(this.views * 0.05);
    }
}
package org.acme.coffee;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "Coffee", description = "A coffee drink with details")
public class Coffee {

    @JsonProperty("coffeeId")
    @Schema(required = true, example = "1", description = "Unique coffee identifier")
    public Long id;

    @NotBlank
    @Size(max = 50)
    @Schema(required = true, example = "Espresso")
    public String name;

    @Schema(example = "Strong and bold taste")
    public String description;

    @JsonIgnore
    public String internalNote;

    public Coffee() {
    }

    public Coffee(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}
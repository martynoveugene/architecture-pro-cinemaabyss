package com.cinemaabyss.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class UserEvent implements Identifiable {

    @NotNull(message = "user_id is required")
    @JsonProperty("user_id")
    private Long userId;

    private String username;

    private String email;

    @NotBlank(message = "action is required")
    private String action;

    @NotNull(message = "timestamp is required")
    private Date timestamp;

    @Override
    public String getId() {
        return "user-"+this.userId+"-"+action;
    }
}

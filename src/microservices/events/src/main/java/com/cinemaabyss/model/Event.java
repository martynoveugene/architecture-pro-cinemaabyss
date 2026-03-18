package com.cinemaabyss.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Event {
    @NotBlank(message = "id is required")
    private String id;

    @NotBlank(message = "type is required")
    private String type;

    @NotNull(message = "timestamp is required")
    private Date timestamp;

    @NotBlank(message = "payload is required")
    private String payload;
}

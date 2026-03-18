package com.cinemaabyss.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventResponse {

    String status;

    Integer partition;

    Long offset;

    Event event;

/*
    required:
            - status
        - partition
        - offset
        - event
*/

}

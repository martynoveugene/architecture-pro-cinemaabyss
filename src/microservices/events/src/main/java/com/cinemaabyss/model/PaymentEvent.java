package com.cinemaabyss.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class PaymentEvent implements Identifiable {

    @NotNull(message = "payment_id is required")
    @JsonProperty("payment_id")
    private Long paymentId;

    @NotNull(message = "user_id is required")
    @JsonProperty("user_id")
    private Long userId;

    @NotNull(message = "amount is required")
    private Float amount;

    @NotBlank(message = "status is required")
    private String status;

    @NotNull(message = "timestamp is required")
    private Date timestamp;

    @JsonProperty("method_type")
    private String methodType;

    @Override
    public String getId() {
        return "payment-"+this.paymentId+"-"+this.status;
    }
}

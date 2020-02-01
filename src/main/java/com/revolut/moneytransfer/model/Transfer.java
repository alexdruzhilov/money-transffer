package com.revolut.moneytransfer.model;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Introspected
public class Transfer {
    @Min(0)
    private long amount;
    @NotBlank
    private String currency;
}

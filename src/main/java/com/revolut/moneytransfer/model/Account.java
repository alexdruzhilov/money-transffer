package com.revolut.moneytransfer.model;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Introspected
public class Account {
    private String id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Min(0)
    private long balance;

    // The only one currency supported
    private final String currency = "USD";
}

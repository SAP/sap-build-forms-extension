package com.sap.bfx.valuehelp.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValueHelpDef {

    public static final long TTL_STATIC = -1;
    public static final long TTL_NONE = 0;

    @NotNull(message="ID cannot be null")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @NotNull(message="Time to live cannot be null")
    @Min(value = TTL_STATIC, message = "Time to live should not be less than " + TTL_STATIC)
    private Long ttl;

    @NotNull(message="Adapter cannot be null")
    @NotBlank(message = "Adapter is mandatory")
    private String adapter;

    @NotNull(message="Config cannot be null")
    private String config;

    @NotNull(message="Description cannot be null")
    private String description;

    @NotNull(message="Ignore languages cannot be null")
    private ArrayList<String> languages;

}

package com.sap.bfx.valuehelp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
public class ValueHelp {

    @NotNull(message = "ID is mandatory")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @NotNull(message = "Version cannot be null")
    @PositiveOrZero(message = "Version must be greater than or equal 0")
    private long version;

    @NotNull(message = "Locale cannot be null")
    private Locale locale;

    @JsonIgnore
    private java.sql.Timestamp validUntil;

    @NotNull(message = "Values cannot be null")
    @Size(min = 1, message = "Values must contain at least one element")
    private List<Map<String, String>> values;

}
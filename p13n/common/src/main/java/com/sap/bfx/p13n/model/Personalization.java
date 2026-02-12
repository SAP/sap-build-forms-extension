package com.sap.bfx.p13n.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class Personalization {

    UUID id;

    String user;

    @NotNull(message = "Key is mandatory")
    @NotBlank(message = "Key is mandatory")
    String key;

    String app;

    @NotNull(message = "Encoding is mandatory")
    @NotBlank(message = "Encoding is mandatory")
    String encoding;

    @NotNull(message = "Value is mandatory")
    @NotBlank(message = "Value is mandatory")
    String value;

    boolean editable;
    boolean visible;
}

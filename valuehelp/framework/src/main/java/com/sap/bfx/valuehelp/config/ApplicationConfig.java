package com.sap.bfx.valuehelp.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Data
public class ApplicationConfig {

    @Value("${forms.valuehelp.adapters.refresh:1m}")
    private Duration refresh = Duration.ofMinutes(1);

    @Value("${forms.valuehelp.locales:en}")
    private String locales;

}

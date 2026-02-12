package com.sap.bfx.cockpit.config;

import com.sap.bfx.cockpit.api.ProcessStateSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FormSerializationConfig {

//    @Bean
//    public Module javaTimeModule() {
//        final var module = new JavaTimeModule();
//        module.addSerializer(LOCAL_DATETIME_SERIALIZER);
//        return module;
//    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer objectMapperCustomizer() {
        return builder -> builder.serializers(new ProcessStateSerializer());
    }
}

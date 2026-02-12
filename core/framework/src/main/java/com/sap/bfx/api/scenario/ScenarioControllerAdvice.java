package com.sap.bfx.api.scenario;

import com.sap.bfx.api.scenario.json.FieldResponse;
import com.sap.bfx.api.scenario.json.JsonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * The Class FormResultAdvice.
 */
@RestControllerAdvice
@Slf4j
public class ScenarioControllerAdvice implements ResponseBodyAdvice<Object> {

    /**
     * The json service.
     */
    @Autowired
    JsonService jsonService;

    @Override
    public Object beforeBodyWrite(Object obj, MethodParameter method, MediaType mediaType, Class<? extends HttpMessageConverter<?>> clazz,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (obj instanceof FieldResponse<?> tempFieldResponse) {
            try {
                jsonService.send(tempFieldResponse, response, MediaType.APPLICATION_JSON_UTF8);
            } catch (Exception e) {
                log.error("Error writing ScenarioController.FieldResponse result", e);
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return null;
        }
        return obj;
    }

    @Override
    public boolean supports(MethodParameter arg0, Class<? extends HttpMessageConverter<?>> arg1) {
        return true;
    }

}

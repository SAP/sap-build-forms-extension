package com.sap.bfx.api;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.valuehelp.ValueHelpService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * REST controller for handling value help related API requests.
 */
@RestController
@RequestMapping("api/v1/valuehelpvalues")
@Slf4j
@Hidden
public class ValueHelpController {

    private final ValueHelpService valueHelpService;
    private final SecurityService securityService;

    /**
     * Constructor for ValueHelpController.
     *
     * @param valueHelpService the ValueHelpService to be used for value help operations
     * @param securityService  the SecurityService to be used for authorization checks
     */
    @Autowired
    public ValueHelpController(final ValueHelpService valueHelpService, final SecurityService securityService) {
        this.valueHelpService = valueHelpService;
        this.securityService = securityService;
    }

    /**
     * Endpoint for finding value help values based on the provided ID and locale.
     *
     * @param id     the value help ID
     * @param locale the locale for which to find values
     * @param token  the authentication token for authorization checks
     * @return a ResponseEntity containing the values and their version
     */
    @GetMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ValuesResponse> findValueHelp(@PathVariable(name = "id") String id,
                                                        @PathVariable(name = "locale") String locale,
                                                        AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Illegal or missing locale");
        }
        securityService.ensureAuthorized(token, EventType.FindValueHelpAuth, Boolean.FALSE, null, null);

        final var response = valueHelpService.findValues(id, new Locale(locale));
        final var values = new HashMap<String, String>();
        response.getValues().forEach(it -> {
            final var key = values.get(response.getKeyKey());
            var value = values.get(response.getValueKeys().get(0));
            if (StringUtils.isEmpty(response.getFormatTemplate())) {
                final var subs = new StringSubstitutor(it);
                value = subs.replace(response.getFormatTemplate());
            }
            values.put(key, value);
        });

        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new ValuesResponse(values, response.getVersion()));
    }

    /**
     * Response class for value help values, containing the values and their version.
     */
    @Data
    @AllArgsConstructor
    public static class ValuesResponse {
        Map<String, String> values;
        Long version;
    }
}

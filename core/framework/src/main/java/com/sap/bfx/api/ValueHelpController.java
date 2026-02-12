package com.sap.bfx.api;

import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.FormsRoles;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.valuehelp.ValueHelpService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("api/v1/valuehelpvalues")
@Slf4j
@Hidden
public class ValueHelpController {

    private final ValueHelpService valueHelpService;
    private final SecurityService securityService;

    @Autowired
    public ValueHelpController(final ValueHelpService valueHelpService, final SecurityService securityService) {
        this.valueHelpService = valueHelpService;
        this.securityService = securityService;
    }

    @GetMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ValuesResponse> findValueHelp(@PathVariable(name = "id") String id,
                                                        @PathVariable(name = "locale") String locale,
                                                        AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Illegal or missing locale");
        }
        securityService.ensureAnyAuthorized(token, FormsRoles.StartProcess, FormsRoles.ParticipateProcess, FormsRoles.SeeAfterStart);

        var result = valueHelpService.findValues(id, new Locale(locale));
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.noCache())
                .body(new ValuesResponse(result.getLeft(), result.getRight()));
    }

    @Data
    @AllArgsConstructor
    public static class ValuesResponse {
        String values;
        Long version;
    }
}

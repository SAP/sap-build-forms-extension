package com.sap.bfx.valuehelp.api;

import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.security.ValueHelpRoles;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("api/v1/valuehelpvalues")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ValueHelpValuesController {

    private final ValueHelpService service;
    private final SecurityService securityService;

    @Autowired
    public ValueHelpValuesController(final ValueHelpService service, final SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelp> getById(@PathVariable String id, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findValueById(id);
    }

    @GetMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelp> getByIdLocale(@PathVariable String id, @PathVariable String locale, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findValueByIdLocale(id, locale);
    }

    @GetMapping(value = "/{id}/{locale}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelp getLatestVersionByIdLocale(@PathVariable String id, @PathVariable String locale, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findValueLatestVersionByIdLocale(id, locale);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-values with id '"
                    + id + "' and locale '" + locale + "'.");
        }
        return resultOpt.get();
    }

    @PostMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ValueHelp create(@PathVariable String id, @PathVariable String locale,
                            @Valid @RequestBody ValueHelp valueHelp, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        if (null == valueHelp) {
            throw new BadRequestException("Missing valueHelp");
        }
        if (!id.equals(valueHelp.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided ID in URL does not match ID in request body");
        }
        if (locale.equals("_")) {
            valueHelp.setLocale(new Locale("_"));
        } else if (valueHelp.getLocale() == null || !locale.equals(valueHelp.getLocale().toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided locale in URL does not match locale in request body");
        }
        if (valueHelp.getVersion() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided version is not 0");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findValueByIdLocaleVersion(id, locale, 0);
        if (resultOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entity with id " + id + " and locale " + locale +
                    " already exists.");
        }
        return service.addValue(valueHelp);
    }


    @PutMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelp update(@PathVariable String id, @PathVariable String locale,
                            @Valid @RequestBody ValueHelp valueHelp, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        if (!id.equals(valueHelp.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided ID in URL does not match ID in request body");
        }
        if (locale.equals("_")) {
            valueHelp.setLocale(new Locale("_"));
        } else if (valueHelp.getLocale() == null || !locale.equals(valueHelp.getLocale().toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided locale in URL does not match locale in request body");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findValueByIdLocaleVersion(id, locale, valueHelp.getVersion());
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot find entity with id " + id + " and locale "
                    + locale + " and version " + valueHelp.getVersion() + ".");
        }
        return service.updateValue(valueHelp);
    }

    @DeleteMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @PathVariable String locale, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findValueLatestVersionByIdLocale(id, locale);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-value with id '"
                    + id + "' and locale '" + locale + "'");
        }
        service.deleteValue(id, locale);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
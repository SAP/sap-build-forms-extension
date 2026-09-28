package com.sap.bfx.valuehelp.api;

import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.SecurityUtils;
import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.security.ValueHelpGrantedAuthorities;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * REST Controller for managing Value Help values via HTTP endpoints.
 * Provides CRUD operations for value help data with locale support and versioning.
 */
@RestController
@RequestMapping("api/v1/valuehelpvalues")
@Slf4j
public class ValueHelpValuesController {

    private final ValueHelpService service;
    private final SecurityService securityService;

    /**
     * Constructs a new ValueHelpValuesController with the specified service and security service.
     *
     * @param service         the ValueHelpService for managing value help data
     * @param securityService the SecurityService for authorization checks
     */
    @Autowired
    public ValueHelpValuesController(final ValueHelpService service, final SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
    }

    /**
     * Retrieves a collection of ValueHelp objects by their ID.
     *
     * @param id the ID of the value help to retrieve
     * @return a collection of ValueHelp objects
     * @throws BadRequestException if the ID is missing or blank
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelp> getById(@PathVariable String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return service.findValueById(id);
    }

    /**
     * Retrieves a collection of ValueHelp objects by their ID and locale.
     *
     * @param id     the ID of the value help to retrieve
     * @param locale the locale of the value help to retrieve
     * @return a collection of ValueHelp objects
     * @throws BadRequestException if the ID or locale is missing or blank
     */
    @GetMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelp> getByIdLocale(@PathVariable String id, @PathVariable String locale) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return service.findValueByIdLocale(id, locale);
    }

    /**
     * Retrieves the latest version of a ValueHelp object by its ID and locale.
     *
     * @param id     the ID of the value help to retrieve
     * @param locale the locale of the value help to retrieve
     * @return a ResponseEntity containing the latest ValueHelp object or a 404 status if not found
     * @throws BadRequestException if the ID or locale is missing or blank
     */
    @GetMapping(value = "/{id}/{locale}/latest", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<ValueHelp> getLatestVersionByIdAndLocale(@PathVariable String id,
                                                                   @PathVariable String locale) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, Boolean.TRUE,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        var resultOpt = service.findValueLatestVersionByIdLocale(id, locale);
        if (resultOpt.isEmpty()) {
            log.info("Cannot find value-help-value with id '{}' and locale '{}'", id, locale);
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok(resultOpt.get());
    }

    /**
     * Creates a new ValueHelp object with the specified ID and locale.
     *
     * @param id        the ID of the value help to create
     * @param locale    the locale of the value help to create
     * @param valueHelp the ValueHelp object to create
     * @return the created ValueHelp object
     * @throws BadRequestException if the ID, locale, or valueHelp is missing or invalid
     */
    @PostMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ValueHelp create(@PathVariable String id, @PathVariable String locale,
                            @Valid @RequestBody ValueHelp valueHelp) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided ID in URL does not match ID in request body");
        }
        if (locale.equals("_")) {
            valueHelp.setLocale(new Locale("_"));
        } else if (valueHelp.getLocale() == null || !locale.equals(valueHelp.getLocale().toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided locale in URL does not match locale in request body");
        }
        if (valueHelp.getVersion() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided version is not 0");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        var resultOpt = service.findValueByIdLocaleVersion(id, locale, 0);
        if (resultOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Entity with id " + id + " and locale " + locale + " already exists.");
        }
        return service.addValue(valueHelp);
    }

    /**
     * Updates an existing ValueHelp object with the specified ID and locale.
     *
     * @param id        the ID of the value help to update
     * @param locale    the locale of the value help to update
     * @param valueHelp the ValueHelp object to update
     * @return the updated ValueHelp object
     * @throws BadRequestException if the ID, locale, or valueHelp is missing or invalid
     */
    @PutMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelp update(@PathVariable String id, @PathVariable String locale,
                            @Valid @RequestBody ValueHelp valueHelp) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }
        if (!id.equals(valueHelp.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided ID in URL does not match ID in request body");
        }
        if (locale.equals("_")) {
            valueHelp.setLocale(new Locale("_"));
        } else if (valueHelp.getLocale() == null || !locale.equals(valueHelp.getLocale().toString())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided locale in URL does not match locale in request body");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        var resultOpt = service.findValueByIdLocaleVersion(id, locale, valueHelp.getVersion());
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Cannot find entity with id " + id + " and locale " + locale + " and version " +
                            valueHelp.getVersion() + ".");
        }
        return service.updateValue(valueHelp);
    }

    /**
     * Deletes a ValueHelp object with the specified ID and locale.
     *
     * @param id     the ID of the value help to delete
     * @param locale the locale of the value help to delete
     * @throws BadRequestException     if the ID or locale is missing or blank
     * @throws ResponseStatusException if the ValueHelp object is not found
     */
    @DeleteMapping(value = "/{id}/{locale}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id, @PathVariable String locale) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        if (StringUtils.isBlank(locale)) {
            throw new BadRequestException("Missing locale");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        var resultOpt = service.findValueLatestVersionByIdLocale(id, locale);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "cannot find value-help-value with id '" + id + "' and locale '" + locale + "'");
        }
        service.deleteValue(id, locale);
    }

    /**
     * Handles validation exceptions thrown during request processing.
     *
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @return a map of field names to error messages
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
package com.sap.bfx.valuehelp.api;

import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.SecurityUtils;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.security.ValueHelpGrantedAuthorities;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing Value Help Definitions.
 */
@RestController
@RequestMapping("api/v1/valuehelpdefs")
@Slf4j
public class ValueHelpDefsController {

    private final ValueHelpService valueHelpService;
    private final SecurityService securityService;

    /**
     * Constructor for ValueHelpDefsController.
     *
     * @param valueHelpService the service for managing value help definitions
     * @param securityService  the service for handling security and authorization
     */
    @Autowired
    public ValueHelpDefsController(final ValueHelpService valueHelpService, final SecurityService securityService) {
        this.valueHelpService = valueHelpService;
        this.securityService = securityService;
    }

    /**
     * Retrieves all value help definitions, optionally filtered by search criteria and adapter.
     *
     * @param search  optional search string to filter definitions
     * @param adapter optional array of adapters to filter definitions
     * @return a collection of value help definitions matching the criteria
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelpDef> findAll(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String[] adapter) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return valueHelpService.findAllDefs(search, adapter);
    }

    /**
     * Retrieves a value help definition by its ID.
     *
     * @param id the ID of the value help definition to retrieve
     * @return the value help definition with the specified ID
     * @throws BadRequestException     if the ID is missing or blank
     * @throws ResponseStatusException if the definition is not found
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelpDef findById(@PathVariable String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return valueHelpService.findDefById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "cannot find value-help-definition with id '" + id + "'"));
    }

    /**
     * Retrieves all available adapters for value help definitions.
     *
     * @return a collection of adapter names
     */
    @GetMapping(value = "/adapter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllAdapter() {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, Boolean.TRUE,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return valueHelpService.findAllAdapter();
    }

    /**
     * Retrieves all defined adapters for value help definitions.
     *
     * @return a collection of defined adapter names
     */
    @GetMapping(value = "/definedAdapter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllDefinedAdapter() {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);
        return valueHelpService.findAllDefinedAdapter();
    }

    /**
     * Retrieves all defined locales for value help definitions.
     *
     * @return an array of defined locale strings
     */
    @GetMapping(value = "/locales", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String[] getLocales() {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAnyAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpDisplay, ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        return valueHelpService.findAllDefinedLocales();
    }

    /**
     * Exports value help definitions based on search criteria, adapter, or IDs.
     *
     * @param search  optional search string to filter definitions
     * @param adapter optional array of adapters to filter definitions
     * @param ids     optional array of IDs to filter definitions
     * @return a ResponseEntity containing the exported XML file as an InputStreamResource
     */
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<InputStreamResource> export(@RequestParam(required = false) String search,
                                                      @RequestParam(required = false) String[] adapter,
                                                      @RequestParam(required = false) String[] ids) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        byte[] xmlByteArray;
        if (search != null && search.length() > 0 && adapter != null && adapter.length > 0) {
            xmlByteArray = valueHelpService.exportDefs(search, adapter);
        } else if (search != null && search.length() > 0) {
            xmlByteArray = valueHelpService.exportDefs(search);
        } else if (adapter != null && adapter.length > 0) {
            xmlByteArray = valueHelpService.exportDefs(adapter);
        } else if (ids != null && ids.length > 0) {
            xmlByteArray = valueHelpService.exportDefsByIds(ids);
        } else {
            xmlByteArray = valueHelpService.exportDefs();
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "./definitions.xml")
                             .contentType(MediaType.APPLICATION_XML).contentLength(xmlByteArray.length)
                             .body(new InputStreamResource(new ByteArrayInputStream(xmlByteArray)));
    }

    /**
     * Creates a new value help definition with the specified ID.
     *
     * @param id           the ID of the new value help definition
     * @param valueHelpDef the value help definition to create
     * @return the created value help definition
     * @throws BadRequestException     if the ID is missing or blank, or if the ID in the URL does not match the ID in the request body
     * @throws ResponseStatusException if a definition with the specified ID already exists
     */
    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ValueHelpDef create(@PathVariable String id, @Valid @RequestBody ValueHelpDef valueHelpDef) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        if (!id.equals(valueHelpDef.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided ID in URL does not match ID in request body");
        }
        var resultOpt = valueHelpService.findDefById(id);
        if (resultOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entity with id " + id + " already exists.");
        }
        valueHelpService.addDef(valueHelpDef);
        return valueHelpDef;
    }

    /**
     * Uploads an XML file to import value help definitions.
     *
     * @param file             the XML file to upload
     * @param override         whether to override existing definitions
     * @param useTechnicalName whether to use technical names for definitions
     * @return a ResponseEntity containing a message about the import result
     */
    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<?> uploadXmlFile(@RequestParam("file") MultipartFile file, @RequestParam boolean override,
                                           @RequestParam boolean useTechnicalName) {
        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        String msg;
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }
        try {
            msg = valueHelpService.importXmlFile(file, override);
        } catch (JAXBException | IOException e) {
            log.debug("Exception during import of xml file.");
            try {
                msg = valueHelpService.importAbpmXmlFile(file, override, useTechnicalName);
            } catch (JAXBException | IOException e1) {
                log.error("Exception during import of abpm xml file.");
                return ResponseEntity.internalServerError()
                                     .body("Error uploading file. Please check file and try again.");
            }
        }
        if (msg != null) {
            return ResponseEntity.ok(msg);
        }
        return ResponseEntity.ok(null);
    }

    /**
     * Updates an existing value help definition with the specified ID.
     *
     * @param id           the ID of the value help definition to update
     * @param valueHelpDef the updated value help definition
     * @return the updated value help definition
     * @throws BadRequestException     if the ID is missing or blank, or if the ID in the URL does not match the ID in the request body
     * @throws ResponseStatusException if the definition with the specified ID is not found
     */
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelpDef update(@PathVariable String id, @Valid @RequestBody ValueHelpDef valueHelpDef) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        if (!id.equals(valueHelpDef.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided ID in URL does not match ID in request body");
        }
        var resultOpt = valueHelpService.findDefById(id);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "cannot find value-help-definition with id '" + id + "'");
        }
        valueHelpService.updateDef(valueHelpDef);
        return valueHelpDef;
    }

    /**
     * Deletes value help definitions with the specified IDs.
     *
     * @param ids the array of IDs of the value help definitions to delete
     * @throws BadRequestException     if any ID is missing or blank
     * @throws ResponseStatusException if a definition with any specified ID is not found
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(required = true) String[] ids) {
        if (Arrays.stream(ids).anyMatch(StringUtils::isBlank)) {
            throw new BadRequestException("Missing value for ids");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        for (String id : ids) {
            var resultOpt = valueHelpService.findDefById(id);
            if (resultOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "cannot find value-help-definition with id '" + id + "'");
            }
            valueHelpService.deleteDef(id);
        }
    }

    /**
     * Deletes a value help definition with the specified ID.
     *
     * @param id the ID of the value help definition to delete
     * @throws BadRequestException     if the ID is missing or blank
     * @throws ResponseStatusException if the definition with the specified ID is not found
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }

        final var securitySession = SecurityUtils.getSecuritySession();
        securityService.ensureAuthorized(null, securitySession.getUser(), null, true,
                ValueHelpGrantedAuthorities.SBFX_ValueHelpEdit);

        var resultOpt = valueHelpService.findDefById(id);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "cannot find value-help-definition with id '" + id + "'");
        }
        valueHelpService.deleteDef(id);
    }

    /**
     * Handles validation exceptions for method arguments.
     *
     * @param ex the MethodArgumentNotValidException thrown during validation
     * @return a map containing field names and their corresponding error messages
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
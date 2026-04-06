package com.sap.bfx.valuehelp.api;

import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.security.ValueHelpRoles;
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
import org.springframework.security.authentication.AbstractAuthenticationToken;
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

@RestController
@RequestMapping("api/v1/valuehelpdefs")
@Slf4j
//@CrossOrigin(origins = "http://localhost:3000")
public class ValueHelpDefsController {

    private final ValueHelpService service;
    private final SecurityService securityService;

    @Autowired
    public ValueHelpDefsController(final ValueHelpService service, final SecurityService securityService) {
        this.service = service;
        this.securityService = securityService;
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ValueHelpDef> findAll(@RequestParam(required = false) String search,
                                            @RequestParam(required = false) String[] adapter,
                                            AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findAllDefs(search, adapter);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelpDef findById(@PathVariable String id, AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findDefById(id);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-definition with id '"
                    + id + "'");
        }
        return resultOpt.get();
    }

    @GetMapping(value = "/adapter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllAdapter(AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findAllAdapter();
    }

    @GetMapping(value = "/definedAdapter", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<String> findAllDefinedAdapter(AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findAllDefinedAdapter();
    }

    @GetMapping(value = "/locales", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String[] getLocales(AbstractAuthenticationToken token) {
        securityService.ensureAnyAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpDisplay, ValueHelpRoles.ValueHelpEdit);
        return service.findAllDefinedLocales();
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<InputStreamResource> export(@RequestParam(required = false) String search,
                                                      @RequestParam(required = false) String[] adapter,
                                                      @RequestParam(required = false) String[] ids,
                                                      AbstractAuthenticationToken token) {
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);

        byte[] xmlByteArray;
        if (search != null && search.length() > 0 && adapter != null && adapter.length > 0) {
            xmlByteArray = service.exportDefs(search, adapter);
        } else if (search != null && search.length() > 0) {
            xmlByteArray = service.exportDefs(search);
        } else if (adapter != null && adapter.length > 0) {
            xmlByteArray = service.exportDefs(adapter);
        } else if (ids != null && ids.length > 0) {
            xmlByteArray = service.exportDefsByIds(ids);
        } else {
            xmlByteArray = service.exportDefs();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + "./definitions.xml")
                .contentType(MediaType.APPLICATION_XML)
                .contentLength(xmlByteArray.length)
                .body(new InputStreamResource(new ByteArrayInputStream(xmlByteArray)));
    }

    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ValueHelpDef create(@PathVariable String id, @Valid @RequestBody ValueHelpDef valueHelpDef,
                               AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        if (!id.equals(valueHelpDef.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided ID in URL does not match ID in request body");
        }
        var resultOpt = service.findDefById(id);
        if (resultOpt.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entity with id " + id + " already exists.");
        }
        service.addDef(valueHelpDef);
        return valueHelpDef;
    }

    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<?> uploadXmlFile(@RequestParam("file") MultipartFile file,
                                           @RequestParam boolean override,
                                           @RequestParam boolean useTechnicalName,
                                           AbstractAuthenticationToken token) {
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        String msg;
        if (file.isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body("No file provided");
        }
        try {
            msg = service.importXmlFile(file, override);
        } catch (JAXBException | IOException e) {
            log.debug("Exception during import of xml file.");
            try {
                msg = service.importAbpmXmlFile(file, override, useTechnicalName);
            } catch (JAXBException | IOException e1) {
                log.error("Exception during import of abpm xml file.");
                return ResponseEntity
                        .internalServerError()
                        .body("Error uploading file. Please check file and try again.");
            }
        }
        if (msg != null) {
            return ResponseEntity.ok(msg);
        }
        return ResponseEntity.ok(null);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ValueHelpDef update(@PathVariable String id, @Valid @RequestBody ValueHelpDef valueHelpDef,
                               AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        if (!id.equals(valueHelpDef.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided ID in URL does not match ID in request body");
        }
        var resultOpt = service.findDefById(id);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-definition with id '"
                    + id + "'");
        }
        service.updateDef(valueHelpDef);
        return valueHelpDef;
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam(required = true) String[] ids,
                       AbstractAuthenticationToken token) {
        if (Arrays.stream(ids).anyMatch(StringUtils::isBlank)) {
            throw new BadRequestException("Missing value for ids");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        for (String id : ids) {
            var resultOpt = service.findDefById(id);
            if (resultOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-definition with id '"
                        + id + "'");
            }
            service.deleteDef(id);
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       AbstractAuthenticationToken token) {
        if (StringUtils.isBlank(id)) {
            throw new BadRequestException("Missing id");
        }
        securityService.ensureAuthorized(token, null, Boolean.TRUE, ValueHelpRoles.ValueHelpEdit);
        var resultOpt = service.findDefById(id);
        if (resultOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find value-help-definition with id '"
                    + id + "'");
        }
        service.deleteDef(id);
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
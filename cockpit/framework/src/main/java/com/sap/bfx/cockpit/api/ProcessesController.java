package com.sap.bfx.cockpit.api;

import com.sap.bfx.cockpit.callback.FrontendParams;
import com.sap.bfx.cockpit.callback.FrontendSettings;
import com.sap.bfx.cockpit.callback.SearchParams;
import com.sap.bfx.cockpit.service.CockpitService;
import com.sap.bfx.cockpit.service.ProcessAbstract;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("api/v1/processes")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class ProcessesController {

    private final CockpitService service;

    @Autowired
    public ProcessesController(final CockpitService service) {
        this.service = service;
    }

    /**
     * Get frontend settings.
     *
     * @param req HTTP request
     * @return frontend settings
     */
    @GetMapping(value = "/settings", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public FrontendSettings settings(final HttpServletRequest req) {
        final var params = new FrontendParams();
        params.setLanguage(req.getParameter("language"));

        return service.init(params);
    }

    /**
     * Find processes.
     *
     * @param req HTTP request
     * @return collection of process instance attributes
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Collection<ProcessAbstract> find(final HttpServletRequest req) {

        final var params = new SearchParams();
        params.setLanguage(req.getParameter("language"));

// TODO(ML): Read more search parameters
//        final var descriptionType = req.getParameter("descriptionType");
//        final var descriptionValue = req.getParameter("descriptionValue");
//        final var functionalIdType = req.getParameter("functionalIdType");
//        final var functionalIdValue = req.getParameter("functionalIdValue");
//        final var additionalInformationType = req.getParameter("additionalInformationType");
//        final var additionalInformationValue = req.getParameter("additionalInformationValue");
//        final var searchParameters = req.getParameterValues("searchParameters");
//        final var status = req.getParameterValues("status");
//        final var user = req.getParameter("user");
//        final var roleUser = req.getParameterValues("roleUser");
//        final var startedBy = req.getParameter("startedBy");
//        final var endedOn = req.getParameter("endedOn");
//        final var scenario = req.getParameter("scenario");
//
//        if (descriptionType != null && (descriptionValue == null || descriptionValue.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//        if (descriptionValue != null && (descriptionType == null || descriptionType.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//
//        if (functionalIdType != null && (functionalIdValue == null || functionalIdValue.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//        if (functionalIdValue != null && (functionalIdType == null || functionalIdType.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//
//        if (additionalInformationType != null
//                && (additionalInformationValue == null || additionalInformationValue.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }
//        if (additionalInformationValue != null
//                && (additionalInformationType == null || additionalInformationType.trim().length() < 1)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
//        }

        return service.findProcesses(params);
    }

//    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public FormAttributes findById(@PathVariable String id) {
//        var resultOpt = service.findProcessById(id);
//        if (resultOpt.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find process instance with id '"
//                    + id + "'");
//        }
//        return resultOpt.get();
//    }

//    @PostMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.CREATED)
//    @ResponseBody
//    public ProcessInstance create(@PathVariable String id, @Valid @RequestBody ProcessInstance processInstance) {
//        if (!id.equals(processInstance.getId())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided id in URL does not match " +
//                    "id in request body");
//        }
//        var resultOpt = service.findProcessById(id);
//        if (resultOpt.isPresent()) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Entity with id " + id
//                    + " already exists.");
//        }
//        service.addProcess(processInstance);
//        return processInstance;
//    }
//
//    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
//    public ProcessInstance update(@PathVariable String id, @Valid @RequestBody ProcessInstance processInstance) {
//        if (!id.equals(processInstance.getId())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided id in URL does not match " +
//                    "id in request body");
//        }
//        var resultOpt = service.findProcessById(id);
//        if (resultOpt.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find process instance with id '"
//                    + id + "'");
//        }
//        service.updateProcess(processInstance);
//        return processInstance;
//    }
//
//    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable String id) {
//        var resultOpt = service.findProcessById(id);
//        if (resultOpt.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "cannot find process instance with id '"
//                    + id + "'");
//        }
//        service.deleteProcess(id);
//    }
}
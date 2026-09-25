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
        params.setSearchParameters(req.getParameterValues("profiles"));
        params.setDescriptionType(req.getParameter("descriptionType"));
        params.setDescriptionValue(req.getParameter("descriptionValue"));
        params.setFunctionalIdType(req.getParameter("functionalIdType"));
        params.setFunctionalIdValue(req.getParameter("functionalIdValue"));
        params.setAdditionalInformationType(req.getParameter("additionalInformationType"));
        params.setAdditionalInformationValue(req.getParameter("additionalInformationValue"));
        params.setStatus(req.getParameterValues("status"));
        params.setUser(req.getParameter("user"));
        params.setRoleUser(req.getParameterValues("roleUser"));
        params.setStartedBy(req.getParameter("startedBy"));
        params.setEndedOn(req.getParameter("endedOn"));
        params.setScenario(req.getParameter("scenario"));

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
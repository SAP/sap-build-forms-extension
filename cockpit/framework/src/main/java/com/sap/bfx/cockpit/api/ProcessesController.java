package com.sap.bfx.cockpit.api;

import com.sap.bfx.cockpit.callback.FrontendParams;
import com.sap.bfx.cockpit.callback.FrontendSettings;
import com.sap.bfx.cockpit.callback.SearchParams;
import com.sap.bfx.cockpit.service.AddFeedRequest;
import com.sap.bfx.cockpit.service.CockpitService;
import com.sap.bfx.cockpit.service.FeedEntry;
import com.sap.bfx.cockpit.service.ProcessPage;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.security.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * @return paged collection of process instance attributes with total count
     */
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ProcessPage find(
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "profiles", required = false) String[] profiles,
            @RequestParam(value = "descriptionType", required = false) String descriptionType,
            @RequestParam(value = "descriptionValue", required = false) String[] descriptionValue,
            @RequestParam(value = "functionalIdType", required = false) String functionalIdType,
            @RequestParam(value = "functionalIdValue", required = false) String[] functionalIdValue,
            @RequestParam(value = "additionalInformationType", required = false) String additionalInformationType,
            @RequestParam(value = "additionalInformationValue", required = false) String[] additionalInformationValue,
            @RequestParam(value = "status", required = false) String[] status,
            @RequestParam(value = "user", required = false) String user,
            @RequestParam(value = "roleUser", required = false) String[] roleUser,
            @RequestParam(value = "startedBy", required = false) String startedBy,
            @RequestParam(value = "endedOn", required = false) String endedOn,
            @RequestParam(value = "scenario", required = false) String scenario,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {

        final var params = new SearchParams();
        params.setLanguage(language);
        params.setSearchParameters(profiles);
        params.setDescriptionType(descriptionType);
        params.setDescriptionValue(descriptionValue);
        params.setFunctionalIdType(functionalIdType);
        params.setFunctionalIdValue(functionalIdValue);
        params.setAdditionalInformationType(additionalInformationType);
        params.setAdditionalInformationValue(additionalInformationValue);
        params.setStatus(status);
        params.setUser(user);
        params.setRoleUser(roleUser);
        params.setStartedBy(startedBy);
        params.setEndedOn(endedOn);
        params.setScenario(scenario);
        params.setPage(page);
        params.setPageSize(pageSize);
        params.setCurrentUser(SecurityUtils.getUserName());

        return service.findProcesses(params);
    }

    /**
     * Returns distinct non-blank values for a given field, filtered by a search substring.
     * Used to populate search-help dialogs in the cockpit filter panel.
     *
     * @param field  logical field name: description, functionalId, or additionalInformation
     * @param search substring to filter by; empty string returns all values
     * @return sorted list of distinct matching values
     */
    @GetMapping(value = "/suggestions", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<String> suggestions(
            @RequestParam(value = "field") String field,
            @RequestParam(value = "search", defaultValue = "") String search) {
        return service.findSuggestions(field, search);
    }

    /**
     * Get all feed entries for a process, ordered by pos ASC.
     */
    @GetMapping(value = "/{id}/feeds", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<FeedEntry> findFeeds(@PathVariable("id") String id) {
        if (StringUtils.isBlank(id)) throw new BadRequestException("missing process-id");
        return service.findFeeds(id);
    }

    /**
     * Add a new feed entry to a process.
     * The author is resolved server-side from the security context.
     */
    @PostMapping(value = "/{id}/feeds",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public FeedEntry addFeed(@PathVariable("id") String id, @RequestBody AddFeedRequest req) {
        if (StringUtils.isBlank(id)) throw new BadRequestException("missing process-id");
        if (StringUtils.isBlank(req.text())) throw new BadRequestException("missing feed text");
        if (StringUtils.isBlank(req.type())) throw new BadRequestException("missing feed type");
        final FeedEntry result = service.addFeed(id, SecurityUtils.getUserName(), req.text(), req.type(), req.parentId());
        if (result == null) throw new NotFoundException("cannot find process with id '" + id + "'");
        return result;
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
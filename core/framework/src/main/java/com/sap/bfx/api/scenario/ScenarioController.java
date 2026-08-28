package com.sap.bfx.api.scenario;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sap.bfx.api.scenario.json.FieldResponse;
import com.sap.bfx.api.scenario.json.ScenarioBaseUrlResponse;
import com.sap.bfx.api.scenario.json.serializer.*;
import com.sap.bfx.callback.CallbackService;
import com.sap.bfx.callback.ContextFactory;
import com.sap.bfx.callback.FormsApi;
import com.sap.bfx.definition.DateRange;
import com.sap.bfx.definition.EventType;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.SecurityUtils;
import com.sap.bfx.session.*;
import com.sap.bfx.utils.IdentifierUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ScenarioController is a REST controller that provides endpoints for interacting with form processes and their fields.
 * It allows retrieving field values in various formats, triggering events, and setting process states.
 */
@RestController
@RequestMapping("api/v1/scenario")
@Slf4j
public class ScenarioController {

    public static final String N_A = "n/a";
    public static final String SCENARIO_FIELD_NAME = "scenarioFieldName";
    private static final String FIELD_IS_NOT_AVAILABLE = "Field with name ''{0}'' is not applicable";
    private static final Map<Class<?>, Class<?>> sourceClassToTargetClass = new HashMap<>();

    static {
        sourceClassToTargetClass.put(DateRange.class, Map.class);
        sourceClassToTargetClass.put(ElementRow.class, Map.class);
        sourceClassToTargetClass.put(Table.class, ArrayList.class);
        sourceClassToTargetClass.put(FieldResponse.class, FieldResponse.class);
        sourceClassToTargetClass.put(LocalDate.class, String.class);
        sourceClassToTargetClass.put(LocalDateTime.class, String.class);
        sourceClassToTargetClass.put(LocalTime.class, String.class);
        sourceClassToTargetClass.put(String.class, String.class);
        sourceClassToTargetClass.put(Boolean.class, Boolean.class);
        sourceClassToTargetClass.put(Integer.class, Integer.class);
        sourceClassToTargetClass.put(BigDecimal.class, BigDecimal.class);
        sourceClassToTargetClass.put(Object.class, Object.class);
        sourceClassToTargetClass.put(MoneyAmount.class, String.class);
    }

    private final FormsService formsService;
    private final SecurityService securityService;
    private final CallbackService callbackService;
    private final ContextFactory contextFactory;
    private final SessionService sessionService;
    private final ObjectMapper om;

    /**
     * Constructor for ScenarioController.
     *
     * @param formsService    the FormsService instance
     * @param securityService the SecurityService instance
     * @param callbackService the CallbackService instance
     * @param contextFactory  the ContextFactory instance
     * @param sessionService  the SessionService instance
     */
    @Autowired
    public ScenarioController(final FormsService formsService, final SecurityService securityService,
                              CallbackService callbackService, ContextFactory contextFactory,
                              SessionService sessionService) {
        this.formsService = formsService;
        this.callbackService = callbackService;
        this.securityService = securityService;
        this.contextFactory = contextFactory;
        this.sessionService = sessionService;
        om = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(DateRange.class, new DateRangeSerializer());
        module.addSerializer(ElementRow.class, new ElementRowSerializer());
        module.addSerializer(Table.class, new TableSerializer());
        module.addSerializer(FieldResponse.class, new FieldResponseSerializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addSerializer(LocalTime.class, new LocalTimeSerializer());
        module.addSerializer(MoneyAmount.class, new MoneyAmountSerializer());
        om.registerModule(module);
    }

    private static Collection<Object> getCollection(Optional<Object> optionalObject) {
        Collection<Object> outputColl = new ArrayList<>();
        if (optionalObject.isPresent()) {
            //TODO evt. noch über die Sortierung der ScenarioDefinition gehen
            //ScenarioDefinition sd = definitionService.findDefinitionByVersion(form.getScenarioVersion()).get();
            //List<ElementDefinition> tableElements = /*form.getSd()*/sd.findElementByKey(key).getElements();
            Table table = (Table) optionalObject.get();
            List<String> rows = table.getRows();
            //rows.forEach(r -> table.getData().get(r).getElements().forEach((eleKey, eleValue) -> outputColl.add(new FieldResponse<>(eleValue.getName(), eleValue.getValue()))));
            rows.forEach(r -> outputColl.add(table.getData().get(r)));
            //form.getValue(r, e.getKey(), Object.class))));
        }
        return outputColl;
    }

    /**
     * Endpoint to retrieve the base URL for forms scenario.
     *
     * @param request the HttpServletRequest object
     * @return ResponseEntity containing the base URL in JSON format
     */
    @GetMapping(value = "/forms-scenario-base-url", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get forms scenario base URL",
            description = "This operation returns the string of forms scenario base URL, where the SessionController of the scenario is executed.")
    public ResponseEntity<ScenarioBaseUrlResponse> getFormsScenarioBaseUrl(HttpServletRequest request) {
// TODO(ML) Check: as we don't deliver any data from the server (it's just returning the base URL of the call itself),
// we don't need to check for authorization here. The base URL is not sensitive information.
//        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, null,
//                (String) null);
        String serverName =
                request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ScenarioBaseUrlResponse(serverName));
    }

    /**
     * Endpoint to retrieve a boolean field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @return ResponseEntity containing the boolean field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsBoolean", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as boolean",
            description = "This operation returns one boolean field of a form process.")
    public ResponseEntity<FieldResponse<Boolean>> getFieldAsBoolean(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, false, ElementRow.ROOT, IdentifierUtils.key(scenarioFieldName));

        Boolean fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, Boolean.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

    /**
     * Endpoint to retrieve a date field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @return ResponseEntity containing the date field value in JSON format (yyyy-MM-dd)
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsDate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as date",
            description = "This operation returns one date field of a form process in the format yyyy-MM-dd.")
    //@ApiResponse(responseCode = "200", description = "Date is in the format yyyy-MM-dd", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "yyyy-MM-dd", description = "Time is in the format yyyy-MM-dd",*/ example = "{\"fieldValue\":\"2025-07-03\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsDate(@RequestParam(required = true) String formsProcessId,
                                                                @RequestParam(required = true) String scenarioFieldName)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, false, ElementRow.ROOT, IdentifierUtils.key(scenarioFieldName));

        LocalDate fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalDate.class);
        String fieldDate =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldDate));
    }

    /**
     * Endpoint to retrieve a time field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @return ResponseEntity containing the time field value in JSON format (HH:mm)
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsTime", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as time",
            description = "This operation returns one time field of a form process in the format HH:mm.")
    //@ApiResponse(responseCode = "200", description = "Time is in the format HH:mm", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "HH:mm", description = "Time is in the format HH:mm",*/ example = "{\"fieldValue\":\"10:25\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsTime(@RequestParam(required = true) String formsProcessId,
                                                                @RequestParam(required = true) String scenarioFieldName)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, false, ElementRow.ROOT, IdentifierUtils.key(scenarioFieldName));

        LocalTime fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalTime.class);
        String fieldTime =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldTime));
    }

    /**
     * Endpoint to retrieve a date range field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @return ResponseEntity containing the date range field value in JSON format (Map with start and end dates)
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsDateRange", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as date range",
            description = "This operation returns one date range field of a form process.")
    public ResponseEntity<FieldResponse<Map<String, String>>> getFieldAsDateRange(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, false, ElementRow.ROOT, IdentifierUtils.key(scenarioFieldName));

        DateRange fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, DateRange.class);
        Map<String, String> fieldDateRange = (Map<String, String>) this.getObjectViaObjectMapper(fieldValue,
                sourceClassToTargetClass.get(DateRange.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldDateRange));
    }

    /**
     * Endpoint to retrieve a datetime field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @return ResponseEntity containing the datetime field value in JSON format (yyyy-MM-dd'T'HH:mm:ss)
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsDateTime", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as datetime",
            description = "This operation returns one date time field of a form process in the format yyyy-MM-dd'T'HH:mm:ss.")
    //@ApiResponse(responseCode = "200", description = "Datetime is in the format yyyy-MM-dd'T'HH:mm:ss", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "yyyy-MM-dd'T'HH:mm:ss", description = "Datetime is in the format yyyy-MM-dd'T'HH:mm:ss",*/ example = "{\"fieldValue\":\"2025-07-03T10:25\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsDateTime(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, false, ElementRow.ROOT, IdentifierUtils.key(scenarioFieldName));

        LocalDateTime fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalDateTime.class);
        String fieldDateTime =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldDateTime));
    }

    /**
     * Endpoint to retrieve a string field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @param token             the authentication token
     * @return ResponseEntity containing the string field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsString", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as string",
            description = "This operation returns one string field of a form process.")
    public ResponseEntity<FieldResponse<String>> getFieldAsString(@RequestParam(required = true) String formsProcessId,
                                                                  @RequestParam(required = true)
                                                                  String scenarioFieldName,
                                                                  AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));

        String fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, String.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

    /**
     * Endpoint to retrieve an integer field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @param token             the authentication token
     * @return ResponseEntity containing the integer field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsInteger", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as integer",
            description = "This operation returns one integer field of a form process.")
    public ResponseEntity<FieldResponse<Integer>> getFieldAsInteger(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));

        Integer fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, Integer.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

    /**
     * Endpoint to retrieve a decimal field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @param token             the authentication token
     * @return ResponseEntity containing the decimal field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldAsDecimal", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as decimal",
            description = "This operation returns one decimal field of a form process.")
    public ResponseEntity<FieldResponse<BigDecimal>> getFieldAsDecimal(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));

        BigDecimal fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, BigDecimal.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

    /**
     * Endpoint to retrieve a collection field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @param token             the authentication token
     * @return ResponseEntity containing the collection field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/collection", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get collection",
            description = "This operation returns a collection (table) of a form process.")
    public ResponseEntity<FieldResponse<Collection<Object>>> getCollection(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));

        Collection<Object> targetColl = getObjectCollection(scenarioFieldName, form);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, targetColl));
    }

    /**
     * Endpoint to retrieve a serialized collection field value from a form process.
     *
     * @param formsProcessId    the ID of the form process
     * @param scenarioFieldName the name of the scenario field
     * @param token             the authentication token
     * @return ResponseEntity containing the serialized collection field value in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/collectionSerialized", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get collection in a serialized string",
            description = "This operation returns a collection (table) of a form process in a serialized string.")
    public ResponseEntity<FieldResponse<String>> getCollectionSerialized(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));

        Collection<Object> targetColl = getObjectCollection(scenarioFieldName, form);
        String jsonSerialized = "";
        try {
            jsonSerialized = om.writeValueAsString(targetColl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>(scenarioFieldName, jsonSerialized));
    }

    /**
     * Endpoint to retrieve multiple fields from a form process.
     *
     * @param formsProcessId     the ID of the form process
     * @param scenarioFieldNames the list of scenario field names
     * @param token              the authentication token
     * @return ResponseEntity containing a map of field names and their corresponding values in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get fields", description = "This operation returns multiple fields from a form process.")
    public ResponseEntity<FieldResponse<Map<String, Object>>> getFields(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) List<String> scenarioFieldNames, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (scenarioFieldNames.stream().anyMatch(StringUtils::isBlank)) {
            throw new BadRequestException("Missing value for a scenarioFieldNames");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();
        String[] sourceKeys = scenarioFieldNames.stream().map(IdentifierUtils::key).toList().toArray(new String[0]);

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT, sourceKeys);

        Map<String, Object> fieldMap = getFieldMap(scenarioFieldNames, form);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new FieldResponse<>(null, fieldMap));
    }

    /**
     * Endpoint to retrieve multiple fields from a form process in a serialized string.
     *
     * @param formsProcessId     the ID of the form process
     * @param scenarioFieldNames the list of scenario field names
     * @param token              the authentication token
     * @return ResponseEntity containing a serialized string of field names and their corresponding values in JSON format
     * @throws Exception if any error occurs during processing
     */
    @GetMapping(value = "/fieldsSerialized", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get fields in a serialized string",
            description = "This operation returns multiple fields from a form process in a serialized string.")
    public ResponseEntity<FieldResponse<String>> getFieldsSerialized(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) List<String> scenarioFieldNames, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (scenarioFieldNames.stream().anyMatch(StringUtils::isBlank)) {
            throw new BadRequestException("Missing value for a scenarioFieldNames");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();
        String[] sourceKeys = scenarioFieldNames.stream().map(IdentifierUtils::key).toList().toArray(new String[0]);

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT, sourceKeys);

        Map<String, Object> fieldMap = getFieldMap(scenarioFieldNames, form);
        String jsonSerialized = "";
        try {
            jsonSerialized = om.writeValueAsString(fieldMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                             .body(new FieldResponse<>("fieldValue", jsonSerialized));
    }

    /**
     * Endpoint to trigger an event for a form process.
     *
     * @param formsProcessId the ID of the form process
     * @param eventName      the name of the event to trigger
     * @param token          the authentication token
     * @return ResponseEntity indicating the execution status of the triggered event
     * @throws Exception if any error occurs during processing
     */
    @PostMapping(value = "/event/{formsProcessId}/{eventName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Trigger event",
            description = "This operation triggers the execution of a scenario event for a form process.")
    public ResponseEntity<String> triggerEvent(@PathVariable(required = true) String formsProcessId,
                                               @PathVariable(required = true) String eventName,
                                               AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(eventName)) {
            throw new BadRequestException("Missing eventName");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.PostScenarioControllerAuth, false, null);
        log.debug("FormsProcessId:{},EventName:{}", formsProcessId, eventName);

        final var preCtx =
                contextFactory.createContext(securitySession, form.getSd(), null, null, null, Form.ROOT, eventName,
                        null);
        var session = sessionService.create(form.getSd(), form, preCtx);
        var ctx = contextFactory.createContext(securitySession, form.getSd(), session, preCtx.getDisplayState(),
                preCtx.getLocale(), preCtx.getSource().getRowId(), preCtx.getSource().getKey(),
                preCtx.getTaskInstanceId());
        var result = callbackService.callEvent(session, Form.ROOT, eventName, EventType.TriggerEvent, ctx, null);
        // persist into DB
        //if (ctx.getSaveIntoDB()) {
        FormsApi formsApi = ctx.getApi(FormsApi.class);
        formsApi.save();
        //}
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("Execution of triggerEvent() is done!");
    }

    /**
     * Endpoint to set the state of a form process.
     *
     * @param formsProcessId the ID of the form process
     * @param stateValue     the new state value to set
     * @param token          the authentication token
     * @return ResponseEntity indicating the execution status of setting the process state
     * @throws Exception if any error occurs during processing
     */
    @PostMapping(value = "/process/{formsProcessId}/{stateValue}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Set process state", description = "This operation sets a state for a form process.")
    public ResponseEntity<String> setProcessState(@PathVariable(required = true) String formsProcessId,
                                                  @PathVariable(required = true) String stateValue,
                                                  AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(stateValue)) {
            throw new BadRequestException("Missing stateValue");
        }

        Form form = formsService.loadById(formsProcessId);
        final var securitySession = SecurityUtils.getSecuritySession();

        securityService.ensureAuthorized(form.getSd().getName(), securitySession.getUser(),
                EventType.PostScenarioControllerAuth, false, null);

        log.debug("FormsProcessId:{},ProcessState:{}", formsProcessId, stateValue);
        try {
            switch (stateValue.toLowerCase()) {
                case "draft":
                    form.setState(ProcessState.Draft);
                    break;
                case "submitted":
                    form.setState(ProcessState.Submitted);
                    break;
                case "running":
                    form.setState(ProcessState.Running);
                    break;
                case "cancelled":
                    form.setState(ProcessState.Cancelled);
                    break;
                case "finished":
                    form.setState(ProcessState.Finished);
                    break;
                default:
                    form.setState(ProcessState.valueOf(stateValue));
                    break;
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("The stateValue is not valid");
        }
        final var preCtx =
                contextFactory.createContext(securitySession, form.getSd(), null, null, null, Form.ROOT, null, null);
        var session = sessionService.create(form.getSd(), form, preCtx);
        var ctx = contextFactory.createContext(securitySession, form.getSd(), session, preCtx.getDisplayState(),
                preCtx.getLocale(), preCtx.getSource().getRowId(), preCtx.getSource().getKey(),
                preCtx.getTaskInstanceId());
        FormsApi formsApi = ctx.getApi(FormsApi.class);
        formsApi.save();
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("Execution of setProcessState() is done!");
    }

    /**
     * Helper method to retrieve the value of a scenario field from a form.
     *
     * @param form              the Form object
     * @param scenarioFieldName the name of the scenario field
     * @param returnType        the expected return type of the field value
     * @param <T>               the type parameter for the return type
     * @return the value of the specified scenario field, cast to the specified return type
     * @throws NotExistingFieldException if the specified field does not exist in the form
     */
    @SuppressWarnings("unchecked")
    private <T> T getScenarioFieldValue(Form form, String scenarioFieldName, Class<T> returnType) {
        if (returnType == null) {
            throw new IllegalArgumentException("Unset return type for getValueOrDefault()");
        }
        String key = IdentifierUtils.key(scenarioFieldName);
        if (returnType == Object.class) {
            Optional<Object> optionalObject = form.getValue(ElementRow.ROOT, key);
            optionalObject.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
            return (T) getScenarioFieldValue(form, scenarioFieldName, optionalObject.get().getClass());
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            Optional<Boolean> optionalBoolean = form.getValue(ElementRow.ROOT, key, Boolean.class);
            return (T) optionalBoolean.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == LocalDate.class) {
            Optional<LocalDate> optionalLocalDate = form.getValue(ElementRow.ROOT, key, LocalDate.class);
            return (T) optionalLocalDate.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == LocalTime.class) {
            Optional<LocalTime> optionalLocalTime = form.getValue(ElementRow.ROOT, key, LocalTime.class);
            return (T) optionalLocalTime.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == DateRange.class) {
            Optional<DateRange> optionalDateRange = form.getValue(ElementRow.ROOT, key, DateRange.class);
            return (T) optionalDateRange.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == LocalDateTime.class) {
            Optional<LocalDateTime> optionalLocalDateTime = form.getValue(ElementRow.ROOT, key, LocalDateTime.class);
            return (T) optionalLocalDateTime.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == int.class || returnType == Integer.class) {
            Optional<Integer> optionalInteger = form.getValue(ElementRow.ROOT, key, Integer.class);
            return (T) optionalInteger.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == BigDecimal.class) {
            Optional<BigDecimal> optionalBigDecimal = form.getValue(ElementRow.ROOT, key, BigDecimal.class);
            return (T) optionalBigDecimal.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        } else if (returnType == Collection.class) {
            Optional<Object> optionalObject = form.getValue(ElementRow.ROOT, key, Object.class);
            optionalObject.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
            Collection<Object> outputColl = getCollection(optionalObject);
            return (T) outputColl;
        } else {
            //default is String
            Optional<String> optionalString = form.getValue(ElementRow.ROOT, key, String.class);
            return (T) optionalString.orElseThrow(
                    () -> new NotExistingFieldException(MessageFormat.format(FIELD_IS_NOT_AVAILABLE, scenarioFieldName),
                            scenarioFieldName));
        }
    }

    /**
     * Helper method to convert an object to the specified target class using ObjectMapper.
     *
     * @param objValue the object to be converted
     * @param clazz    the target class to convert the object to
     * @param <T>      the type parameter for the target class
     * @return the converted object of the specified target class
     */
    @SuppressWarnings("unchecked")
    private <T> T getObjectViaObjectMapper(Object objValue, Class<T> clazz) {
        try {
            if (clazz == String.class && objValue instanceof String) {
                return (T) objValue;
            } else if (objValue instanceof LocalDate || objValue instanceof LocalTime ||
                    objValue instanceof LocalDateTime || objValue instanceof MoneyAmount) {
                return (T) om.writeValueAsString(objValue);
            } else if (objValue instanceof DateRange tempDateRange) {
                return om.readValue(om.writeValueAsBytes(tempDateRange), clazz);
            } else {
                return om.readValue(om.writeValueAsBytes(objValue), clazz);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to retrieve a collection of objects from a form process field.
     *
     * @param scenarioFieldName the name of the scenario field
     * @param form              the Form object
     * @return Collection of objects retrieved from the specified field
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> getObjectCollection(String scenarioFieldName, Form form) {
        Collection<Object> sourceColl = this.getScenarioFieldValue(form, scenarioFieldName, Collection.class);
        Collection<Object> targetColl = sourceColl.stream().map(o -> this.getObjectViaObjectMapper(o,
                sourceClassToTargetClass.get(o.getClass()))).collect(Collectors.toList());
        return targetColl;
    }

    /**
     * Helper method to retrieve a map of field names and their corresponding values from a form process.
     *
     * @param scenarioFieldNames the list of scenario field names
     * @param form               the Form object
     * @return Map containing field names as keys and their corresponding values as values
     */
    private Map<String, Object> getFieldMap(List<String> scenarioFieldNames, Form form) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (String f : scenarioFieldNames) {
            Object tempField = this.getScenarioFieldValue(form, f, Object.class);
            fieldMap.put(f,
                    this.getObjectViaObjectMapper(tempField, sourceClassToTargetClass.get(tempField.getClass())));
        }
        return fieldMap;
    }
}

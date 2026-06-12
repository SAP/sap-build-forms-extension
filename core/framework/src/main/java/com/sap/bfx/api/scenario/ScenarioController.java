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

    @Autowired
    public ScenarioController(final FormsService formsService, final SecurityService securityService, CallbackService callbackService,
                              ContextFactory contextFactory, SessionService sessionService) {
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

    @GetMapping(value = "/forms-scenario-base-url", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get forms scenario base URL",
            description = "This operation returns the string of forms scenario base URL, where the SessionController of the scenario is executed.")
    public ResponseEntity<ScenarioBaseUrlResponse> getFormsScenarioBaseUrl(HttpServletRequest request,
                                                                           AbstractAuthenticationToken token) {
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, null, null);
        String serverName =
                request.getRequestURL().substring(0, request.getRequestURL().indexOf(request.getRequestURI()));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new ScenarioBaseUrlResponse(serverName));
    }

    @GetMapping(value = "/fieldAsBoolean", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as boolean",
            description = "This operation returns one boolean field of a form process.")
    public ResponseEntity<FieldResponse<Boolean>> getFieldAsBoolean(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        Boolean fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, Boolean.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

    @GetMapping(value = "/fieldAsDate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as date",
            description = "This operation returns one date field of a form process in the format yyyy-MM-dd.")
    //@ApiResponse(responseCode = "200", description = "Date is in the format yyyy-MM-dd", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "yyyy-MM-dd", description = "Time is in the format yyyy-MM-dd",*/ example = "{\"fieldValue\":\"2025-07-03\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsDate(@RequestParam(required = true) String formsProcessId,
                                                                @RequestParam(required = true) String scenarioFieldName,
                                                                AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        LocalDate fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalDate.class);
        String fieldDate =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldDate));
    }

    @GetMapping(value = "/fieldAsTime", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as time",
            description = "This operation returns one time field of a form process in the format HH:mm.")
    //@ApiResponse(responseCode = "200", description = "Time is in the format HH:mm", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "HH:mm", description = "Time is in the format HH:mm",*/ example = "{\"fieldValue\":\"10:25\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsTime(@RequestParam(required = true) String formsProcessId,
                                                                @RequestParam(required = true) String scenarioFieldName,
                                                                AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        LocalTime fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalTime.class);
        String fieldTime =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldTime));
    }


    @GetMapping(value = "/fieldAsDateRange", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as date range",
            description = "This operation returns one date range field of a form process.")
    public ResponseEntity<FieldResponse<Map<String, String>>> getFieldAsDateRange(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        DateRange fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, DateRange.class);
        Map<String, String> fieldDateRange = (Map<String, String>) this.getObjectViaObjectMapper(fieldValue,
                sourceClassToTargetClass.get(DateRange.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldDateRange));
    }

    @GetMapping(value = "/fieldAsDateTime", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Get field as datetime",
            description = "This operation returns one date time field of a form process in the format yyyy-MM-dd'T'HH:mm:ss.")
    //@ApiResponse(responseCode = "200", description = "Datetime is in the format yyyy-MM-dd'T'HH:mm:ss", content = @Content(schema = @Schema(implementation = FieldResponse.class, /*format = "yyyy-MM-dd'T'HH:mm:ss", description = "Datetime is in the format yyyy-MM-dd'T'HH:mm:ss",*/ example = "{\"fieldValue\":\"2025-07-03T10:25\"}")))
    public ResponseEntity<FieldResponse<String>> getFieldAsDateTime(
            @RequestParam(required = true) String formsProcessId,
            @RequestParam(required = true) String scenarioFieldName, AbstractAuthenticationToken token)
            throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(scenarioFieldName)) {
            throw new BadRequestException("Missing scenarioFieldName");
        }
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        LocalDateTime fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, LocalDateTime.class);
        String fieldDateTime =
                (String) this.getObjectViaObjectMapper(fieldValue, sourceClassToTargetClass.get(String.class));
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldDateTime));
    }

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
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        String fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, String.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

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
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        Integer fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, Integer.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

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
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        BigDecimal fieldValue = this.getScenarioFieldValue(form, scenarioFieldName, BigDecimal.class);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, fieldValue));
    }

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
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
        Collection<Object> targetColl = getObjectCollection(scenarioFieldName, form);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>(scenarioFieldName, targetColl));
    }

    @SuppressWarnings("unchecked")
    private Collection<Object> getObjectCollection(String scenarioFieldName, Form form) {
        Collection<Object> sourceColl = this.getScenarioFieldValue(form, scenarioFieldName, Collection.class);
        Collection<Object> targetColl = sourceColl.stream().map(o -> this.getObjectViaObjectMapper(o,
                sourceClassToTargetClass.get(o.getClass()))).collect(Collectors.toList());
        return targetColl;
    }

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
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                IdentifierUtils.key(scenarioFieldName));
        Form form = formsService.loadById(formsProcessId);
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
        String[] sourceKeys = scenarioFieldNames.stream().map(IdentifierUtils::key).toList().toArray(new String[0]);
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                sourceKeys);
        Form form = formsService.loadById(formsProcessId);
        Map<String, Object> fieldMap = getFieldMap(scenarioFieldNames, form);
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(new FieldResponse<>(null, fieldMap));
    }

    private Map<String, Object> getFieldMap(List<String> scenarioFieldNames, Form form) {
        Map<String, Object> fieldMap = new HashMap<>();
        for (String f : scenarioFieldNames) {
            Object tempField = this.getScenarioFieldValue(form, f, Object.class);
            fieldMap.put(f,
                    this.getObjectViaObjectMapper(tempField, sourceClassToTargetClass.get(tempField.getClass())));
        }
        return fieldMap;
    }

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
        String[] sourceKeys = scenarioFieldNames.stream().map(IdentifierUtils::key).toList().toArray(new String[0]);
        securityService.ensureAuthorized(token, EventType.GetScenarioControllerAuth, Boolean.FALSE, ElementRow.ROOT,
                sourceKeys);
        Form form = formsService.loadById(formsProcessId);
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

    @PostMapping(value = "/event/{formsProcessId}/{eventName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Trigger event",
            description = "This operation triggers the execution of a scenario event for a form process.")
    public ResponseEntity<String> triggerEvent(@PathVariable(required = true) String formsProcessId, @PathVariable(required = true) String eventName,
                                               AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(eventName)) {
            throw new BadRequestException("Missing eventName");
        }
        securityService.ensureAuthorized(token, EventType.PostScenarioControllerAuth, null, null);
        log.debug("FormsProcessId:{},EventName:{}", formsProcessId, eventName);
        Form form = formsService.loadById(formsProcessId);
        final var preCtx =
                contextFactory.createContext(token, form.getSd(), null, null, null, Form.ROOT, eventName, null);
        var session = sessionService.create(form.getSd(), form, preCtx);
        var ctx = contextFactory.createContext(token, form.getSd(), session, preCtx.getDisplayState(), preCtx.getLocale(), preCtx.getSource().getRowId(), preCtx.getSource().getKey(), preCtx.getTaskInstanceId());
        var result = callbackService.callEvent(session, Form.ROOT, eventName, EventType.TriggerEvent, ctx, null);
        // persist into DB
        //if (ctx.getSaveIntoDB()) {
        FormsApi formsApi = ctx.getApi(FormsApi.class);
        formsApi.save();
        //}
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("Execution of triggerEvent() is done!");
    }

    @PostMapping(value = "/process/{formsProcessId}/{stateValue}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Operation(summary = "Set process state", description = "This operation sets a state for a form process.")
    public ResponseEntity<String> setProcessState(@PathVariable(required = true) String formsProcessId, @PathVariable(required = true) String stateValue,
                                                  AbstractAuthenticationToken token) throws Exception {
        if (StringUtils.isBlank(formsProcessId)) {
            throw new BadRequestException("Missing formsProcessId");
        }
        if (StringUtils.isBlank(stateValue)) {
            throw new BadRequestException("Missing stateValue");
        }
        securityService.ensureAuthorized(token, EventType.PostScenarioControllerAuth, null, null);
        log.debug("FormsProcessId:{},ProcessState:{}", formsProcessId, stateValue);
        Form form = formsService.loadById(formsProcessId);
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
                contextFactory.createContext(token, form.getSd(), null, null, null, Form.ROOT, null, null);
        var session = sessionService.create(form.getSd(), form, preCtx);
        var ctx = contextFactory.createContext(token, form.getSd(), session, preCtx.getDisplayState(), preCtx.getLocale(), preCtx.getSource().getRowId(), preCtx.getSource().getKey(), preCtx.getTaskInstanceId());
        FormsApi formsApi = ctx.getApi(FormsApi.class);
        formsApi.save();
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body("Execution of setProcessState() is done!");
    }
}

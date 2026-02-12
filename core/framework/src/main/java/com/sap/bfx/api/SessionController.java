package com.sap.bfx.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.bfx.valuehelp.ValueHelpClient;
import com.sap.bfx.callback.CallbackService;
import com.sap.bfx.callback.ConfigurationService;
import com.sap.bfx.callback.ContextFactory;
import com.sap.bfx.callback.LifecycleHookType;
import com.sap.bfx.definition.DefinitionService;
import com.sap.bfx.definition.EventType;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.exception.BadRequestException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.security.FormsRoles;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.session.*;
import com.sap.bfx.workflow.TaskInputContext;
import com.sap.bfx.workflow.WorkflowService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * TODO
 * - Parallelise different parts of the event processing
 * - Create the json response stream by coding itself and allow parallel
 * processing with REDIS storage of session
 * see <a href=
 * "https://technicalsand.com/streaming-data-spring-boot-restful-web-service/">here</a>.
 */
@RestController
@RequestMapping("api/v1/sessions")
@Slf4j
@Hidden
public class SessionController {

    private final DefinitionService definitionService;
    private final SessionService sessionService;
    private final ValueHelpClient valueHelpClient;
    private final TaskExecutor taskExecutor;
    private final CallbackService callbackService;
    private final ControllerUtils utils;
    private final ContextFactory contextFactory;
    private final WorkflowService workflowService;
    private final FormsService formsService;
    private final ConfigurationService configurationService;
    private final SecurityService securityService;

    /**
     * Constructor with all necessary services.
     *
     * @param scenarioService      scenario definition service
     * @param sessionService       session service
     * @param callbackService      callback service
     * @param valueHelpClient      value help client
     * @param taskExecutor         task executor for async processing
     * @param utils                controller utils
     * @param contextFactory       context factory
     * @param workflowService      workflow service
     * @param formsService         forms service
     * @param configurationService configuration service
     * @param securityService      security service
     */
    @Autowired
    public SessionController(final DefinitionService scenarioService, final SessionService sessionService,
                             final CallbackService callbackService, final ValueHelpClient valueHelpClient,
                             final TaskExecutor taskExecutor, final ControllerUtils utils,
                             final ContextFactory contextFactory, final WorkflowService workflowService,
                             final FormsService formsService, final ConfigurationService configurationService, final SecurityService securityService) {
        this.definitionService = scenarioService;
        this.sessionService = sessionService;
        this.callbackService = callbackService;
        this.valueHelpClient = valueHelpClient;
        this.taskExecutor = taskExecutor;
        this.utils = utils;
        this.contextFactory = contextFactory;
        this.workflowService = workflowService;
        this.formsService = formsService;
        this.configurationService = configurationService;
        this.securityService = securityService;
    }

    /**
     * Main entry point for event processing. The client sends the session-id, the
     * command (event) and the source element (row and key) together with the
     * journal information (frontend changes).
     *
     * @param token security token of user
     * @param node  json request from client
     * @return json response for client
     * @throws Exception in case of errors
     */
    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> patchSession(AbstractAuthenticationToken token, @RequestBody JsonNode node)
            throws Exception {

        log.info("SessionController.patchSession: started for session ({})", node.get("id").asText());

        final String sessionId = node.get("id").asText();
        final String command = node.get("command").asText();
        final String sourceRowId = node.get("srcRow").asText();
        final String sourceKey = node.get("srcKey").asText();

        if (StringUtils.isBlank(sessionId)) {
            throw new BadRequestException("Missing session-id");
        }
        if (StringUtils.isBlank(command)) {
            throw new BadRequestException("Missing command");
        }
        if (StringUtils.isBlank(sourceRowId)) {
            throw new BadRequestException("Missing source-row-id");
        }
        if (StringUtils.isBlank(sourceKey)) {
            throw new BadRequestException("Missing source-key");
        }

        // load session from store
        final var session = sessionService.findById(sessionId);
        if (StringUtils.isNotBlank(session.getTaskInstanceId())) {
            securityService.ensureAuthorized(token, FormsRoles.ParticipateProcess);
            String principalPropagationDestinationName = configurationService.getWorkflowRuntimePrincipalPropagationDestinationName();
            if (StringUtils.isBlank(principalPropagationDestinationName)) {
                throw new NotFoundException("No destination name available to read workflow task!");
            }
            if (!workflowService.isTaskExecutable(principalPropagationDestinationName, session.getTaskInstanceId(), utils.getSimplifiedPrincipalName(token.getName()), false)) {
                throw new NotFoundException("Task " + session.getTaskInstanceId() + " is no longer executable");
            }
        } else {
            // non task execution cases
            securityService.ensureAuthorized(token, FormsRoles.StartProcess);
        }
        var context = contextFactory.createContext(token, null, null, null, null,
                sourceRowId, sourceKey, session.getTaskInstanceId());
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, context, null);

        // read scenario-definition
        final var sd = session.getForm().getSd();

        // parse journal info, necessary because we need to load the session before
        final var frontendJournal = FrontendJournal.initJson(sd, node.get(FormUtils.NM_JOURNAL));
        // apply frontend changes from journal
        session.getForm().apply(frontendJournal, session.getJournal());

        // Creating "real" context
        context = contextFactory.createContext(token, sd, session, context.getDisplayState(), context.getLocale(),
                sourceRowId, sourceKey, context.getTaskInstanceId());

        // execute event handlers
        var optEvent = EventType.valueByKey(command);
        if (optEvent.isPresent()) {
            result = callbackService.callEvent(session, sourceRowId, sourceKey, optEvent.get(), context, result);
        } else {
            throw new BadRequestException("Unknown command '" + command + "'");
        }

        // calculate visual settings like visible, editable and required
        FormUtils.calculateVisualAttributes(sd, session.getForm(), true, false, context);

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        // save the session to session store
        final var wg = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            sessionService.save(session);
            wg.countDown();
        });

        final var response = new SessionResponse(session.getId(), result, session.getForm(), session.getJournal());
        var jsonResponse = utils.createSessionResult(response);

        // wait until session is stored...
        wg.await();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache())
                .body(jsonResponse.toByteArray());
    }

    /**
     * Create a new session for a scenario definition. The client can provide a
     * state (e.g. to continue an existing process) and the locale to be used.
     * <p>
     * If no state is provided, the active scenario definition will be used to
     * create a new form.
     *
     * @param token     security token of user
     * @param principal user principal
     * @param request   json request from client
     * @return json response for client
     * @throws Exception in case of errors
     */
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    ResponseEntity<byte[]> createSession(AbstractAuthenticationToken token, Principal principal,
                                         @RequestBody CreateSessionRequest request) throws Exception {
        log.info("Create-session called with {} for user {}", request.toString(),
                principal == null ? "Anonymous" : principal.getName());

        // reading input parameters and define context
        String state = request.getState();
        String formsId = request.getFormsId();
        String taskInstanceId = request.getTask();

        final var isTaskContext = StringUtils.isNotBlank(taskInstanceId);
        final var isShowContext = StringUtils.isNotBlank(formsId);
        final var isInitContext = !isTaskContext && !isShowContext;

        // checks to ensure only one of taskInstanceId or formsId is set
        if (StringUtils.isBlank(state) && StringUtils.isBlank(taskInstanceId) && StringUtils.isBlank(formsId)) {
            throw new BadRequestException("Missing state, task-id or forms-id");
        }
        if (StringUtils.isBlank(request.getLocale())) {
            throw new BadRequestException("Illegal or missing locale");
        }
        var locale = new Locale(request.getLocale());

        // define form
        Form form = null;
        if (isTaskContext) {
            securityService.ensureAuthorized(token, FormsRoles.ParticipateProcess);
            // handle loading the forms information from workflow engine (in detail from task input data) and
            // continue with loading the necessary information (e.g. according scenario definition version)
            String principalPropagationDestinationName = configurationService.getWorkflowRuntimePrincipalPropagationDestinationName();
            if (StringUtils.isBlank(principalPropagationDestinationName)) {
                throw new NotFoundException("No destination name available to read workflow task!");
            }
            if (!workflowService.isTaskExecutable(principalPropagationDestinationName, taskInstanceId,
                    utils.getSimplifiedPrincipalName(principal.getName()), true)) {
                throw new NotFoundException("Task " + taskInstanceId + " is no longer executable");
            }
            TaskInputContext taskInputContext = workflowService.findFormByTask(principalPropagationDestinationName,
                    taskInstanceId);
            if (null == taskInputContext) {
                throw new NotFoundException("No form information found for task " + taskInstanceId + "!");
            }
            state = taskInputContext.getFormsProcessState();
            // continue to load the form via PersistenceService ...
            form = formsService.loadById(taskInputContext.getFormsProcessID());
        }
        if (isShowContext) {
            securityService.ensureAnyAuthorized(token, FormsRoles.StartProcess, FormsRoles.ParticipateProcess, FormsRoles.SeeAfterStart);
            form = formsService.loadById(formsId);
        }
        if (isInitContext) {
            securityService.ensureAuthorized(token, FormsRoles.StartProcess);
        }

        // Callback at begin of round-trip
        final var preContext = contextFactory.createContext(token, null, null, state,
                locale, null, null, taskInstanceId);
        var result = callbackService.callLifecycleHook(LifecycleHookType.StartRoundtrip, preContext, null);

        // Collect and define variables for context
        Optional<ScenarioDefinition> sdOpt = null;
        if (isTaskContext || isShowContext) {
            sdOpt = definitionService.findDefinitionByVersion(form.getScenarioVersion());
        } else {
            // default handling is to create a new session for blank form -> find active scenario definition
            sdOpt = definitionService.findActiveDefinition();
        }
        if (sdOpt.isEmpty()) {
            throw new BadRequestException("No scenario definition defined");
        }
        final var sd = sdOpt.get();

        final CompletableFuture<Map<String, Long>> vhVersions = new CompletableFuture<>();
        taskExecutor.execute(() -> {
            vhVersions.complete(valueHelpClient.findValuesVersion(sd.getValueHelpIds(), preContext.getLocale()));
        });

        // create session (and form), this will also set the access-class into the
        // context!
        var session = sessionService.create(sd, form, preContext);
        var context = contextFactory.createContext(token, sd, session, preContext.getDisplayState(),
                preContext.getLocale(), null, null, preContext.getTaskInstanceId());

        if (isTaskContext) {
            session.setTaskInstanceId(taskInstanceId);
        }

        // calculate visual settings like visible, editable and required. This time with initialisation
        FormUtils.calculateVisualAttributes(sd, session.getForm(), true, true, context);

        // TODO (OB) differ between FormCreated in case of new Form and FormLoaded between TaskExecution
        // create the context for following callbacks
        if (!isTaskContext) {
            result = callbackService.callLifecycleHook(LifecycleHookType.FormCreated, context, result);
        } else {
            result = callbackService.callLifecycleHook(LifecycleHookType.FormLoaded, context, result);
        }

        // calculate visual settings like visible, editable and required. This time without initialisation. So
        // values set in hook will stay the same (this call just re-calculates visual-dependent values)
        FormUtils.calculateVisualAttributes(sd, session.getForm(), true, false, context);

        // callback at end of round-trip
        result = callbackService.callLifecycleHook(LifecycleHookType.EndRoundtrip, context, result);

        // save the session to session store
        final var wg = new CountDownLatch(1);
        taskExecutor.execute(() -> {
            sessionService.save(session);
            wg.countDown();
        });

        // create response and sent it to client
        final var response = new SessionResponse(session.getId(), result, form, null);
        response.setDef(context.getScenarioDefinition());
        response.setValues(session.getForm().getElements());
        response.setLocale(context.getLocale());
        response.setCallbackService(this.callbackService);
        response.setValueHelps(vhVersions.get());
        var jsonResponse = utils.createSessionResult(response);

        // wait until session is stored...
        wg.await();
        log.info("Session created and stored. returning call...");

        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.noCache())
                .body(jsonResponse.toByteArray());
    }

    /**
     * Request class for creating a new session.
     */
    @Data
    static class CreateSessionRequest {
        private String state;
        private String task;
        private String locale;
        private String formsId;
    }

}

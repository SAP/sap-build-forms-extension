package com.sap.bfx.callback;

import com.sap.bfx.definition.DefinitionService;
import com.sap.bfx.definition.ElementDefinition;
import com.sap.bfx.definition.EventType;
import com.sap.bfx.definition.Message;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.session.*;
import com.sap.bfx.standard.StandardTableComparator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class CallbackService {

    private final Map<Integer, Map<String, EventHandlerInfo>> eventHandlerMap = new HashMap<>();
    private final Map<Integer, Map<LifecycleHookType, Collection<LifecycleHook>>> hookMap = new HashMap<>();

    @Autowired
    private DefinitionService definitionService;

    /**
     * Calls a lifecycle hook for the specified type and context.
     * Executes the lifecycle hook in three phases: before, on, and after.
     * If validation is required, it is performed after the lifecycle hook execution.
     *
     * @param type     the type of the lifecycle hook to call
     * @param ctx      the context in which the lifecycle hook is executed
     * @param previous the previous `CallbackResult` to carry forward, or null
     * @return the resulting `CallbackResult` after executing the lifecycle hook
     */
    @SuppressWarnings("rawtypes")
    public CallbackResult callLifecycleHook(final LifecycleHookType type,
                                            final Context<? extends AccessClass> ctx, CallbackResult previous) {

        final var optDefinition = definitionService.findActiveDefinition();
        if (optDefinition.isEmpty()) {
            throw new FormsCoreException("cannot find active scenario definition, please check your configuration!");
        }
        final var version = ctx.getScenarioDefinition() != null
                ? ctx.getScenarioDefinition().getVersion()
                : optDefinition.get().getVersion();

        var result = (previous == null) ? new CallbackResult() : previous;
        var hm = hookMap.get(version);
        if (hm == null) {
            return result;
        }
        var hl = hm.get(type);
        if (hl == null) {
            return result;
        }

        try {
            // execute all before calls
            for (var it : hl) {
                var r = it.before(ctx, result);
                if (r != null) {
                    result = r;
                    if (result.isStopProcessing()) {
                        break;
                    }
                }
            }
            // execute all on calls, results are forwarded through iteration
            if (!result.isStopProcessing()) {
                for (var it : hl) {
                    var r = it.on(ctx, result);
                    if (r != null) {
                        result = r;
                        if (result.isStopProcessing()) {
                            break;
                        }
                    }
                }
            }
            // execute all after calls
            if (!result.isStopProcessing()) {
                for (var it : hl) {
                    var r = it.before(ctx, result);
                    if (r != null) {
                        result = r;
                        if (result.isStopProcessing()) {
                            break;
                        }
                    }
                }
            }

            // if re-validation is required, then execute it
            if (result.isValidate()) {
                validate(ctx);
            }
        } catch (NotAuthorizedException e) {
            log.error("Not authorized to execute callback for app='{}', roles='{}' by user='{}'",
                    e.getAppName(), e.getRoles(), e.getUser());
            throw ExceptionUtils.from(e);
        } catch (Throwable t) {
            throw ExceptionUtils.from("Error during lifecycle-hook-call for type '" + type + "'", t);
        }

        return result;
    }

    /**
     * Calls the event handler for the given sourceRowId and sourceKey.
     *
     * @param session     the current session
     * @param sourceRowId the row id of the source element
     * @param sourceKey   the key of the source element
     * @param type        the type of the event
     * @param ctx         the context of the event
     * @param previous    previous result, can be null
     * @return CallbackResult with results of all handlers
     */
    public CallbackResult callEvent(final Session session, final String sourceRowId, final String sourceKey,
                                    final EventType type, final Context<? extends AccessClass> ctx,
                                    CallbackResult previous) {

        log.debug("CallbackService.callEvent: started for {}", sourceKey);

        final var version = ctx.getScenarioDefinition() != null
                ? ctx.getScenarioDefinition().getVersion()
                : VersionSelector.IGNORE;

        var result = (previous == null) ? new CallbackResult() : previous;
        var ehm = eventHandlerMap.get(version);
        if (ehm != null && ehm.containsKey(sourceKey)) {
            try {
                final var info = ehm.get(sourceKey);

                log.debug("CallbackService.callEvent: event-info: {}", info.toString());
                // if validation is required then execute validation
                if (info.isValidating()) {
                    validate(ctx);
                }

                // execute all before handlers
                for (var it : info.handlers) {
                    if (it.match(sourceKey, type, ctx.getScenarioDefinition().getVersion())) {
                        var r = it.before(ctx, result);
                        if (r != null) {
                            result = r;
                            if (result.isStopProcessing()) {
                                break;
                            }
                        }
                    }
                }
                // execute all on handlers, results are forwarded through iteration
                if (!result.isStopProcessing()) {
                    for (var it : info.handlers) {
                        if (it.match(sourceKey, type, ctx.getScenarioDefinition().getVersion())) {
                            var r = it.on(ctx, result);
                            if (r != null) {
                                result = r;
                                if (result.isStopProcessing()) {
                                    break;
                                }
                            }
                        }
                    }
                }
                // execute all after handlers
                if (!result.isStopProcessing()) {
                    for (var it : info.handlers) {
                        if (it.match(sourceKey, type, ctx.getScenarioDefinition().getVersion())) {
                            var r = it.after(ctx, result);
                            if (r != null) {
                                result = r;
                                if (result.isStopProcessing()) {
                                    break;
                                }
                            }
                        }
                    }
                }
                // if re-validation is required, then execute it
                if (result.isValidate()) {
                    validate(ctx);
                }
            } catch (Throwable t) {
                throw ExceptionUtils.fromCallback(t, type.name());
            }
        } else {
            // if not processed and browse event and table then we execute a default event
            if (EventType.Sort.equals(type)) {
                final var element = FormUtils.findElementByRowAndKey(session.getForm(), sourceRowId, sourceKey);
                Collections.sort(((Table) element.getValue()).getRows(),
                        new StandardTableComparator(((Table) element.getValue())));
                session.getJournal().addUpdated(sourceRowId, element, ChangePropertyType.Value, element.getValue());
            } else if (EventType.Browse.equals(type) || EventType.Delete.equals(type)) {
                final var element = FormUtils.findElementByRowAndKey(session.getForm(), sourceRowId, sourceKey);
                session.getJournal().addUpdated(sourceRowId, element, ChangePropertyType.Value, element.getValue());
            } else {
                throw new FormsCoreException(String.format("No handlers found for '%s' of '%s' in version '%d'",
                        type, sourceKey, version));
            }
        }

        // if a post event processing validation is set, then execute it now
        if (result.isValidate()) {
            validate(ctx);
        }

        return result;
    }

    /**
     * Searches for all lifecycle hooks and event handlers in the application context
     * and registers them for the corresponding versions.
     *
     * @param event the ContextRefreshedEvent that contains the application context
     */
    public void searchAndRegisterCallbacks(final ContextRefreshedEvent event) {
        final var appContext = event.getApplicationContext();

        final var scenarioService = appContext.getBean(DefinitionService.class);
        final Collection<Integer> versions = scenarioService.getVersions();

        final var hooks = appContext.getBeansOfType(LifecycleHook.class);
        log.info("there are {} hook candidates.", hooks.size());
        if (!hooks.isEmpty()) {
            log.info("found lifecycle hooks:");
            hooks.values().stream().sorted((a, b) -> a.order().getOrder() - b.order().getOrder()).forEach(it -> {
                for (var version : versions) {
                    if (it.match(version)) {
                        var hm = hookMap.computeIfAbsent(version, k -> new HashMap<>());
                        var hl = hm.computeIfAbsent(it.getType(), k -> new ArrayList<>());
                        hl.add(it);
                        log.info("  Lifecycle '{}' implemented in {}", it.getType(), it.getClass().getName());
                    }
                }
            });
        } else {
            log.warn("No lifecycle hooks defined!");
        }

        final var handlers = appContext.getBeansOfType(EventHandler.class);
        log.info("there are {} handler candidates.", handlers.size());
        if (!handlers.isEmpty()) {
            log.info("found event handlers:");
            eventHandlerMap.clear();
            handlers.values().stream().sorted((a, b) -> a.order().getOrder() - b.order().getOrder()).forEach(it -> {
                log.debug("  Event handler candidate: " + it.getClass().getName() + " for key=" + it.getKey());
                for (var version : versions) {
                    final var sd = scenarioService.findDefinitionByVersion(version).get();
                    final var ed = sd.findElementByKey(it.getKey());
                    if ((ed != null && it.match(ed.getKey(), it.getType(), version)) || it.getType().equals(EventType.TriggerEvent)) {
                        // this event handler matches for the given version. Storing it in the
                        // event-handler-map
                        var ehm = eventHandlerMap.computeIfAbsent(version, k -> new HashMap<>());
                        var eventInfo = ehm.computeIfAbsent(it.getKey(), k -> new EventHandlerInfo(version, ed));
                        eventInfo.add(it);
                        log.info("  Event '{}' for '{}' in version '{}' added -> '{}'",
                                it.getType(), it.getKey(), version, it.getClass().getName());
                    }
                }
            });
        } else {
            log.warn("No Event handlers defined!");
        }
    }

    /**
     * Finds all event handlers for the given version.
     *
     * @param version the version to search for event handlers
     * @return a map of event handler key to EventHandlerInfo for the specified version
     */
    public Map<String, EventHandlerInfo> findEventHandlersByVersion(final Integer version) {
        return eventHandlerMap.get(version);
    }

    /**
     * Validates the data in the context by checking required fields and validation rules.
     * If a field is required but empty, or if a validation rule fails, an error message is set.
     *
     * @param ctx the context containing the data to validate
     */
    private void validate(final Context<? extends AccessClass> ctx) {
        final var api = ctx.getDataApi();

        api.forEach((ed, rowId, context) -> {
            Optional<Message> optMessage = Optional.empty();

            // first check all required
            if (api.isRequired(rowId, ed.getKey())) {
                // log.debug("CallbackService.validate: is-required for: " + ed.getName());
                final var dt = ElementDefinition.getDataTypeClass(ed);
                if (dt == String.class) {
                    if (StringUtils.isBlank((String) api.getValue(rowId, ed.getKey()))) {
                        optMessage = Optional.of(Message.REQUIRED_ERROR);
                    }
                } else if (dt == Table.class) {
                    final var rows = ((Table) api.getValue(rowId, ed.getKey())).getRows();
                    if (rows.isEmpty()) {
                        optMessage = Optional.of(Message.REQUIRED_ERROR);
                    }
                } else if (dt == Attachment.class) {
                    // TODO(ML) Write Attachement infos
                } else {
                    if (api.getOptVal(rowId, ed.getKey()).isEmpty()) {
                        optMessage = Optional.of(Message.REQUIRED_ERROR);
                    }
                }
            }

            // second, execute validation rules...
            if (optMessage.isEmpty()) {
                for (var rule : ed.getValidationRules()) {
                    // log.debug("for {} found rule {}", ed.getName(), rule.getClass().getName());
                    optMessage = rule.validate(rowId, ed.getKey(), context);
                    if (optMessage.isPresent()) {
                        break;
                    }
                }
            }

            if (optMessage.isEmpty()) {
                api.setMessage(rowId, ed.getKey(), null);
            } else {
                // log.debug("CallbackService.validate: -- validation result set");
                api.setMessage(rowId, ed.getKey(), optMessage.get());
            }

            return true;
        }, ctx);
    }

    /**
     * Information about an event handler for a specific element definition.
     */
    @Data
    public static class EventHandlerInfo {
        private Integer version;
        private ElementDefinition ed;
        private boolean validating;
        private Collection<EventHandler> handlers;

        /**
         * Constructor for EventHandlerInfo.
         *
         * @param version the version of the event handler
         * @param ed      the element definition associated with the event handler
         */
        EventHandlerInfo(final Integer version, final ElementDefinition ed) {
            this.version = version;
            this.ed = ed;
            this.validating = false;
            this.handlers = new ArrayList<EventHandler>();
        }

        /**
         * Adds an event handler to the collection and updates the validating flag if necessary.
         *
         * @param eh the event handler to add
         */
        void add(EventHandler eh) {
            this.validating = this.validating || eh.validating();
            handlers.add(eh);
        }
    }
}

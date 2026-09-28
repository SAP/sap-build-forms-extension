package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.session.ElementPos;
import com.sap.bfx.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Factory to create Context objects.
 */
@Service
public class ContextFactory {

    private final AccessClassFactory acFactory;
    private final ApiFactory apiFactory;

    /**
     * @param acFactory
     * @param apiFactory
     */
    @Autowired
    public ContextFactory(final AccessClassFactory acFactory, final ApiFactory apiFactory) {
        this.acFactory = acFactory;
        this.apiFactory = apiFactory;
    }

    /**
     * Create a new Context object and set all relevant data.
     *
     * @param securitySession the security session
     * @param sd              the scenario definition
     * @param session         the session
     * @param displayState    the state
     * @param locale          the locale
     * @param sourceRowId     the source row ID
     * @param sourceKey       the source key
     * @param taskInstanceId  the task instance ID
     * @param <AC>            the access class type
     * @return the created Context object
     */
    public <AC extends AccessClass> ContextImpl<AC> createContext(final SecuritySession securitySession,
                                                                  final ScenarioDefinition sd, final Session session,
                                                                  final String displayState, final Locale locale,
                                                                  final String sourceRowId, final String sourceKey,
                                                                  final String taskInstanceId) {
        var ctx = new ContextImpl<AC>();

        // set data in context
        ctx.acFactory = this.acFactory;
        ctx.apiFactory = this.apiFactory;
        ctx.user = securitySession.getUser();
        ctx.authObject = securitySession.getTokenValue();
        ctx.source = new ElementPos(sourceRowId, sourceKey);

        if (sd != null) {
            ctx.scenarioDefinition = sd;
            if (session != null) {
                ctx.session = session;
                ctx.displayState = session.getDisplayState();
                ctx.locale = session.getLocale();
            }
        }
        if (displayState != null) {
            ctx.displayState = displayState;
        }
        if (locale != null) {
            ctx.locale = locale;
        }
        if (taskInstanceId != null) {
            ctx.taskInstanceId = taskInstanceId;
        }

        return ctx;
    }
}

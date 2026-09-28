package com.sap.bfx.session;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.AccessClassFactory;
import com.sap.bfx.callback.ContextImpl;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.PersonalizationService;
import com.sap.bfx.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Session> redis;
    private final AccessClassFactory acFactory;
    private final PersonalizationService personalizationService;

    /**
     * Constructor-based dependency injection for RedisTemplate, AccessClassFactory, and PersonalizationService.
     */
    @Autowired
    public SessionService(final AccessClassFactory acFactory,
                          @Qualifier("session-redis-template") final RedisTemplate<String, Session> redis,
                          PersonalizationService personalizationService) {

        this.acFactory = acFactory;
        this.redis = redis;
        this.personalizationService = personalizationService;
    }

    /**
     * Creates a new Session object based on the provided ScenarioDefinition, Form, and ContextImpl.
     *
     * @param sd   the ScenarioDefinition used to create the session
     * @param form the Form associated with the session; if null, a new form will be created
     * @param ctx  the ContextImpl providing locale and display state information
     * @return a newly created Session object
     */
    public Session create(final ScenarioDefinition sd, final Form form, final ContextImpl<? extends AccessClass> ctx) {

        var session = new Session();

        session.setId(UUID.randomUUID().toString());
        session.setLocale(ctx.getLocale());
        session.setDisplayState(ctx.getDisplayState());

        String application;
        try {
            application = sd.getBasePackage() + "." + sd.getAccessObjectName();
            log.info("Application: " + application);
        } catch (Exception e) {
            log.error("Application not readable");
            application = "_";
        }

        final var username = SecurityUtils.getUserName();
        session.setUserName(username);
        session.setSettings(personalizationService.getSettings(application, username));

        session.setJournal(new BackendJournal(sd));
        if (form == null) {
            session.setForm(FormUtils.create(sd, session.getJournal(), acFactory, ctx));
        } else {
            form.setJournal(session.getJournal());
            session.setForm(form);
        }
        session.getForm().setSd(sd);

        return session;
    }

    /**
     * Saves the provided Session object to Redis.
     *
     * @param session the Session object to be saved
     */
    public void save(Session session) {
        redis.boundValueOps(session.getId()).set(session);
    }

    /**
     * Retrieves a Session object from Redis based on the provided session ID.
     *
     * @param id the ID of the session to be retrieved
     * @return the Session object associated with the given ID
     */
    public Session findById(String id) {
        final var session = redis.boundValueOps(id).get();
        final var sd = session.getForm().getSd();
        final var journal = new BackendJournal(sd);
        // initialise the initial form rows for some optimaisations when sending the result
        journal.copyInitialTableRowsRoot(session.getForm());
        // setting the journal to session and form
        session.setJournal(journal);
        session.getForm().init(sd, journal);

        return session;
    }
}

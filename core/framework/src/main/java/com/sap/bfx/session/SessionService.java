package com.sap.bfx.session;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.AccessClassFactory;
import com.sap.bfx.callback.ContextImpl;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.PersonalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class SessionService {

    private final RedisTemplate<String, Session> redis;
    private final AccessClassFactory acFactory;
    private final PersonalizationService personalizationService;

    /**
     * @param scenarioService
     * @param acFactory
     * @param redis
     * @param personalizationService
     */
    @Autowired
    public SessionService(final AccessClassFactory acFactory,
                          final RedisTemplate<String, Session> redis, PersonalizationService personalizationService) {

        this.acFactory = acFactory;
        this.redis = redis;
        this.personalizationService = personalizationService;
    }

    /**
     * @param sd
     * @param ctx
     * @return
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

        final var username = SecurityContextHolder.getContext().getAuthentication().getName();
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
     * @param session
     */
    public void save(Session session) {
        redis.boundValueOps(session.getId()).set(session);
    }

    /**
     * @param id
     * @return
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

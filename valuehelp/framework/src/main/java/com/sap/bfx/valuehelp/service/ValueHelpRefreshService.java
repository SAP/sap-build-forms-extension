package com.sap.bfx.valuehelp.service;

import com.sap.bfx.callback.AdapterDescriptor;
import com.sap.bfx.valuehelp.adapter.ValueHelpAdapter;
import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.support.locks.ExpirableLockRegistry;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;

@Service
@Slf4j
public class ValueHelpRefreshService {

    private final Map<String, ValueHelpAdapter> adapterMap = new HashMap<>();

    @Autowired
    public ValueHelpRefreshService(final ValueHelpService service, final ExpirableLockRegistry redisLockRegistry) {
        this.service = service;
        this.redisLockRegistry = redisLockRegistry;
    }

    private final ValueHelpService service;
    private final ExpirableLockRegistry redisLockRegistry;

    public void task() {
        log.info("Execute task at: " + LocalDateTime.now());

        final String TASK_NAME = "VALUEHELP_REFRESH";
        Lock lock = redisLockRegistry.obtain(TASK_NAME);
        boolean acquired = false;

        try {
            acquired = lock.tryLock();
            if(acquired) {
                log.info("Lock " + TASK_NAME + " acquired");
                Collection<ValueHelpDef> defs = service.findAllDefs(null, null);
                for (ValueHelpDef def : defs) {
                    if (def.getTtl() != ValueHelpDef.TTL_STATIC && !def.getAdapter().equals("local")) {
                        if(def.getLanguages().size() > 0) {
                            for (String language : def.getLanguages()) {
                                createOrUpdateValueHelp(def, language);
                            }
                        } else {
                            createOrUpdateValueHelp(def, "_");
                        }
                    }
                }
            } else {
                log.info("Lock " + TASK_NAME + " could not be acquired");
            }
        } catch (Exception e){
            log.error("Error while refreshing valuehelps");
        } finally {
            if(acquired) {
                lock.unlock();
                log.info("Lock " + TASK_NAME + " released");
            }
        }
    }

    private void createOrUpdateValueHelp(ValueHelpDef def, String locale) {
        Optional<ValueHelp> vh = service.findValueLatestVersionByIdLocale(def.getId(), locale);
        if (vh.isPresent()) {
            ValueHelp valueHelp = vh.get();
            if (valueHelp.getValidUntil().before(new Timestamp(System.currentTimeMillis()))) {
                log.info("Update valueHelp " + def.getId() + " " + locale);
                var adapter = adapterMap.get(def.getAdapter());
                if (adapter != null) {
                    ValueHelp newValueHelp = adapter.query(def, valueHelp.getLocale());
                    if (newValueHelp != null) {
                        service.updateValue(valueHelp);
                    } else {
                        log.debug("Value Help " + def.getId() + " with locale " + locale + " could not be updated.");
                    }
                }
            }
        } else {
            log.info("Create valueHelp " + def.getId() + " " + locale);
            var adapter = adapterMap.get(def.getAdapter());
            if (adapter != null) {
                ValueHelp newValueHelp = adapter.query(def, new Locale(locale));
                if (newValueHelp != null) {
                    service.addValue(newValueHelp);
                } else {
                    log.debug("Value Help " + def.getId() + " with locale " + locale + " could not be created.");
                }
            }


        }
    }

    /**
     * @param ctx
     */
    public void initValueHelpAdapters(final ApplicationContext ctx) {
        adapterMap.clear();

        final var adapter = ctx.getBeansOfType(ValueHelpAdapter.class);
        adapter.values().forEach(it -> {
            var descriptor = it.getClass().getAnnotation(AdapterDescriptor.class);
            if (descriptor == null) {
                log.error("ValueHelpAdapter '" + it.getClass().getName()
                        + "' has not annotation of type ValueHelpAdapterDescriptor");
            } else {
                adapterMap.put(descriptor.value(), it);
                log.info("ValueHelpAdapter '" + it.getClass().getName() + "' added with name '"
                        + descriptor.value() + "'.");
            }
        });
    }
}

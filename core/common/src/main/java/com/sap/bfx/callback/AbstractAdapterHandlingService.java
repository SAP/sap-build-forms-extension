package com.sap.bfx.callback;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract service to handle adapters.
 *
 * @param <A>
 */
@Slf4j
public abstract class AbstractAdapterHandlingService<A extends Adapter> {

    private final ApplicationContext applicationContext;
    private final Map<String, A> adapterMap = new HashMap<>();
    private final Class<A> adapterClz;

    /**
     * Constructor
     *
     * @param applicationContext
     * @param adapterClz
     */
    protected AbstractAdapterHandlingService(final ApplicationContext applicationContext, final Class<A> adapterClz) {
        this.applicationContext = applicationContext;
        this.adapterClz = adapterClz;
    }

    /**
     * Get adapter by name.
     *
     * @param name
     * @return adapter
     */
    protected A getAdapter(String name) {
        if (name == null) {
            name = Adapter.DEFAULT;
        }

        if (!adapterMap.containsKey(name)) {
            this.scanForAdapters();
        }
        return adapterMap.get(name);
    }

    /**
     * Get all adapters.
     *
     * @return collection of adapters
     */
    protected Collection<A> getAllAdapters() {
        final var result = new ArrayList<A>();

        if (adapterMap.isEmpty()) {
            this.scanForAdapters();
        }
        adapterMap.keySet().stream()
                .filter(key -> !Strings.CS.equals(key, Adapter.DEFAULT))
                .forEach(key -> result.add(adapterMap.get(key)));
        
        return result;
    }

    /**
     * Scan for adapters and populate the adapter map.
     */
    private void scanForAdapters() {
        final var adapters = applicationContext.getBeansOfType(adapterClz);
        adapters.values().forEach(it -> {
            final var descriptor = it.getClass().getAnnotation(AdapterDescriptor.class);
            if (descriptor == null) {
                log.error("Adapter '" + it.getClass().getName() + "' has not the according annotation");
            } else {
                adapterMap.put(descriptor.value(), it);
                log.info("Adapter '" + it.getClass().getName() + "' added with name '" + descriptor.value() + "'.");
            }
        });
        if (adapters.size() == 1) {
            adapterMap.put(Adapter.DEFAULT, adapters.values().stream().findFirst().get());
        }
    }
}

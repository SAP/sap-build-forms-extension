package com.sap.bfx.definition;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.utils.PropertyFileUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.*;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ScenarioDefinition extends AbstractStructureDefinition {
    private boolean active;
    private String rootElementName;
    private String rootElementKey;
    private Locale defaultLocale;
    private Map<String, ElementDefinition> elementsMap = new HashMap<>();
    private Set<String> valueHelpIds = new HashSet<>();

    /**
     * @param key
     * @return
     */
    public ElementDefinition findElementByKey(final String key) {
        return elementsMap.get(key);
    }

    /**
     * @param appContext
     */
    public void postLoad(final ApplicationContext appContext) {
        elementsMap.clear();

        // populate the elementsMap
        getElements().forEach(it -> ElementDefinition.postLoad(this, it, appContext));

        // add default keys/texts to the texts
        log.info("Searching for default texts");
        final var resolver = new PathMatchingResourcePatternResolver();
        try {
            final var pattern = Pattern.compile("^.*_([a-zA-Z_]+)\\.properties");

            for (var resource : resolver.getResources("classpath*:forms-default-texts/**/*.*")) {
                var matcher = pattern.matcher(resource.getFilename());
                if (matcher.find()) {
                    log.info("  found default texts '{}' for language '{}'", matcher.group(), matcher.group(1));
                    final var locale = Locale.of(matcher.group(1));
                    var textMap = getTexts().get(locale);
//                    texts.keySet().forEach(it -> log.info("Available locale {}, hashKeys {} == {}, equals {}",
//                            it.toString(),
//                            it.hashCode(),
//                            locale.hashCode(),
//                            it.equals(locale)));
                    if (textMap == null) {
                        log.warn("  Locale {} not found in texts ({}), creating a new entry!", locale, getTexts().size());
                        textMap = new HashMap<>();
                        getTexts().put(locale, textMap);
                    }
                    PropertyFileUtils.readTexts(resource.getInputStream(), "", textMap);
                }
            }
        } catch (Exception e) {
            throw ExceptionUtils.from("Error reading default texts", e);
        }
    }
}

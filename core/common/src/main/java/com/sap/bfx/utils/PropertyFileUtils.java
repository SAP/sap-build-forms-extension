package com.sap.bfx.utils;

import com.sap.bfx.definition.DefinitionNames;
import com.sap.bfx.definition.ElementDefinition;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.exception.ExceptionUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * Utility class for handling property files
 */
public final class PropertyFileUtils {

    /**
     * Avoid external construction of objects
     */
    private PropertyFileUtils() {
    }

    /**
     * Read texts from property file
     *
     * @param fName  file name
     * @param prefix prefix for keys
     * @param texts  map to fill
     */
    public static void readTexts(final String fName, final String prefix, final Map<String, String> texts) {
        try {
            readTexts(new FileInputStream(fName), prefix, texts);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Read texts from property file
     *
     * @param is     input stream
     * @param prefix prefix for keys
     * @param texts  map to fill
     */
    public static void readTexts(final InputStream is, final String prefix, final Map<String, String> texts) {
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            for (; ; ) {
                final var line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                var parts = line.split("=");
                if (parts.length == 2) {
                    texts.put(IdentifierUtils.toPascalCase(prefix + StringUtils.trim(parts[0])),
                            StringUtils.trim(parts[1]));
                } else if (parts.length == 1) {
                    texts.put(IdentifierUtils.toPascalCase(prefix + StringUtils.trim(parts[0])), "");
                }
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Write texts for all elements of a scenario definition to a property file
     *
     * @param fName        file name
     * @param sd           scenario definition
     * @param texts        map with texts
     * @param defaultTexts map with default texts
     */
    public static void writeScenarioTexts(final String fName, final ScenarioDefinition sd,
                                          final Map<String, String> texts, final Map<String, String> defaultTexts) {
        try (var writer = new BufferedWriter(new FileWriter(fName, StandardCharsets.UTF_8))) {
            writeElementsTexts(writer, sd.getElements(), texts, defaultTexts);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Write texts for a collection of element definitions to a property file
     *
     * @param writer       writer to use
     * @param elements     collection of element definitions
     * @param texts        map with texts
     * @param defaultTexts map with default texts
     */
    private static void writeElementsTexts(final Writer writer, final Collection<ElementDefinition> elements,
                                           final Map<String, String> texts, final Map<String, String> defaultTexts) {
        for (var ed : elements) {
            writeElementText(writer, ed, texts, defaultTexts);
        }
    }

    /**
     * Write texts for an element definition and all sub element definitions to a property file
     *
     * @param writer       writer to use
     * @param ed           element definition
     * @param texts        map with texts
     * @param defaultTexts map with default texts
     */
    private static void writeElementText(final Writer writer, final ElementDefinition ed,
                                         final Map<String, String> texts, final Map<String, String> defaultTexts) {

        try {
            writer.write(getText(ed.getName() + DefinitionNames.PF_TITLE, texts, defaultTexts) + "\n");
            writer.write(getText(ed.getName() + DefinitionNames.PF_LONG, texts, defaultTexts) + "\n");
            writer.write(getText(ed.getName() + DefinitionNames.PF_DOC, texts, defaultTexts) + "\n");
            writer.write(getText(ed.getName() + DefinitionNames.PF_PLACEHOLDER, texts, defaultTexts) + "\n");

            // ensure that all ElementDefinitions are also handled (direct or if inside a collection)
            for (var pd : PropertyUtils.getPropertyDescriptors(ed.getClass())) {
                final var prop = PropertyUtils.getProperty(ed, pd.getName());
                if (prop instanceof ElementDefinition) {
                    writeElementText(writer, (ElementDefinition) prop, texts, defaultTexts);
                } else if (prop instanceof Collection) {
                    try {
                        writeElementsTexts(writer, (Collection<ElementDefinition>) prop, texts, defaultTexts);
                    } catch (ClassCastException e) {
                        // TODO(ML) Just for testing, later ClassCastException should be ignored!
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Get text for a key from texts or default texts
     *
     * @param key          key to search
     * @param texts        map with texts
     * @param defaultTexts map with default texts
     * @return text found or empty string
     */
    private static String getText(final String key, final Map<String, String> texts,
                                  final Map<String, String> defaultTexts) {
        var text = texts.get(key);
        if (StringUtils.isBlank(text)) {
            text = defaultTexts.get(key);
        }
        if (text == null) {
            text = "";
        }
        return text;
    }
}

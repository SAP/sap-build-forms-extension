package com.sap.bfx.maven.common;

import com.fasterxml.jackson.annotation.JsonValue;
import com.sap.bfx.definition.AbstractStructureDefinition;
import com.sap.bfx.definition.ElementDefinition;
import com.sap.bfx.definition.Severity;
import com.sap.bfx.utils.Identifier;
import com.sap.bfx.utils.IdentifierUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.plugin.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public abstract class AbstractProcessor {

    private final List<ValidationMessage> messages = new LinkedList<>();

    @Autowired
    protected Log log;

    protected void addError(final AbstractStructureDefinition sd, final ElementDefinition ed, final String msg,
                            final ElementPart elementPart) {
        messages.add(new ValidationMessage(Severity.Error, sd, ed, msg, elementPart));
    }

    protected void clearMessages() {
        this.messages.clear();
    }

    protected List<ValidationMessage> getMessages() {
        return this.messages;
    }

    protected void printMessages(final Severity level) {
        // Sort the messages by version and then by element name
        messages.sort((o1, o2) -> {
            if (o1.getDef().getVersion() == o2.getDef().getVersion()) {
                return StringUtils.compare(o1.getElement().getName(), o2.getElement().getName());
            }
            return o1.getDef().getVersion() < o2.getDef().getVersion() ? -1 : 1;
        });

        var version = 0;
        var elementName = "";
        for (var it : messages) {
            if (it.getSeverity() == level) {
                if (it.getDef().getVersion() != version || !Objects.equals(it.getElement().getName(), elementName)) {
                    version = it.getDef().getVersion();
                    elementName = it.getElement().getName();

                    log.info("  On version '" + version + "' of element '"
                            + elementName + "' the following messages ocuured:");
                }

                var valueMap = new HashMap<String, String>();
                valueMap.put("ed.name", it.getElement().getName());
                valueMap.put("ed.uiElementType", it.getElement().getType().toString());
                valueMap.put("ed.dataType", it.getElement().getDataType().toString());

                switch (level) {
                    case Error:
                        log.error(ansi().fgBrightRed().a(StringSubstitutor.replace("    " +
                                it.getMessage(), valueMap)).reset().toString());
                        break;
                    case Warning:
                        log.warn(ansi().fgBrightYellow().a(StringSubstitutor.replace("    " +
                                it.getMessage(), valueMap)).reset().toString());
                        break;
                    default:
                        log.info(StringSubstitutor.replace("    " + it.getMessage(), valueMap));
                }
            }
        }
    }

    protected void normalizeNameKey(final ElementDefinition ed) {
        final var name = ed.getName();
        ed.setName(IdentifierUtils.capitalCamelCase(name));
        ed.setKey(IdentifierUtils.key(name));
    }

    public enum ElementPart implements Identifier {
        Version("v"),
        Name("n"),
        UiElementType("u"),
        DataType("d"),
        None("_");

        private final String identifier;

        ElementPart(String identifier) {
            this.identifier = identifier;
        }

        @JsonValue
        @Override
        public String getIdentifier() {
            return identifier;
        }
    }

    @Data
    @AllArgsConstructor
    public static class ValidationMessage {
        Severity severity;
        AbstractStructureDefinition def;
        ElementDefinition element;
        String message;
        ElementPart elementPart;
    }
}

package com.sap.bfx.maven.common.services;

import com.sap.bfx.definition.*;
import com.sap.bfx.maven.common.AbstractProcessor;
import com.sap.bfx.maven.common.definition.ExtendedScenarioDefinition;
import com.sap.bfx.utils.IdentifierUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

import static org.fusesource.jansi.Ansi.ansi;

@Service
public class CheckAndProcessService extends AbstractProcessor {

    @Autowired
    MetadataService metadataService;

    /**
     * Checks and processes the scenario definitions. It will execute the checks and processings and return true if
     * no error occured, otherwise false. The messages can be retrieved via getMessages() and contain all
     * errors/warnings/info messages occured during the checks and processings. The messages are also printed in
     * the log with appropriate severity. The checks and processings are executed in the order of the scenario
     * definitions as they are defined in the metadata.
     *
     * @param project
     * @return
     */
    public boolean checkAndProcess(final MavenProject project) {
        checkAndProcessWithMessages(project);

        // process messages
        int errors = 0, warnings = 0, infos = 0;
        for (var it : getMessages()) {
            switch (it.getSeverity()) {
                case Error:
                    errors++;
                    break;
                case Warning:
                    warnings++;
                    break;
                default:
                    infos++;
            }
        }
        if (errors > 0) {
            log.error(ansi().fgBrightRed().bold().a(errors + " validation error(s) occured!").reset().toString());
            this.printMessages(Severity.Error);
        }
        if (warnings > 0) {
            log.warn(ansi().fgBrightYellow().bold().a(warnings + " validation warning(s) occured!").reset().toString());
            this.printMessages(Severity.Warning);
        }
        if (infos > 0) {
            log.info(ansi().bold().a(infos + " validation info(s)/success message(s) occured!").boldOff().toString());
            this.printMessages(Severity.Info);
        }
        if (errors == 0 && warnings == 0) {
            log.info(ansi().fgBrightGreen().bold().a("No errors/warnings occured during checking/processing").reset().toString());
        }

        return (errors == 0);
    }

    /**
     * @param project
     * @return
     */
    public List<ValidationMessage> checkAndProcessWithMessages(final MavenProject project) {
        Set<Integer> versions = new HashSet<>();
        clearMessages();

        // after this, compute the fields and execute checks
        metadataService.getScenarioDefinitions().forEach(it -> this.execute(project, versions, it));

        return this.getMessages();
    }

    /**
     * @param project
     * @param versions
     * @param sd
     */
    private void execute(final MavenProject project, final Set<Integer> versions, final ExtendedScenarioDefinition sd) {
        if (versions.contains(sd.getVersion())) {
            this.addError(sd, null, "Version ${sd.version} is duplicate", ElementPart.Version);
        }

        sd.setRootElementName(IdentifierUtils.capitalCamelCase(sd.getRootElementName()));

        final var processingInfo = new ProcessingInfo(project, sd);

        // compute through all elements and execute checks and computing
        sd.getElements().forEach(it -> this.checkAndProcessElement(processingInfo, it, null));
        sd.getElements().addAll(processingInfo.ed2Add);

        // sort also on root level (form/wizard and dialogs: form/wizard will always be the first, dialogs are ordered
        // by their sort-number
        sd.getElements().sort((a, b) -> {
            if (a.getType() == UIElementType.Form || a.getType() == UIElementType.Wizard) {
                return -1;
            }
            if (b.getType() == UIElementType.Form || b.getType() == UIElementType.Wizard) {
                return 1;
            }
            return Math.abs(a.getSort() - b.getSort());
        });
    }

    /**
     * @param processingInfo
     * @param ed
     * @param parent
     */
    public void checkAndProcessElement(final ProcessingInfo processingInfo, final ElementDefinition ed,
                                       final ElementDefinition parent) {
        // norm name to camel case and compute MD5 hash for key
        normalizeNameKey(ed);
        // also set the key to root element if this one is the root element
        if (processingInfo.getDef() instanceof ScenarioDefinition) {
            if (StringUtils.equals(ed.getName(), ((ScenarioDefinition) processingInfo.getDef()).getRootElementName())) {
                ((ScenarioDefinition) processingInfo.getDef()).setRootElementKey(ed.getKey());
            }
        }

        // Test name of the element
        final var pattern = Pattern.compile("^[a-zA-Z0-9_]+$");
        if (!pattern.matcher(ed.getName()).find()) {
            this.addError(processingInfo.getDef(), ed,
                    "Invalid name '${ed.name}'", ElementPart.Name);
        }
        // Check if name is duplicated
        if (processingInfo.getIds().contains(ed.getName())) {
            this.addError(processingInfo.getDef(), ed,
                    "Duplicate name '${ed.name}'", ElementPart.Name);
        }
        processingInfo.getIds().add(ed.getName());

        // check elements on root level / root-elements not on root level
        if (ed.isRootType() && parent != null) {
            log.info("  Error: root-element not on root: " + ed.getName()
                    + ",(type=" + ed.getType().getIdentifier()
                    + ",class=" + ed.getClass().getName()
                    + ")");
            this.addError(processingInfo.getDef(), ed,
                    "UI-Element '${ed.uiElementType}' is only allowed on root", ElementPart.UiElementType);
        }
        if (!ed.isRootType() && parent == null) {
            log.info("  Error: non-root-element is on root: " + ed.getName()
                    + ",(type=" + ed.getType().getIdentifier()
                    + ",class=" + ed.getClass().getName()
                    + ")");
            this.addError(processingInfo.getDef(), ed,
                    "UI-Element '${ed.uiElementType}' is NOT allowed on root", ElementPart.UiElementType);
        }

        // for most elements the data-type is auto
        if (ed.getType() != UIElementType.Input) {
            ed.setDataType(DataType.Auto);
        } else {
            if (ed.getDataType() != DataType.String && ed.getDataType() != DataType.Date
                    && ed.getDataType() != DataType.DateTime && ed.getDataType() != DataType.Time
                    && ed.getDataType() != DataType.Decimal && ed.getDataType() != DataType.Int) {
                this.addError(processingInfo.getDef(), ed,
                        "Invalid data-type '${ed.dataType}' for UI-Element Input", ElementPart.DataType);
            }
        }

        // check and clear value-help
        if (ed instanceof HasValueHelp) {
            if ((((HasValueHelp) ed).getValueHelp()) == null && ed.getType() != UIElementType.Attachment) {
                this.addError(processingInfo.getDef(), ed,
                        "UI-Element '${ed.uiElementType}' requires valueHelp to be set", ElementPart.UiElementType);
            }
        }

        // Sort elements by sort-oder-id
        ed.getElements().sort(Comparator.comparingInt(ElementDefinition::getSort));

        // process child elements
        ed.getChildren().forEach(it1 -> it1.forEach(it2 -> checkAndProcessElement(processingInfo, it2, ed)));
    }

    /**
     *
     */
    @Data
    static class ProcessingInfo {
        private Set<String> ids = new HashSet<>();
        private AbstractStructureDefinition def;
        private MavenProject project;

        private Collection<ElementDefinition> ed2Add = new ArrayList<>();

        public ProcessingInfo(final MavenProject project, final AbstractStructureDefinition def) {
            this.project = project;
            this.def = def;
        }
    }
}

package com.sap.bfx.maven.common.services;

import com.sap.bfx.definition.*;
import com.sap.bfx.maven.common.AbstractProcessor;
import com.sap.bfx.maven.common.definition.ExtendedScenarioDefinition;
import com.sap.bfx.maven.common.definition.MixinInfo;
import com.sap.bfx.utils.FileUtils;
import com.sap.bfx.utils.IdentifierUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.fusesource.jansi.Ansi.ansi;

@Service
public class MixinService extends AbstractProcessor {

    private final List<CheckAndProcessService.ValidationMessage> messages = new LinkedList<>();

    @Autowired
    MetadataService metadataService;

    @Autowired
    TemplateService templateService;

    @Autowired
    CheckAndProcessService checkAndProcessService;

    public void execute(final MixinServiceType type, final ServiceParameters params) throws Exception {
        // Just to be sure, clear all existing messages
        clearMessages();

        // after this, compute the fields and execute checks
        if (type == MixinServiceType.ScenarioDefinition) {
            metadataService.getScenarioDefinitions().forEach(it -> this.handleScenario(params, it));
        } else {
            handleLibrary(params);
        }

        // process messages
        int errors = 0, warnings = 0, infos = 0;
        for (var it : messages) {
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
            log.info(ansi().fgBrightGreen().bold().a("No errors/warnings occured during checking/processing").reset()
                           .toString());
        }
    }

    /**
     * Handles mixins for a given scenario definition. This includes the following steps:
     * - First, we compute the mixins for all elements. As we allow recursive mixins, we need to repeat this step
     * until no mixin is found anymore.
     * - For each mixin, we load the mixin definition and compute the final list of elements by applying the
     * modifiers specified in the mixin element (e.g. adaptVisible) and evaluating SpEL expressions if specified.
     *
     * @param params ServiceParameters object with context information
     * @param sd     the ScenarioDefinition to be processes
     */
    private void handleScenario(final ServiceParameters params, final ExtendedScenarioDefinition sd) {

        sd.setRootElementName(IdentifierUtils.camelCase(sd.getRootElementName()));

        final var processingInfo = new ProcessingInfo(params, sd);

        // first, go through all elements and compute mixins. As we allow recursive mixins we need to repeat
        // the loop until no mixin is found.
        for (; ; ) {
            var handled = false;
            final var queue = new ConcurrentLinkedQueue<List<ElementDefinition>>();
            queue.add(sd.getElements());

            while (!queue.isEmpty()) {
                var elements = queue.poll();
                for (var i = 0; i < elements.size(); i++) {
                    final var it = elements.get(i);
                    if (it.getElements() != null && !it.getElements().isEmpty()) {
                        queue.add(it.getElements());
                    }
                    try {
                        handled = this.resolveMixin(processingInfo, it, elements) || handled;
                    } catch (Exception e) {
                        log.error(e);
                        addError(processingInfo.getScenarioDefinition(), it, "Error handling mixin: " + e.getMessage(),
                                ElementPart.None);
                    }
                }
            }
            if (!handled) {
                break;
            }
        }
    }

    /**
     *
     * @param params
     * @throws Exception
     */
    private void handleLibrary(final ServiceParameters params) throws Exception {
        // scan for all mixin metadata files
        final var paths = new ArrayList<String>();
        metadataService.scanMetadata(MetadataService.MetadataType.Mixin, params.getMetadataFolder(), paths);

        // now handle each mixin metadata file
        for (var path : paths) {
            // skip non-yaml files for this
            if (!FileUtils.isYamlFile(path)) {
                continue;
            }
            log.info("Found mixin '" + path + "'.");

            // for now, we don't allow mixins within mixins
            // so we "just" need to prepare generation of enums for better field access.

            MixinDefinition mixinDef = null;
            try (var is = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                mixinDef = MixinLoader.readElementDef(is);
            }
            // if no elements are returned, we through an error
            if (mixinDef == null) {
//                log.error("Mixin '" + path + "' does not conatin elements!");
                throw new Exception("error loading mixin '" + path + "'!");
            }
            if (mixinDef.getElements().isEmpty()) {
                log.error("Mixin '" + path + "' does not contain elements!");
            }

            // compute through all elements and execute checks and computing, e.g. calculating the key
            final var processingInfo = new CheckAndProcessService.ProcessingInfo(params.getProject(), mixinDef);
            mixinDef.getElements()
                    .forEach(it -> checkAndProcessService.checkAndProcessElement(processingInfo, it, null));
            mixinDef.getElements().addAll(processingInfo.getEd2Add());

            // create enums
            log.info("Creating enums for mixin '" + path + "'!");
            templateService.createFieldEnumsForMixin(params.getTargetFolder(), mixinDef);
        }
    }

    /**
     *
     * @param processingInfo
     * @param mixin
     * @param parentElements
     * @return
     * @throws Exception
     */
    private boolean resolveMixin(final ProcessingInfo processingInfo, final ElementDefinition mixin,
                                 final List<ElementDefinition> parentElements) throws Exception {
        if (mixin.getType() != UIElementType.Mixin) {
            return false;
        }

        log.debug("handleMixin for element: " + mixin.getName());

        final var mixinDef = MixinLoader.create(processingInfo, (MetaFileElementDefinition) mixin, log).load();
        // if no elements are returned, we through an error
        if (mixinDef == null) {
            throw new Exception("no elements for mixing '" + mixin.getName() + "' found!");
        }

        // Sort the elements to follow the correct order
        mixinDef.getElements().sort(Comparator.comparingInt(ElementDefinition::getSort));

        // Modify it with the modifiers specified in adapt*, rename Ids and calculate keys
        final var visibleExp = ElementDefinition.isExpression(mixin.getVisible()) ?
                new SpelEvaluator<>(mixin.getVisible(), String.class) : null;
        final var editableExp = ElementDefinition.isExpression(mixin.getEditable()) ?
                new SpelEvaluator<>(mixin.getEditable(), String.class) : null;
        final var requiredExp = ElementDefinition.isExpression(mixin.getRequired()) ?
                new SpelEvaluator<>(mixin.getRequired(), String.class) : null;
        log.debug(
                "Mixin: visibleExp is " + (visibleExp == null ? "null" : "contains expression") + " because mixin: '" +
                        mixin.getVisible() + "'.");

        final var queue = new ConcurrentLinkedQueue<List<ElementDefinition>>();
        queue.add(mixinDef.getElements());
        var rootLevel = true;

        while (!queue.isEmpty()) {
            for (var it : queue.poll()) {
                it.setName(mixin.getName() + "_" + it.getName());
                if (it.getElements() != null && !it.getElements().isEmpty()) {
                    queue.add(it.getElements());
                }

                if (rootLevel) {
                    // for root level set the sort of the element to the mixin one because they should
                    // fill in at the place of the mixin
                    it.setSort(mixin.getSort());

                    // Evaluate SpEL expressions if available. Currently, this works for visible, editable and required
                    if (visibleExp != null) {
                        it.setVisible(visibleExp.eval(
                                new EvaluationContext(processingInfo.getScenarioDefinition(), mixin, it)));
                    }
                    if (editableExp != null) {
                        it.setEditable(editableExp.eval(
                                new EvaluationContext(processingInfo.getScenarioDefinition(), mixin, it)));
                    }
                    if (requiredExp != null) {
                        it.setRequired(requiredExp.eval(
                                new EvaluationContext(processingInfo.getScenarioDefinition(), mixin, it)));
                    }
                }
            }
            rootLevel = false;
        }

        // Save the mixin element to a dedicated map that contains all mixins for a given type
        normalizeNameKey(mixin);
        var clsName = metadataService.findAccessClassForElement(processingInfo.getScenarioDefinition().getElements(),
                processingInfo.getScenarioDefinition().getAccessObjectName() +
                        processingInfo.getScenarioDefinition().getVersion(), mixin);
        var l = processingInfo.getScenarioDefinition().getMixins().computeIfAbsent(clsName, k -> new ArrayList<>());
        l.add(new MixinInfo(mixin, mixinDef.getElements()));
//        log.debug("Mixin::resolveMixin: Adding mixin '" + mixin.getName() + "' to Class '" + clsName + "'.");
//        log.debug("Mixin::resolveMixin: Keys: " + processingInfo.getScenarioDefinition().getMixins().keySet());
        // Last step is to insert the list of mixinElements as replacement of mixin element
        var pos = parentElements.indexOf(mixin);
        parentElements.remove(pos);
        parentElements.addAll(pos, mixinDef.getElements());

        // add also the texts
//        log.debug("Mixin::resolveMixin '" + mixin.getName() + "' texts: " + mixinDef.getTexts());
        for (var itLocale : mixinDef.getTexts().keySet()) {
            final var source = mixinDef.getTexts().get(itLocale);
            final var target = processingInfo.getScenarioDefinition().getTexts().get(itLocale);
            for (var it : source.keySet()) {
                target.put(it, source.get(it));
            }
        }

        return true;
    }

    /**
     * Identifies the differnt kind of input files for scenario definitions and standalone mixin metadata files.
     * This is needed to be able to use the same service for both handling mixins in scenario definitions and
     * handling standalone mixin metadata files, as we need to know where to find the metadata and where to
     * generate the output.
     */
    public enum MixinServiceType {
        StandaloneMixins,
        ScenarioDefinition
    }

    /**
     * Context information for evaluating SpEL expressions in mixin elements. This is needed as we allow to use
     * SpEL expressions in mixin elements that can refer to properties of the scenario definition, the mixin element
     * itself and the current element. This class is used to provide these properties in a structured way for the
     * SpEL evaluation.
     */
    @Data
    @AllArgsConstructor
    private static class EvaluationContext {
        private ScenarioDefinition sd;
        private ElementDefinition mixinElement;
        private ElementDefinition element;
    }

    @Data
    static class ProcessingInfo {
        private Set<String> ids = new HashSet<>();
        private Map<String, List<MixinInfo>> mixins = new HashMap<>();
        private ExtendedScenarioDefinition scenarioDefinition;
        private ServiceParameters params;

        private Collection<ElementDefinition> ed2Add = new ArrayList<>();

        public ProcessingInfo(ServiceParameters params, final ExtendedScenarioDefinition sd) {
            this.params = params;
            this.scenarioDefinition = sd;
        }
    }

    /**
     * Context information for the mixin service. This includes the current Maven project, the folder where the
     * metadata is stored, the target folder for generated files and a map with mixin paths for libraries (key
     * is the library name, value is the path to the mixin metadata file). This information is needed for both
     * handling mixins in scenario definitions and for handling standalone mixin metadata files, as we need to know
     * where to find the metadata and where to generate the output.
     */
    @Data
    @AllArgsConstructor
    public static class ServiceParameters {
        private MavenProject project;
        private String metadataFolder;
        private String targetFolder;
        private Map<String, String> mixinPaths;
    }
}

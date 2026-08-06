package com.sap.bfx.maven.common.services;

import com.sap.bfx.definition.*;
import com.sap.bfx.maven.common.AbstractProcessor;
import com.sap.bfx.maven.common.definition.MixinInfo;
import com.sap.bfx.session.ElementRow;
import com.sap.bfx.utils.IdentifierUtils;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service class for generating Java source files based on scenario definitions and mixins. It uses the Freemarker
 * template engine to create access classes, field enums, context factory classes, and mixin mapping structures.
 */
@Service
public class TemplateService extends AbstractProcessor {

    final private static String NM_VERSION = "version";
    final private static String NM_BASE_PACKAGE = "basePackage";
    final private static String NM_COLLECTION = "collection";
    final private static String NM_HAS_DIALOG = "dialog";
    final private static String NM_ELEMENTS = "elements";
    final private static String NM_LEAF = "leaf";
    final private static String NM_CLASS = "className";
    final private static String NM_CLASSES = "classes";
    final private static String NM_DEF = "definition";
    final private static String NM_IS_ROOT = "isRoot";
    final private static String NM_ROOT_KEY = "rootKey";
    final private static String NM_ROOT_DATA_TYPE = "rootDataType";
    final private static String NM_RETURN_DATA_TYPE = "returnDataType";
    final private static String NM_RETURN_DIALOG_DATA_TYPE = "returnDialogType";
    //    final private static String NM_TYPE_DATA_TYPE = "typeDataType";
    final private static String NM_TYPE_DIALOG_DATA_TYPE = "typeDialogDataType";
    //    final private static String NM_TYPES = "types";
    final private static String NM_ROOT_ROW_ID = "rootRowId";
    final private static String NM_CAN_BE_EDITABLE = "canBeEditable";
    final private static String NM_CAN_BE_REQUIRED = "canBeRequired";
    final private static String NM_CAN_HAVE_MESSAGE = "canHaveMessage";

//    final private static String TYPE_EXCLUSION
//            = "ElementDefinition.BooleanType;ElementDefinition.StringType;ElementDefinition.AttachmentType;"
//            + "ElementDefinition.IntegerType;ElementDefinition.BigDecimalType;ElementDefinition.DateType;"
//            + "ElementDefinition.TimeType;ElementDefinition.DateTimeType;ElementDefinition.DateRangeType";

    final private static DataTypeInfo DATA_TYPE_INFO_STRING = new DataTypeInfo("String");
    final private static DataTypeInfo DATA_TYPE_INFO_ATTACHMENT = new DataTypeInfo("Attachments");
    final private static DataTypeInfo DATA_TYPE_INFO_DATERANGE = new DataTypeInfo("DateRange");
    final private static DataTypeInfo DATA_TYPE_INFO_CURRENCY = new DataTypeInfo("MoneyAmount");
    final private static DataTypeInfo DATA_TYPE_INFO_DOCFORM = new DataTypeInfo("DocFormData");
    final private static DataTypeInfo DATA_TYPE_INFO_INTEGER = new DataTypeInfo("Integer");
    final private static DataTypeInfo DATA_TYPE_INFO_BIGDECIMAL = new DataTypeInfo("java.math.BigDecimal");
    final private static DataTypeInfo DATA_TYPE_INFO_DATE = new DataTypeInfo("java.time.LocalDate");
    final private static DataTypeInfo DATA_TYPE_INFO_TIME = new DataTypeInfo("java.time.LocalTime");
    final private static DataTypeInfo DATA_TYPE_INFO_DATETIME = new DataTypeInfo("java.time.LocalDateTime");
    final private static DataTypeInfo DATA_TYPE_INFO_BOOLEAN = new DataTypeInfo("Boolean");

    private final Configuration templateCfg;

    private final MetadataService metadataService;

    /**
     * Constructor for TemplateService. It initializes the Freemarker template engine and retrieves the MetadataService
     * bean from the application context.
     *
     * @param appContext the application context from which to retrieve the MetadataService bean
     */
    @Autowired
    public TemplateService(ApplicationContext appContext) {

        this.metadataService = appContext.getBean(MetadataService.class);

        // Initialize the template engine (freemarker)
        templateCfg = new Configuration(Configuration.VERSION_2_3_32);
        templateCfg.setClassLoaderForTemplateLoading(TemplateService.class.getClassLoader(), "templates");
        templateCfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        templateCfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        templateCfg.setLogTemplateExceptions(false);
        templateCfg.setWrapUncheckedExceptions(true);
        templateCfg.setFallbackOnNullLoopVariable(false);
    }

    /**
     * Generates Java source files based on scenario definitions and mixins. It creates access classes, field enums,
     * context factory classes, and mixin mapping structures by processing the corresponding Freemarker templates.
     *
     * @param targetPath the path where the generated Java source files should be saved
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    public void generate(final String targetPath) throws IOException, TemplateException {
        log.info(">> start generating to '" + targetPath + "'");

        // generate access classes
        this.createAccessClasses(targetPath);
        // generate field enums
        this.createFieldEnumsForScenarios(targetPath);
        // generate context factory class
        this.createAccessClassFactory(targetPath);
        // generate enums and mapping structures for mixins
        this.createMixinMappings(targetPath);
    }

    /**
     * Generates field enums for mixins based on the provided scenario definitions and mixin information. It scans the
     * elements of the scenario definitions to build up the necessary data for access classes and then creates field
     * enums for the scenarios.
     *
     * @param targetPath the path where the generated field enum Java source files should be saved
     * @param def        the scenario definition containing the mixin information to be processed
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    public void createFieldEnumsForMixin(final String targetPath, MixinDefinition def)
            throws IOException, TemplateException {
        // scan elements to build up the necessary data for access classes
        final var params = new HashMap<String, Map<String, Object>>();
        createNewClassInfo(params, def.getAccessObjectName() + def.getVersion(), def.getVersion(), def.getBasePackage(),
                false, false, "", "");

        final Map<String, List<MixinInfo>> mixins = new HashMap<>();

        for (var ed : def.getElements()) {
            scanElement(params, def.getAccessObjectName() + def.getVersion(), mixins, def, ed, ScanType.EnumScan);
        }

        createFieldEnumsForScenarios(targetPath, params);
    }

    /**
     * Scans the given element definition and its children to build up the necessary data for access classes and field
     * enums. It adds the element information to the class info and processes mixins if there are any for the given type.
     *
     * @param params          the map containing the class information to be built up
     * @param accessClassName the name of the access class being processed
     * @param mixins          the map containing the mixin information for the scenario definitions
     * @param sd              the scenario definition containing the element definitions to be scanned
     * @param ed              the element definition to be scanned
     * @param scanType        the type of scan being performed (AccessClassScan or EnumScan)
     */
    private void scanElement(final Map<String, Map<String, Object>> params, String accessClassName,
                             final Map<String, List<MixinInfo>> mixins, final AbstractStructureDefinition sd,
                             final ElementDefinition ed, final ScanType scanType) {
        if (!ed.isRootType()) {
            // add the element info to the class info
            var ae = new HashMap<String, Object>();
            ae.put(NM_DEF, ed);
            ae.put(NM_COLLECTION, ed.isCollection());
            ae.put(NM_LEAF, ed.getElements() == null || ed.getElements().isEmpty());
            final var info = this.getDataTypeClassName(accessClassName, ed);
            ae.put(NM_RETURN_DATA_TYPE, info.getReturnType());
//            ae.put(NM_TYPE_DATA_TYPE, info.getType());
            ae.put(NM_CAN_BE_EDITABLE, ed.canBeEditable());
            ae.put(NM_CAN_BE_REQUIRED, ed.canBeRequired());
            ae.put(NM_CAN_HAVE_MESSAGE, ed.canHaveMessage());
            ae.put(NM_HAS_DIALOG, false);
//            if (!StringUtils.endsWith(info.getType(), ".class")) {
//                ((Set<Pair<String, String>>) params.get(accessClassName).get(NM_TYPES)).add(
//                        new ImmutablePair<>(info.getReturnType(), info.getType()));
//            }
//            if (StringUtils.isNotBlank(info.getDialogType()) && !StringUtils.endsWith(info.getDialogType(), ".class")) {
//                ((Set<Pair<String, String>>) params.get(accessClassName).get(NM_TYPES)).add(
//                        new ImmutablePair<>(info.getDialogReturnType(), info.getDialogType()));
//            }
            // Search-help needs
            if (UIElementType.SearchHelp.equals(ed.getType())) {
                ae.put(NM_HAS_DIALOG, true);
                ae.put(NM_RETURN_DIALOG_DATA_TYPE, info.getDialogReturnType());
                ae.put(NM_TYPE_DIALOG_DATA_TYPE, info.getDialogType());
            }
            // For Dialogs we set the according flag to true
            if (UIElementType.Dialog.equals(ed.getType())) {
                ae.put(NM_HAS_DIALOG, true);
                ae.put(NM_RETURN_DIALOG_DATA_TYPE, info.getDialogReturnType());
            }

            // add to the elements lists of ac
//            log.debug("Adding element '" + ed.getName() + "' to class '" + accessClassName + "'");
            ((List<HashMap<String, Object>>) params.get(accessClassName).get(NM_ELEMENTS)).add(ae);
        }

        if (ed.hasOwnType()) {
            if (!ed.isRootType()) {
                accessClassName += ed.getName();
            }

            var ac = params.get(accessClassName);
            if (ac == null) {
                ac = createNewClassInfo(params, accessClassName, sd.getVersion(), sd.getBasePackage(),
                        ed.isCollection(), ed.isRootType(), ed.getKey(), getDataTypeClassName("", ed).returnType);
            }

            // add mixins if there are any for the given type
            if (scanType == ScanType.EnumScan) {
//                log.debug("TemplateService::Scan: Mixin Keys: " + sd.getMixins().keySet());
//                log.debug("TemplateService::Scan: Name of Type: " + accessClassName);
                if (mixins.containsKey(accessClassName)) {
//                    log.debug("TemplateService::Scan: found!");
                    for (var it : mixins.get(accessClassName)) {
//                        log.debug("TemplateService::Scan: Found " + it.getMixin().getName());
                        // add the mixin element to the according type
                        var ae = new HashMap<String, Object>();
                        ae.put(NM_DEF, it.getMixin());
                        ((Collection<Map<String, Object>>) ac.get(NM_ELEMENTS)).add(ae);
                    }
                }
            }
        }

        // scan all children
        for (var it1 : ed.getChildren()) {
            for (var it2 : it1) {
                scanElement(params, accessClassName, mixins, sd, it2, scanType);
            }
        }
    }

    /**
     * Creates a new class info map for the given access class name and adds it to the params map.
     *
     * @param params          the map containing the class information to be built up
     * @param accessClassName the name of the access class for which to create the class info
     * @param version         the version of the scenario definition containing the access class
     * @param basePackage     the base package for the generated access class
     * @param isCollection    whether the access class represents a collection type
     * @param isRoot          whether the access class represents a root type
     * @param rootKey         the key of the element definition if the access class represents a root type
     * @param rootDataType    the data type of the root element if the access class represents a root type
     * @return the created class info map
     */
    public HashMap<String, Object> createNewClassInfo(final Map<String, Map<String, Object>> params,
                                                      final String accessClassName, final int version,
                                                      final String basePackage, final boolean isCollection,
                                                      final boolean isRoot, final String rootKey, String rootDataType) {

        final var ac = new HashMap<String, Object>();

        ac.put(NM_CLASS, accessClassName);
        ac.put(NM_VERSION, version);
        ac.put(NM_BASE_PACKAGE, basePackage);
        ac.put(NM_COLLECTION, isCollection);
        ac.put(NM_ELEMENTS, new ArrayList<HashMap<String, Object>>());
//        ac.put(NM_TYPES, new HashSet<Pair<String, String>>());
        ac.put(NM_ROOT_ROW_ID, ElementRow.ROOT);
        ac.put(NM_IS_ROOT, isRoot);
        ac.put(NM_ROOT_KEY, rootKey);
        ac.put(NM_ROOT_DATA_TYPE, rootDataType);
        //TODO(ML) fill singleSelect

        params.put(accessClassName, ac);
        return ac;
    }

    /**
     * Generates Java source files for access classes based on the provided scenario definitions. It scans the elements
     * of the scenario definitions to build up the necessary data for access classes and then creates the access class
     * Java source files using the corresponding Freemarker template.
     *
     * @param targetPath the path where the generated access class Java source files should be saved
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    private void createAccessClasses(final String targetPath) throws IOException, TemplateException {
        // scan elements to build up the necessary data for access classes
        var params = new HashMap<String, Map<String, Object>>();
        metadataService.getScenarioDefinitions().forEach(sd -> sd.getElements().forEach(
                it -> scanElement(params, sd.getAccessObjectName() + sd.getVersion(), sd.getMixins(), sd, it,
                        ScanType.AccessClassScan)));

        final var template = templateCfg.getTemplate("AccessClass.ftlh");

        for (var it : params.values()) {
//            log.info("Class " + it.get(NM_CLASS) + " has " + ((Set<?>) it.get(NM_TYPES)).size() + " types.");
            try (var writer = this.openFile(targetPath + File.separator + it.get(NM_BASE_PACKAGE) + ".metadata",
                    it.get(NM_CLASS) + ".java")) {
                template.process(it, writer);
            }
        }
    }

    /**
     * Generates field enum Java source files for the scenarios based on the provided scenario definitions. It scans the
     * elements of the scenario definitions to build up the necessary data for access classes and then creates the field
     * enum Java source files using the corresponding Freemarker templates.
     *
     * @param targetPath the path where the generated field enum Java source files should be saved
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    private void createFieldEnumsForScenarios(final String targetPath) throws IOException, TemplateException {
        // scan elements to build up the necessary data for access classes
        var params = new HashMap<String, Map<String, Object>>();
        for (var sd : metadataService.getScenarioDefinitions()) {
            for (var ed : sd.getElements()) {
                scanElement(params, sd.getAccessObjectName() + sd.getVersion(), sd.getMixins(), sd, ed,
                        ScanType.EnumScan);
            }
        }

        createFieldEnumsForScenarios(targetPath, params);
    }

    /**
     * Generates field enum Java source files for the scenarios based on the provided class information parameters. It
     * creates the field enum Java source files using the corresponding Freemarker templates.
     *
     * @param targetPath the path where the generated field enum Java source files should be saved
     * @param params     the map containing the class information to be used for generating the field enum Java source files
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    private void createFieldEnumsForScenarios(final String targetPath, Map<String, Map<String, Object>> params)
            throws IOException, TemplateException {

        for (int i = 0; i < 2; i++) {
            final var template = templateCfg.getTemplate(i == 0 ? "FieldAccessor.ftlh" : "FieldEnum.ftlh");
            for (var it : params.values()) {
                try (var writer = this.openFile(targetPath + File.separator + it.get(NM_BASE_PACKAGE) + ".metadata",
                        it.get(NM_CLASS) + (i == 0 ? "Fields.java" : "FieldsEnum.java"))) {
                    template.process(it, writer);
                }
            }
        }
    }

    /**
     * Generates the AccessClassFactory Java source file based on the provided scenario definitions. It creates the
     * AccessClassFactory Java source file using the corresponding Freemarker template and includes the necessary
     * information about the access classes for each scenario definition.
     *
     * @param targetPath the path where the generated AccessClassFactory Java source file should be saved
     * @throws IOException       if an I/O error occurs while writing the generated file
     * @throws TemplateException if an error occurs while processing the Freemarker template
     */
    private void createAccessClassFactory(final String targetPath) throws IOException, TemplateException {
        final var template = templateCfg.getTemplate("AccessClassFactory.ftlh");
        final var params = new HashMap<String, Object>();
        final var classes = new HashMap<String, Map<String, String>>();
        var basePackage = "";

        for (var sd : metadataService.getScenarioDefinitions()) {
            log.info(">> access-object-name: " + sd.getAccessObjectName() + ", version: " + sd.getVersion());
            final var info = new HashMap<String, String>();
            info.put(NM_VERSION, String.valueOf(sd.getVersion()));
            info.put(NM_CLASS, sd.getAccessObjectName());
            info.put(NM_BASE_PACKAGE, sd.getBasePackage());
//            log.debug(">> Generating access class factory with version '" + sd.getVersion() + "'"
//                    + " and base package '" + sd.getBasePackage() + "'");
            classes.put(String.valueOf(sd.getVersion()), info);

            if (sd.isActive()) {
                basePackage = sd.getBasePackage();
            }
        }
        params.put(NM_BASE_PACKAGE, basePackage);
        params.put(NM_CLASSES, classes);

        try (var writer = this.openFile(targetPath + File.separator + basePackage + ".metadata",
                "AccessClassFactoryImpl.java")) {
            template.process(params, writer);
        }
    }

    /**
     * Generates mixin mapping Java source files based on the provided scenario definitions and mixin information. It
     * creates mapping enums for the mixins and a mapping class that contains the mapping information for all mixins
     * used in the scenario definitions. The generated Java source files are created using the corresponding Freemarker
     * templates.
     *
     * @param targetPath the path where the generated mixin mapping Java source files should be saved
     * @throws IOException       if an I/O error occurs while writing the generated files
     * @throws TemplateException if an error occurs while processing the Freemarker templates
     */
    private void createMixinMappings(final String targetPath) throws IOException, TemplateException {
        // Create a mapping enum with for the mixin and all it's members
        {
            for (var sd : metadataService.getScenarioDefinitions()) {
                final var params = new HashMap<String, Map<String, Object>>();

                for (var it : sd.getMixins().values()) {
                    for (var mixinInfo : it) {
                        final var name = "Mixin" + IdentifierUtils.toPascalCase(
                                ((MetaFileElementDefinition) mixinInfo.getMixin()).getMixinName()) + sd.getVersion() +
                                "Fields";

                        if (!params.containsKey(name)) {
                            final var p = new HashMap<String, Object>();
                            params.put(name, p);

                            p.put(NM_CLASS, name);
                            p.put(NM_BASE_PACKAGE, sd.getBasePackage());

                            final var prefixLength = StringUtils.length(mixinInfo.getMixin().getName());
                            final var elements = new ArrayList<Pair<String, String>>();
                            p.put(NM_ELEMENTS, elements);
                            mixinInfo.getElements().forEach(e -> {
                                final var n = StringUtils.substring(e.getName(), prefixLength);
                                elements.add(new ImmutablePair<>(n, DigestUtils.md5Hex(n)));
                            });
                        }
                    }
                }

                for (var i = 0; i < 2; i++) {
                    final var template = templateCfg.getTemplate(i == 0 ? "MixinAccessor.ftlh" : "MixinEnum.ftlh");
                    for (var p : params.values()) {
                        try (var writer = this.openFile(
                                targetPath + File.separator + p.get(NM_BASE_PACKAGE) + ".metadata",
                                p.get(NM_CLASS) + (i == 0 ? "" : "Enum") + ".java")) {
                            template.process(p, writer);
                        }
                    }
                }
            }
        }

        // Create the mapping class
        {
            final var template = templateCfg.getTemplate("MixinMapping.ftlh");

            for (var sd : metadataService.getScenarioDefinitions()) {
                final var m = new HashMap<String, Object>();

                m.put(NM_CLASS,
                        "Mixin" + IdentifierUtils.toPascalCase(sd.getAccessObjectName()) + sd.getVersion() + "Mapping");
                m.put(NM_BASE_PACKAGE, sd.getBasePackage());

                // Elements map contains an item for each mixin found in the definition (even recursive elements)
                final var em = new HashMap<String, List<Pair<String, String>>>();
                m.put(NM_ELEMENTS, em);
                for (var key : sd.getMixins().keySet()) {
                    final var it = sd.getMixins().get(key);
                    // for each mixin element we add an entry in the list that is the value for each mixin, so
                    // we can have a link from the mixin to all of its fields
                    for (var mixinInfo : it) {
                        final var l = new ArrayList<Pair<String, String>>();
                        em.put(key + "Fields." + mixinInfo.getMixin().getName(), l);
                        mixinInfo.getElements().forEach(e -> {
                            final var mixinName = "Mixin" + IdentifierUtils.toPascalCase(
                                    ((MetaFileElementDefinition) mixinInfo.getMixin()).getMixinName()) +
                                    sd.getVersion() + "Fields." + StringUtils.substring(e.getName(),
                                    StringUtils.length(mixinInfo.getMixin().getName()));

//                            this.log.info("Calling findAccessClassForElement with elements=" + sd.getElements() +
//                                    ", className=" + sd.getAccessObjectName() + sd.getVersion() + ", element=" + e);
                            var mixedFieldName = mixinInfo.getClassName() + "Fields." + e.getName();
//                            this.log.info("=> " + mixedFieldName);

                            l.add(new ImmutablePair<>(mixinName, mixedFieldName));
                        });
                    }
                }

                try (var writer = this.openFile(targetPath + File.separator + m.get(NM_BASE_PACKAGE) + ".metadata",
                        m.get(NM_CLASS) + ".java")) {
                    template.process(m, writer);
                }
            }
        }
    }

    /**
     * Opens a file for writing the generated Java source code. It ensures that the necessary directories exist and
     * creates a BufferedWriter for the specified file.
     *
     * @param path  the path where the file should be created
     * @param fname the name of the file to be created
     * @return a Writer for writing to the specified file
     * @throws IOException if an I/O error occurs while creating the file or its parent directories
     */
    private Writer openFile(String path, String fname) throws IOException {
        // Ensure path exists
        path = StringUtils.replace(path, ".", File.separator);
//        log.debug("Ensuring path for '" + path + File.separator + fname + "'");
        var p = PathUtils.createParentDirectories(new File(path + File.separator + fname).toPath());
//        log.debug("Created path '" + p.toString() + "'");

        // Write definitions.json file to target directory
        return new BufferedWriter(new FileWriter(path + File.separator + fname));
    }

    /**
     * Determines the return type and dialog type for the given element definition based on its UI element type and data
     * type. It returns a DataTypeInfo object containing the determined return type and dialog type information.
     *
     * @param accessClassName the name of the access class being processed, used for constructing custom types
     * @param ed              the element definition for which to determine the data type information
     * @return a DataTypeInfo object containing the determined return type and dialog type information
     */
    private DataTypeInfo getDataTypeClassName(final String accessClassName, final ElementDefinition ed) {
        return switch (ed.getType()) {
            case Select, MultiSelect, Radio, Button, Text, TextEdit -> DATA_TYPE_INFO_STRING;
            case Attachment -> DATA_TYPE_INFO_ATTACHMENT;
            case DateRangePicker -> DATA_TYPE_INFO_DATERANGE;
            case Input -> switch (ed.getDataType()) {
                case Int -> DATA_TYPE_INFO_INTEGER;
                case Decimal -> DATA_TYPE_INFO_BIGDECIMAL;
                case Date -> DATA_TYPE_INFO_DATE;
                case Time -> DATA_TYPE_INFO_TIME;
                case DateTime -> DATA_TYPE_INFO_DATETIME;
                case Boolean -> DATA_TYPE_INFO_BOOLEAN;
                default -> DATA_TYPE_INFO_STRING;
            };
            case Dialog, SearchHelp ->
                    new DataTypeInfo("String", accessClassName + ed.getName(), accessClassName + ed.getName() + "Type");
            case Table -> new DataTypeInfo(accessClassName + ed.getName()/*, accessClassName + ed.getName() + "Type"*/);
            case Currency -> DATA_TYPE_INFO_CURRENCY;
            case DocForm -> DATA_TYPE_INFO_DOCFORM;
            default -> DATA_TYPE_INFO_BOOLEAN;
        };
    }

    /**
     * Enum representing the type of scan being performed when processing element definitions. It is used to differentiate
     * between scanning for access class information and scanning for field enum information when processing the scenario definitions and their elements.
     */
    private enum ScanType {
        AccessClassScan,
        EnumScan
    }

    /**
     * Class representing the data type information for an element definition. It contains the return type and dialog
     * type information that is determined based on the UI element type and data type of the element definition.
     */
    @Data
    private static class DataTypeInfo {
        private String returnType;
        //        private String type;
        private String dialogReturnType;
        private String dialogType;

        /**
         * @param dataType
         */
        DataTypeInfo(final String dataType) {
            this.returnType = dataType;
//            this.type = dataType + ".class";
        }

//        /**
//         * @param dataType
//         * @param type
//         */
//        DataTypeInfo(final String dataType, final String type) {
//            this(dataType);
//            this.type = type;
//        }

        /**
         * @param dataType
         * @param dialogReturnType
         * @param dialogType
         */
        DataTypeInfo(final String dataType, final String dialogReturnType, final String dialogType) {
            this(dataType);
            this.dialogReturnType = dialogReturnType;
            this.dialogType = dialogType;
        }
    }
}

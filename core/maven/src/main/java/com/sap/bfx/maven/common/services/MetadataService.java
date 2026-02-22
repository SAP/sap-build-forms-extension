package com.sap.bfx.maven.common.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sap.bfx.definition.*;
import com.sap.bfx.maven.Constants;
import com.sap.bfx.maven.common.AbstractProcessor;
import com.sap.bfx.maven.common.definition.ExtendedScenarioDefinition;
import com.sap.bfx.maven.common.definition.ExtendedScenarioDefinitionDeserializer;
import com.sap.bfx.utils.IdentifierUtils;
import com.sap.bfx.utils.PropertyFileUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class MetadataService extends AbstractProcessor {

    private Map<Integer, ExtendedScenarioDefinition> scenarioDefinitionMap = new HashMap<>();
    private final Map<String, Map<Integer, MixinDefinition>> mixinDefinitionMap = new HashMap<>();

    /**
     * Finds the scenario definition for the given version. If no scenario definition is found for the given version, a
     * new scenario definition is created, added to the map and returned. This ensures that there is always a scenario
     * definition for the given version, even if it is not defined in the meta-data files.
     *
     * @param version
     * @return
     */
    public ScenarioDefinition findScenarioVersion(int version) {
        var sd = scenarioDefinitionMap.get(version);
        if (sd == null) {
            sd = new ExtendedScenarioDefinition();
            scenarioDefinitionMap.put(version, sd);
        }
        return sd;
    }

    /**
     * Finds the mixin definition for the given name and version. If no mixin definition is found for the given name
     * and version, a new mixin definition is created, added to the map and returned. This ensures that there is
     * always a mixin definition for the given name and version, even if it is not defined in the meta-data files.
     *
     * @param name
     * @param version
     * @return
     */
    public MixinDefinition findMixinNameVersion(String name, int version) {
        var innerMap = mixinDefinitionMap.get(name);
        if (innerMap == null) {
            innerMap = new HashMap<>();
        }
        var mixinDefinition = innerMap.get(version);
        if (mixinDefinition == null) {
            mixinDefinition = new MixinDefinition();
            mixinDefinition.setName(name);
            mixinDefinition.setVersion(version);
            innerMap.put(version, mixinDefinition);
            mixinDefinitionMap.put(name, innerMap);
        }
        return mixinDefinition;
    }

    /**
     * Finds the texts for the given scenario definition version and locale. If no texts are found for the given
     * scenario definition version and locale, a new texts map is created, added to the scenario definition and
     * returned. This ensures that there is always a texts map for the given scenario definition version and locale,
     * even if it is not defined in the meta-data files.
     *
     * @param version
     * @param locale
     * @return
     */
    public Map<String, String> findTexts(int version, Locale locale) {
        final ScenarioDefinition sd = this.findScenarioVersion(version);
        // texts is set in scenario-definition constructor, so it's always defined..
        var texts = sd.getTexts();
        return texts.computeIfAbsent(locale, v -> new HashMap<>());
    }

    public Map<String, String> findTextsMixin(String mixinName, int version, Locale locale) {
        final MixinDefinition md = this.findMixinNameVersion(mixinName, version);
        // texts is set in scenario-definition constructor, so it's always defined..
        var texts = md.getTexts();
        return texts.computeIfAbsent(locale, v -> new HashMap<>());
    }

    public Collection<ExtendedScenarioDefinition> getScenarioDefinitions() {
        return scenarioDefinitionMap.values();
    }

    public Set<Integer> getScenarioDefinitionVersions() {
        return scenarioDefinitionMap.keySet();
    }

    /**
     * Scans the given root path for scenario definition meta-data files and reads them into the service. The method will
     * look for files with the name pattern 'definition.{version}.yaml' and 'texts_def_{locale}.{version}.properties'.
     *
     * @param rootPath the root path to scan for meta-data files
     * @throws Exception if any error occurs during scanning or reading the files
     */
    public void scanDefinitionMetadata(String rootPath) throws Exception {
        this.scenarioDefinitionMap.clear();
        final var paths = new ArrayList<String>();

        scanMetadata(MetadataType.Definition, rootPath, paths);

        for (var path : paths) {
            final String fName = FilenameUtils.getName(path);
            final String[] fParts = fName.split("\\.");
            final int version = Integer.parseInt(fParts[1]);

            if (StringUtils.equalsIgnoreCase(fParts[0], Constants.NAME_PREFIX_DEFINITION)) {
                log.info("    processing definition file '" + fName + "'");
                try (var is = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                    final var mapper = new ObjectMapper(new YAMLFactory());
                    final var module = new SimpleModule();
                    module.addDeserializer(ExtendedScenarioDefinition.class,
                            new ExtendedScenarioDefinitionDeserializer());
                    mapper.registerModule(module);

                    final var sd = mapper.readValue(is, ExtendedScenarioDefinition.class);
                    sd.setElements(sortElements(sd.getElements()));
                    if (sd.getVersion() != version) {
                        throw new Exception(
                                "Metadata file with version " + version + " contains version " + sd.getVersion() +
                                        " as version in file data!");
                    }
                    if (scenarioDefinitionMap.containsKey(version)) {
                        sd.setTexts(scenarioDefinitionMap.get(version).getTexts());
                    }
//                    log.debug(">> Loaded Scenario Definition: '" + sd.getName() + "' (version '"
//                            + sd.getVersion() + "')");
                    scenarioDefinitionMap.put(version, sd);
                }
            } else if (StringUtils.startsWithIgnoreCase(fParts[0], Constants.NAME_PREFIX_TEXTS)) {
                log.info("    found Texts file '" + fName + "'");
                var map = this.findTexts(version, this.getLocaleFromFilename(fParts[0]));
                PropertyFileUtils.readTexts(path, "", map);
            }
        }
    }

    /**
     * Scans the given root path for mixin definition meta-data files and reads them into the service. The method will
     * look for files with the name pattern 'mixin.{name}.{version}.yaml' and 'texts_{name}_{locale}.{version}.properties'.
     *
     * @param rootPath the root path to scan for meta-data files
     * @throws Exception if any error occurs during scanning or reading the files
     */
    public void scanMixinMetadata(String rootPath) throws Exception {
        this.mixinDefinitionMap.clear();
        final var paths = new ArrayList<String>();

        scanMetadata(MetadataType.Mixin, rootPath, paths);

        for (var path : paths) {
            final String fName = FilenameUtils.getName(path);
            final String[] fParts = fName.split("\\.");
            final String name = fParts[1];
            final int version = Integer.parseInt(fParts[2]);

            if (StringUtils.equalsIgnoreCase(fParts[0], Constants.NAME_PREFIX_MIXIN)) {
                log.info("    processing mixin file '" + fName + "'");
                try (var is = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
                    final var mapper = new ObjectMapper(new YAMLFactory());
                    final var module = new SimpleModule();
                    module.addDeserializer(MixinDefinition.class, new MixinDefinitionDeserializer<>(false));
                    mapper.registerModule(module);

                    final var sd = mapper.readValue(is, MixinDefinition.class);
                    sd.setElements(sortElements(sd.getElements()));
                    if (sd.getVersion() != version) {
                        throw new Exception(
                                "Metadata file with version " + version + " contains version " + sd.getVersion() +
                                        " as version in file data!");
                    }

                    Map<Integer, MixinDefinition> innerMap = mixinDefinitionMap.get(name);
                    if (innerMap == null) {
                        innerMap = new HashMap<>();
                    }
                    sd.setName(name);
                    innerMap.put(version, sd);
                    mixinDefinitionMap.put(name, innerMap);
                }
            } else if (StringUtils.startsWithIgnoreCase(fParts[0], Constants.NAME_PREFIX_TEXTS)) {
                log.info("    found Texts file '" + fName + "'");
                var map = this.findTextsMixin(name, version, this.getLocaleFromFilename(fParts[0]));
                PropertyFileUtils.readTexts(path, "", map);
            }
        }
    }

    /**
     * Scans the given root path for meta-data files of the given type and adds their paths to the given collection.
     * The method will look for files with the name pattern 'definition.{version}.yaml' and
     * 'texts_def_{locale}.{version}.properties' for scenario definitions, and
     * 'mixin.{name}.{version}.yaml' and 'texts_{name}_{locale}.{version}.properties' for mixin definitions.
     *
     * @param type the type of meta-data files to scan for
     * @param rootPath the root path to scan for meta-data files
     * @param paths the collection to add the paths of found meta-data files to
     * @throws Exception if any error occurs during scanning the files
     */
    public void scanMetadata(MetadataType type, String rootPath, Collection<String> paths) throws Exception {
        log.info("  scanning " + rootPath);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(rootPath))) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    log.debug("    found folder " + path);
                    scanMetadata(type, path.toString(), paths);
                } else {
                    log.debug("    found file " + path);
                    scanFile(type, path, paths);
                }
            }
        }
    }

    /**
     * Scans the given file for meta-data of the given type and adds its path to the given collection if it matches the
     * expected file name pattern for the meta-data type. The method will look for files with the name pattern
     * 'definition.{version}.yaml' and 'texts_def_{locale}.{version}.properties' for scenario definitions, and
     * 'mixin.{name}.{version}.yaml' and 'texts_{name}_{locale}.{version}.properties' for mixin definitions.
     *
     * @param type the type of meta-data to scan for
     * @param path the path of the file to scan for meta-data
     * @param paths the collection to add the path of the found meta-data file to
     * @throws Exception if any error occurs during scanning the file
     */
    private void scanFile(MetadataType type, Path path, Collection<String> paths) throws Exception {
        final String fName = FilenameUtils.getName(path.toString());

        if (type == MetadataType.Definition) {
            if (StringUtils.countMatches(fName, '.') != 2) {
                log.warn("    file '" + fName + "' cannot be split into 3 parts, separated by '.' -> skipping it");
                return;
            }

            if (StringUtils.startsWithIgnoreCase(fName, Constants.NAME_PREFIX_DEFINITION)) {
                log.debug("    found definition file '" + fName + "'");
                paths.add(path.toString());
            } else if (StringUtils.startsWithIgnoreCase(fName, Constants.NAME_PREFIX_TEXTS)) {
                log.debug("    found definition texts file '" + fName + "'");
                paths.add(path.toString());
            }
        } else if (type == MetadataType.Mixin) {
            if (StringUtils.countMatches(fName, '.') != 3) {
                log.warn("    file '" + fName + "' cannot be split into 4 parts, separated by '.' -> skipping it");
                return;
            }

            if (StringUtils.startsWithIgnoreCase(fName, Constants.NAME_PREFIX_MIXIN)) {
                log.debug("    found mixin file '" + fName + "'");
                paths.add(path.toString());
            } else if (StringUtils.startsWithIgnoreCase(fName, Constants.NAME_PREFIX_TEXTS)) {
                log.debug("    found mixin text file '" + fName + "'");
                paths.add(path.toString());
            }
        }
    }

    /**
     * Extracts the locale from the given file name. The method will look for the last '_' character in the file name and
     * take the substring after it as the locale. For example, for a file name 'texts_def_en.1.properties', the method will
     * return a Locale object for 'en'.
     *
     * @param fName the file name to extract the locale from
     * @return a Locale object representing the locale extracted from the file name
     */
    private Locale getLocaleFromFilename(String fName) {
//        final Log log = this.getLog();
        var localeText = StringUtils.substringAfterLast(fName, "_");
//        log.info("        getLocalFromFilename for " + fName + " -> " + localeText);
        return new Locale(localeText);
    }

    /**
     * Writes the meta-data of all scenario definitions into a single JSON file at the given path. The method will create
     * the parent directories for the file if they do not exist, and then write the meta-data into a file named
     * 'definitions.json' in the target directory.
     *
     * @param path the path to write the meta-data JSON file to
     * @throws IOException if any error occurs during writing the file
     */
    public void writeMetadataToDefinitionJson(String path) throws IOException {
        // Ensure path exists
        log.debug("Creating path: " + path);
        PathUtils.createParentDirectories(new File(path + "/definitions.json").toPath());
        log.debug("Created: " + path);

        // Write definitions.json file to target directory
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(path + "/definitions.json", StandardCharsets.UTF_8))) {
            this.writeMetadataAsJson(writer, true, true);
        }
    }

    /**
     * Writes the meta-data of all mixin definitions into a single JSON file at the given path. The method will create
     * the parent directories for the file if they do not exist, and then write the meta-data into a file named
     * 'mixins.json' in the target directory.
     *
     * @throws IOException if any error occurs during writing the file
     */
    public String getMetadataAsJson() throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            this.writeMetadataAsJson(writer, false, true);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return writer.toString();
    }

    /**
     * Writes the meta-data of all mixin definitions into a single JSON file at the given path. The method will create
     * the parent directories for the file if they do not exist, and then write the meta-data into a file named
     * 'mixins.json' in the target directory.
     *
     * @throws IOException if any error occurs during writing the file
     */
    public String getMixinMetadataAsJson() throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            this.writeMixinMetadataAsJson(writer, false, true);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return writer.toString();
    }

    /**
     * Writes the meta-data of all scenario definitions into the given writer in JSON format. The method will use a custom
     * serializer to write the meta-data in a specific format, and will include keys and texts in the output based on the
     * given parameters.
     *
     * @param writer the writer to write the meta-data JSON to
     * @param includeKeys whether to include keys in the output JSON
     * @param includeTexts whether to include texts in the output JSON
     * @throws IOException if any error occurs during writing the JSON
     */
    private void writeMetadataAsJson(final Writer writer, final boolean includeKeys, final boolean includeTexts)
            throws IOException {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        module.addSerializer(ScenarioDefinition.class, new ScenarioDefinitionSerializer(includeKeys, includeTexts));
        mapper.registerModule(module);
        mapper.writeValue(writer, scenarioDefinitionMap);
    }

    /**
     * Writes the meta-data of all mixin definitions into the given writer in JSON format. The method will use a custom
     * serializer to write the meta-data in a specific format, and will include keys and texts in the output based on the
     * given parameters.
     *
     * @param writer the writer to write the meta-data JSON to
     * @param includeKeys whether to include keys in the output JSON
     * @param includeTexts whether to include texts in the output JSON
     * @throws IOException if any error occurs during writing the JSON
     */
    private void writeMixinMetadataAsJson(final Writer writer, final boolean includeKeys, final boolean includeTexts)
            throws IOException {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        module.addSerializer(MixinDefinition.class, new MixinDefinitionSerializer(includeKeys, includeTexts));
        mapper.registerModule(module);
        mapper.writeValue(writer, mixinDefinitionMap);
    }

    /**
     * Writes the meta-data of all scenario definitions and mixin definitions into separate files in the given root
     * path. The method will look for existing meta-data files in the root path and update them with the current
     * meta-data, or create new files if they do not exist. The method will also delete any existing meta-data files
     * in the root path that are not updated with the current meta-data.
     *
     * @param rootPath the root path to write the meta-data files to
     * @throws IOException if any error occurs during writing the files
     */
    public void writeMetadataFiles(final String rootPath) throws IOException {
        writeScenarioMetadataFiles(rootPath);
        writeMixinMetadataFiles(rootPath);
    }

    /**
     * Writes the meta-data of all scenario definitions into separate files in the given root path. The method will look for
     * existing meta-data files in the root path and update them with the current meta-data, or create new files if they do
     * not exist. The method will also delete any existing meta-data files in the root path that are not updated with the
     * current meta-data.
     *
     * @param rootPath the root path to write the meta-data files to
     * @throws IOException if any error occurs during writing the files
     */
    public void writeScenarioMetadataFiles(final String rootPath) throws IOException {
        List<String> files_modified = new ArrayList<>();
        var files = FileUtils.listFiles(new File(rootPath), null, true);
        for (Map.Entry<Integer, ExtendedScenarioDefinition> definitionEntry : scenarioDefinitionMap.entrySet()) {
            ExtendedScenarioDefinition sd = definitionEntry.getValue();
            sd.setElements(sortElements(sd.getElements()));
            var f = findAndEnsureFile(sd, files, rootPath, "definition.%s.yaml");
            files_modified.add((f.getName()));
            // write meta-data file
            this.writeScenarioMetadataFile(f, sd);

            // go through all languages and update/create the properties files for labels
            for (Map.Entry<Locale, Map<String, String>> entry : sd.getTexts().entrySet()) {
                File languageFile = findAndEnsureLanguageFile(files, entry.getKey(), sd.getVersion(), rootPath, null);
                files_modified.add(languageFile.getName());
                saveLanguageFile(languageFile, entry.getValue());
            }
        }

        // Delete all other meta-related files
        for (File file : files) {
            if (!files_modified.contains(file.getName()) &&
                    (file.getName().startsWith("definition") || file.getName().startsWith("texts_def"))) {
                if (file.delete()) {
                    System.out.println("File " + file.getName() + " successfully deleted");
                } else {
                    System.out.println("File " + file.getName() + " could not be deleted");
                }
            }
        }
    }

    /**
     * Writes the meta-data of all mixin definitions into separate files in the given root path. The method will look for
     * existing meta-data files in the root path and update them with the current meta-data, or create new files if they do
     * not exist. The method will also delete any existing meta-data files in the root path that are not updated with the
     * current meta-data.
     *
     * @param rootPath the root path to write the meta-data files to
     * @throws IOException if any error occurs during writing the files
     */
    public void writeMixinMetadataFiles(final String rootPath) throws IOException {
        var files = FileUtils.listFiles(new File(rootPath), null, true);
        List<String> files_modified = new ArrayList<>();
        for (var mixinNames : this.mixinDefinitionMap.values()) {
            for (var mixin : mixinNames.values()) {
                mixin.setElements(sortElements(mixin.getElements()));
                var f = findAndEnsureMixinFile(files, mixin.getName(), mixin.getVersion(), rootPath);
                files_modified.add((f.getName()));
                this.writeMixinMetadataFile(f, mixin);
                //TODO: Texts
                for (Map.Entry<Locale, Map<String, String>> entry : mixin.getTexts().entrySet()) {
                    File languageFile = findAndEnsureLanguageFile(files, entry.getKey(), mixin.getVersion(), rootPath,
                            mixin.getName());
                    files_modified.add(languageFile.getName());
                    saveLanguageFile(languageFile, entry.getValue());
                }
            }
        }

        // Delete all other meta-related files
        for (File file : files) {
            if (!files_modified.contains(file.getName()) &&
                    (file.getName().startsWith("mixin") || file.getName().startsWith("texts_de.") ||
                            file.getName().startsWith("texts_en."))) {
                if (file.delete()) {
                    System.out.println("File " + file.getName() + " successfully deleted");
                } else {
                    System.out.println("File " + file.getName() + " could not be deleted");
                }
            }
        }
    }

    /**
     * Finds the file for the given scenario definition in the given collection of files, or creates a new file if it
     * does not exist. The method will look for a file with the name pattern 'definition.{version}.yaml' in the
     * collection of files, where {version} is the version of the given scenario definition. If such a file is found,
     * it is returned and removed from the collection of files. If no such file is found, a new file with the name
     * pattern 'definition.{version}.yaml' is created in the given root path and returned.
     *
     * @param sd the scenario definition to find or create the file for
     * @param files the collection of files to search for the existing file
     * @param rootPath the root path to create a new file in if no existing file is found
     * @return the existing or newly created file for the given scenario definition
     */
    private File findAndEnsureFile(final ScenarioDefinition sd, Collection<File> files, final String rootPath,
                                   final String fNameTemplate) {
        final var fName = String.format(fNameTemplate, sd.getVersion());
        // search if file already exists or if needed to be created...
        var optFile =
                files.stream().filter(f -> StringUtils.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
        if (optFile.isPresent()) {
            files.remove(optFile.get());
            return optFile.get();
        } else {
            try {
                File fNew = new File(FilenameUtils.concat(rootPath, fName));
                FileUtils.touch(fNew);
                return fNew;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Writes the meta-data of the given scenario definition into the given file in YAML format. The method will use a custom
     * serializer to write the meta-data in a specific format, and will not include keys and texts in the output.
     *
     * @param f the file to write the meta-data to
     * @param sd the scenario definition to write the meta-data of
     */
    private void writeScenarioMetadataFile(final File f, final ScenarioDefinition sd) {
        try (var writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            final var mapper = new ObjectMapper(new YAMLFactory());
            final var module = new SimpleModule();
            module.addSerializer(ScenarioDefinition.class, new ScenarioDefinitionSerializer(false, false));
            mapper.registerModule(module);

            mapper.writeValue(writer, sd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the meta-data of the given mixin definition into the given file in YAML format. The method will use a custom
     * serializer to write the meta-data in a specific format, and will not include keys and texts in the output.
     *
     * @param f the file to write the meta-data to
     * @param mixin the mixin definition to write the meta-data of
     */
    private void writeMixinMetadataFile(final File f, final MixinDefinition mixin) {
        try (var writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            final var mapper = new ObjectMapper(new YAMLFactory());
            final var module = new SimpleModule();
            module.addSerializer(MixinDefinition.class, new MixinDefinitionSerializer(false, false));
            mapper.registerModule(module);
            mapper.writeValue(writer, mixin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the given map of texts into the given file in properties format. The method will write each entry of the map
     * as a line in the file with the format 'key=value', where key is the key of the entry and value is the value of the
     * entry. The method will use UTF-8 encoding to write the file.
     *
     * @param file the file to write the texts to
     * @param entries the map of texts to write into the file
     * @throws IOException if any error occurs during writing the file
     */
    private void saveLanguageFile(final File file, Map<String, String> entries) throws IOException {
        try (var writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    writer.append(entry.getKey()).append("=").append(entry.getValue()).append(System.lineSeparator());
                }
            }
        }
    }

    /**
     * Finds the language file for the given locale and scenario definition version in the given collection of files,
     * or creates a new file if it does not exist. The method will look for a file with the name pattern
     * 'texts_def_{locale}.{version}.properties' in the collection of files, where {locale} is the given locale
     * and {version} is the version of the scenario definition. If such a file is found, it is returned and removed
     * from the collection of files. If no such file is found, a new file with the name pattern
     * 'texts_def_{locale}.{version}.properties' is created in the given root path and returned.
     *
     * @param files the collection of files to search for the existing file
     * @param language the locale to find or create the language file for
     * @param version the version of the scenario definition to find or create the language file for
     * @param rootPath the root path to create a new file in if no existing file is found
     * @return the existing or newly created language file for the given locale and scenario definition version
     */
    private File findAndEnsureLanguageFile(final Collection<File> files, final Locale language, final int version,
                                           final String rootPath, final String mixinName) {
        String fName;
        if (mixinName == null) {
            fName = String.format("texts_def_%s.%s.properties", language, version);
        } else {
            fName = String.format("texts_%s.%s.%s.properties", language, mixinName, version);
        }

        File file;
        var optFile =
                files.stream().filter(f -> StringUtils.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
        if (optFile.isPresent()) {
            files.remove(optFile.get());
            file = optFile.get();
        } else {
            try {
                File fNew = new File(FilenameUtils.concat(rootPath, fName));
                FileUtils.touch(fNew);
                file = fNew;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * Finds the mixin file for the given name and version in the given collection of files, or creates a new file if it
     * does not exist. The method will look for a file with the name pattern 'mixin.{name}.{version}.yaml' in the
     * collection of files, where {name} is the given name and {version} is the given version. If such a file is found,
     * it is returned and removed from the collection of files. If no such file is found, a new file with the name
     * pattern 'mixin.{name}.{version}.yaml' is created in the given root path and returned.
     *
     * @param files the collection of files to search for the existing file
     * @param name the name to find or create the mixin file for
     * @param version the version to find or create the mixin file for
     * @param rootPath the root path to create a new file in if no existing file is found
     * @return the existing or newly created mixin file for the given name and version
     */
    private File findAndEnsureMixinFile(final Collection<File> files, final String name, final int version,
                                        final String rootPath) {
        final var fName = String.format("mixin.%s.%s.yaml", name, version);
        File file;
        var optFile =
                files.stream().filter(f -> StringUtils.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
        if (optFile.isPresent()) {
            files.remove(optFile.get());
            file = optFile.get();
        } else {
            try {
                File fNew = new File(FilenameUtils.concat(rootPath, fName));
                FileUtils.touch(fNew);
                file = fNew;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * Reads the meta-data of scenario definitions from the given input stream in JSON format and stores it in the
     * service. The method will use a custom deserializer to read the meta-data in a specific format, and will
     * populate the scenario definition map with the read meta-data.
     *
     * @param is the input stream to read the meta-data JSON from
     * @throws Exception if any error occurs during reading the JSON
     */
    public void readMetadataFromJson(final InputStream is) throws Exception {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        module.addDeserializer(ExtendedScenarioDefinition.class, new ExtendedScenarioDefinitionDeserializer());
        mapper.registerModule(module);
        scenarioDefinitionMap = mapper.readValue(is, new TypeReference<>() {
        });
    }

    /**
     * Reads the meta-data of mixin definitions from the given input stream in JSON format and stores it in the
     * service. The method will use a custom deserializer to read the meta-data in a specific format, and will
     * populate the mixin definition map with the read meta-data.
     *
     * @param is the input stream to read the meta-data JSON from
     * @throws Exception if any error occurs during reading the JSON
     */
    public void readMixinMetadataFromJson(final InputStream is) throws Exception {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        module.addDeserializer(MixinDefinition.class, new MixinDefinitionDeserializer<>(true));
        mapper.registerModule(module);
        Map<Integer, MixinDefinition> map = mapper.readValue(is, new TypeReference<>() {
        });
        mixinDefinitionMap.clear();
        for (MixinDefinition entry : map.values()) {
            Map<Integer, MixinDefinition> innerMap = mixinDefinitionMap.get(entry.getName());
            if (innerMap == null) {
                innerMap = new HashMap<>();
            }
            innerMap.put(entry.getVersion(), entry);
            mixinDefinitionMap.put(entry.getName(), innerMap);
        }
    }

    /**
     * Finds the access class name for the given element in the given list of elements. The method will recursively search
     * through the list of elements and their sub-elements to find an element with the same key as the given search
     * element. If such an element is found, the method will return the given class name. If no such element is found,
     * the method will return null.
     *
     * @param elements the list of elements to search through
     * @param className the class name to return if an element with the same key as the search element is found
     * @param search the element to search for in the list of elements
     * @return the class name if an element with the same key as the search element is found, or null if no such
     * element is found
     */
    public String findAccessClassForElement(final List<ElementDefinition> elements, String className,
                                            final ElementDefinition search) {
        if (elements != null && !elements.isEmpty()) {
            for (var it : elements) {
                if (StringUtils.equals(it.getKey(), search.getKey())) {
                    return className;
                }
                final var helpClassName = (it.hasOwnType() && it.getType() != UIElementType.Form &&
                        it.getType() != UIElementType.Wizard) ? className + IdentifierUtils.camelCase(it.getName()) :
                        className;
                // recursive action for subtree
                var r = findAccessClassForElement(it.getElements(), helpClassName, search);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Sorts the given list of elements by their sort value and name. The method will first sort the elements by their
     * sort value in descending order, and if two elements have the same sort value, it will sort them by their name
     * in ascending order. After sorting the elements, the method will set the sort value of form and wizard elements to
     * 1, the sort value of dialog elements to a number starting from 150 and increasing by 10 for each dialog element,
     * and the sort value of all other elements to a number starting from 10 and increasing by 10 for each element. The
     * method will then recursively sort the sub-elements of form elements with header segments and all other elements.
     *
     * @param elements the list of elements to sort
     * @return the sorted list of elements
     */
    private List<ElementDefinition> sortElements(List<ElementDefinition> elements) {
        // order by sort-value and name
        elements.sort((element2, element1) -> {
            int sort1 = Integer.compare(element1.getSort(), element2.getSort());
            if (sort1 == 0) {
                int sort2 = element1.getName().compareTo(element2.getName());
                if (sort2 == 0) {
                    return sort2;
                } else {
                    return -sort2;
                }
            } else {
                return -sort1;
            }
        });

        int number = 10;
        int numberDialog = 150;

        for (ElementDefinition e : elements) {
            if (e instanceof FormElementDefinition || e instanceof WizardElementDefinition) {
                e.setSort(1);
            } else if (e instanceof DialogElementDefinition) {
                e.setSort(numberDialog);
                numberDialog += 10;
            } else {
                e.setSort(number);
                number += 10;
            }
        }

        for (ElementDefinition e : elements) {
            if (e instanceof FormElementDefinition && ((FormElementDefinition) e).getHeaderSegment() != null) {
                ((FormElementDefinition) e).getHeaderSegment().setElements(
                        sortElements(((FormElementDefinition) e).getHeaderSegment().getElements()));
            }
            if (e.getElements() != null) {
                e.setElements(sortElements(e.getElements()));
            }
        }

        return elements;
    }

    /**
     * Enum representing the type of meta-data to scan for. The enum has two values: Definition for scenario definitions and
     * Mixin for mixin definitions. This enum is used to specify the type of meta-data files to look for when scanning a
     * directory for meta-data files, and to determine the expected file name patterns for the meta-data files.
     */
    public enum MetadataType {
        Definition, Mixin
    }

}
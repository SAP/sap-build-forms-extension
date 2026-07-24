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
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Service for processing the metadata information coming from the yaml definition files
 */
@Service
public class MetadataService extends AbstractProcessor {

    private final Map<String, Map<Integer, MixinDefinition>> mixinDefinitionMap = new HashMap<>();
    private Map<Integer, ExtendedScenarioDefinition> scenarioDefinitionMap = new HashMap<>();

    /**
     * Finds the scenario definition for a given version. If it does not exist, a new ExtendedScenarioDefinition is
     * created and added to the map.
     *
     * @param version the version of the scenario definition to find
     * @return the found or newly created ScenarioDefinition
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
     * Finds the mixin definition for a given name and version. If it does not exist, a new MixinDefinition is created
     * and added to the map.
     *
     * @param name    the name of the mixin definition to find
     * @param version the version of the mixin definition to find
     * @return the found or newly created MixinDefinition
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
     * Finds the texts for a given scenario version and locale. If the texts do not exist, a new map is created and
     * added to the scenario definition.
     *
     * @param version the version of the scenario definition to find
     * @param locale  the locale for which to find the texts
     * @return the found or newly created map of texts
     */
    public Map<String, String> findTexts(int version, Locale locale) {
        final ScenarioDefinition sd = this.findScenarioVersion(version);
        // texts is set in scenario-definition constructor, so it's always defined..
        var texts = sd.getTexts();
        return texts.computeIfAbsent(locale, v -> new HashMap<>());
    }

    /**
     * Finds the texts for a given mixin name, version, and locale. If the texts do not exist, a new map is created and
     * added to the mixin definition.
     *
     * @param mixinName the name of the mixin definition to find
     * @param version   the version of the mixin definition to find
     * @param locale    the locale for which to find the texts
     * @return the found or newly created map of texts
     */
    public Map<String, String> findTextsMixin(String mixinName, int version, Locale locale) {
        final MixinDefinition md = this.findMixinNameVersion(mixinName, version);
        // texts is set in scenario-definition constructor, so it's always defined..
        var texts = md.getTexts();
        return texts.computeIfAbsent(locale, v -> new HashMap<>());
    }

    /**
     * Returns a collection of all scenario definitions currently stored in the service.
     *
     * @return a collection of ExtendedScenarioDefinition objects
     */
    public Collection<ExtendedScenarioDefinition> getScenarioDefinitions() {
        return scenarioDefinitionMap.values();
    }

//    /**
//     * Returns a set of all scenario definition versions currently stored in the service.
//     *
//     * @return a set of Integer values representing the scenario definition versions
//     */
//    public Set<Integer> getScenarioDefinitionVersions() {
//        return scenarioDefinitionMap.keySet();
//    }

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

            if (Strings.CI.equals(fParts[0], Constants.NAME_PREFIX_DEFINITION)) {
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
            } else if (Strings.CI.startsWith(fParts[0], Constants.NAME_PREFIX_TEXTS)) {
                log.info("    found Texts file '" + fName + "'");
                var map = this.findTexts(version, this.getLocaleFromFilename(fParts[0]));
                PropertyFileUtils.readTexts(path, "", map);
            }
        }
    }

    /**
     * Scans the given root path for mixin definition meta-data files and reads them into the service. The method will
     * look for files with the name pattern 'mixin.{name}.{version}.yaml' and 'texts_{locale}.{name}.{version}.properties'.
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

            if (Strings.CI.equals(fParts[0], Constants.NAME_PREFIX_MIXIN)) {
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
            } else if (Strings.CI.startsWith(fParts[0], Constants.NAME_PREFIX_TEXTS)) {
                log.info("    found Texts file '" + fName + "'");
                var map = this.findTextsMixin(name, version, this.getLocaleFromFilename(fParts[0]));
                PropertyFileUtils.readTexts(path, "", map);
            }
        }
    }

    /**
     * Recursively scans the given root path for meta-data files of the specified type (Definition or Mixin) and adds
     * their paths to the provided collection.
     *
     * @param type     the type of metadata to scan for (Definition or Mixin)
     * @param rootPath the root path to scan for meta-data files
     * @param paths    the collection to which found file paths will be added
     * @throws Exception if any error occurs during scanning
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
     * Scans a single file to determine if it matches the expected naming conventions for the specified metadata type
     * (Definition or Mixin). If it does, the file path is added to the provided collection.
     *
     * @param type  the type of metadata to scan for (Definition or Mixin)
     * @param path  the path of the file to scan
     * @param paths the collection to which found file paths will be added
     */
    private void scanFile(MetadataType type, Path path, Collection<String> paths) {
        final String fName = FilenameUtils.getName(path.toString());

        if (type == MetadataType.Definition) {
            if (StringUtils.countMatches(fName, '.') != 2) {
                log.warn("    file '" + fName + "' cannot be split into 3 parts, separated by '.' -> skipping it");
                return;
            }

            if (Strings.CI.startsWith(fName, Constants.NAME_PREFIX_DEFINITION)) {
                log.debug("    found definition file '" + fName + "'");
                paths.add(path.toString());
            } else if (Strings.CI.startsWith(fName, Constants.NAME_PREFIX_TEXTS)) {
                log.debug("    found definition texts file '" + fName + "'");
                paths.add(path.toString());
            }
        } else if (type == MetadataType.Mixin) {
            if (StringUtils.countMatches(fName, '.') != 3) {
                log.warn("    file '" + fName + "' cannot be split into 4 parts, separated by '.' -> skipping it");
                return;
            }

            if (Strings.CI.startsWith(fName, Constants.NAME_PREFIX_MIXIN)) {
                log.debug("    found mixin file '" + fName + "'");
                paths.add(path.toString());
            } else if (Strings.CI.startsWith(fName, Constants.NAME_PREFIX_TEXTS)) {
                log.debug("    found mixin text file '" + fName + "'");
                paths.add(path.toString());
            }
        }
    }

    /**
     * Extracts the locale from a given filename. The locale is expected to be the substring after the last underscore
     * in the filename.
     *
     * @param fName the filename from which to extract the locale
     * @return the extracted Locale object
     */
    private Locale getLocaleFromFilename(String fName) {
//        final Log log = this.getLog();
        var localeText = StringUtils.substringAfterLast(fName, "_");
//        log.info("        getLocalFromFilename for " + fName + " -> " + localeText);
        return new Locale(localeText);
    }

    /**
     * Writes the metadata information to a JSON file named 'definitions.json' in the specified path. The method
     * ensures that the parent directories exist before writing the file.
     *
     * @param path the path where the 'definitions.json' file will be created
     * @throws IOException if any error occurs during file writing
     */
    public void writeMetadataToDefinitionJson(String path) throws IOException {
        // Ensure path exists
        log.debug("Creating path: " + path);
        PathUtils.createParentDirectories(new File(path + "/definitions.json").toPath());
        log.debug("Created: " + path);

        // Write definitions.json file to target directory
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(path + "/definitions.json", StandardCharsets.UTF_8))) {
            this.writeMetadataAsJson(writer, true, true, false);
        }
    }

    /**
     * Writes the mixin metadata information to a JSON structure and returns this as a string. Will be used from
     * frontend and for writing files as well.
     *
     * @param toFrontend whether the output is intended for frontend consumption
     * @throws IOException if any error occurs during file writing
     */
    public String getMetadataAsJson(final boolean toFrontend) throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            this.writeMetadataAsJson(writer, false, true, toFrontend);
        } finally {
            IOUtils.closeQuietly(writer);
        }
        return writer.toString();
    }

    /**
     * Writes the mixin metadata information to a JSON file named 'mixins.json' in the specified path. The method
     * ensures that the parent directories exist before writing the file.
     *
     * @throws IOException if any error occurs during file writing
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
     * Writes the scenario metadata information to a JSON format using the provided Writer. The method uses the
     * Jackson ObjectMapper to serialize the scenarioDefinitionMap into JSON, applying custom serialization rules
     * defined in ScenarioDefinitionSerializer.
     *
     * @param writer       the Writer to which the JSON will be written
     * @param includeKeys  whether to include keys in the serialized output
     * @param includeTexts whether to include texts in the serialized output
     * @param toFrontend   whether the output is intended for frontend consumption
     * @throws IOException if any error occurs during writing
     */
    private void writeMetadataAsJson(final Writer writer, final boolean includeKeys, final boolean includeTexts,
                                     final boolean toFrontend) throws IOException {
        final var mapper = new ObjectMapper();
        final var module = new SimpleModule();
        module.addSerializer(ScenarioDefinition.class,
                new ScenarioDefinitionSerializer(includeKeys, includeTexts, toFrontend));
        mapper.registerModule(module);
        mapper.writeValue(writer, scenarioDefinitionMap);
    }

    /**
     * Writes the mixin metadata information to a JSON format using the provided Writer. The method uses the
     * Jackson ObjectMapper to serialize the mixinDefinitionMap into JSON, applying custom serialization rules
     * defined in MixinDefinitionSerializer.
     *
     * @param writer       the Writer to which the JSON will be written
     * @param includeKeys  whether to include keys in the serialized output
     * @param includeTexts whether to include texts in the serialized output
     * @throws IOException if any error occurs during writing
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
     * Writes the scenario and mixin metadata information to their respective files in the specified root path. The method
     * calls writeScenarioMetadataFiles and writeMixinMetadataFiles to handle the writing of each type of metadata.
     *
     * @param rootPath the root path where the metadata files will be written
     * @throws IOException if any error occurs during file writing
     */
    public void writeMetadataFiles(final String rootPath) throws IOException {
        writeScenarioMetadataFiles(rootPath);
        writeMixinMetadataFiles(rootPath);
    }

    /**
     * Writes the scenario metadata information to their respective files in the specified root path. The method
     * iterates through the scenarioDefinitionMap, writing each definition and its associated texts to files. It also
     * deletes any old meta-related files that are no longer needed.
     *
     * @param rootPath the root path where the metadata files will be written
     * @throws IOException if any error occurs during file writing
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
     * Writes the mixin metadata information to their respective files in the specified root path. The method
     * iterates through the mixinDefinitionMap, writing each definition and its associated texts to files. It also
     * deletes any old meta-related files that are no longer needed.
     *
     * @param rootPath the root path where the metadata files will be written
     * @throws IOException if any error occurs during file writing
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
     * Finds a file based on the scenario definition and ensures it exists. If the file does not exist, it is created.
     * The method searches for a file with a name formatted according to the provided template and the version of the
     * scenario definition.
     *
     * @param sd            the scenario definition for which to find or create the file
     * @param files         the collection of existing files to search through
     * @param rootPath      the root path where the file should be located or created
     * @param fNameTemplate the template for formatting the filename, which should include a placeholder for the version
     * @return the found or newly created File object
     */
    private File findAndEnsureFile(final ScenarioDefinition sd, Collection<File> files, final String rootPath,
                                   final String fNameTemplate) {
        final var fName = String.format(fNameTemplate, sd.getVersion());
        // search if file already exists or if needed to be created...
        var optFile =
                files.stream().filter(f -> Strings.CI.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
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
     * Writes the scenario definition metadata to a YAML file. The method uses the Jackson ObjectMapper with a YAML
     * factory to serialize the ScenarioDefinition object into YAML format, applying custom serialization rules defined
     * in ScenarioDefinitionSerializer.
     *
     * @param f  the File object representing the file to which the metadata will be written
     * @param sd the ScenarioDefinition object containing the metadata to be written
     */
    private void writeScenarioMetadataFile(final File f, final ScenarioDefinition sd) {
        try (var writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            final var mapper = new ObjectMapper(new YAMLFactory());
            final var module = new SimpleModule();
            module.addSerializer(ScenarioDefinition.class, new ScenarioDefinitionSerializer(false, false, false));
            mapper.registerModule(module);

            mapper.writeValue(writer, sd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the mixin definition metadata to a YAML file. The method uses the Jackson ObjectMapper with a YAML
     * factory to serialize the MixinDefinition object into YAML format, applying custom serialization rules defined
     * in MixinDefinitionSerializer.
     *
     * @param f     the File object representing the file to which the metadata will be written
     * @param mixin the MixinDefinition object containing the metadata to be written
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
     * Saves the provided language entries to a properties file. The method writes each key-value pair from the entries
     * map to the specified file in UTF-8 encoding, ensuring that null keys or values are not written.
     *
     * @param file    the File object representing the properties file to which the entries will be written
     * @param entries a map containing the key-value pairs to be written to the properties file
     * @throws IOException if any error occurs during file writing
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
     * Finds a language properties file based on the specified locale, version, and mixin name, and ensures it exists.
     * If the file does not exist, it is created. The method searches for a file with a name formatted according to the
     * provided parameters.
     *
     * @param files     the collection of existing files to search through
     * @param language  the Locale object representing the language for which to find or create the file
     * @param version   the version number to be included in the filename
     * @param rootPath  the root path where the file should be located or created
     * @param mixinName the name of the mixin, if applicable; can be null for scenario definitions
     * @return the found or newly created File object
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
                files.stream().filter(f -> Strings.CI.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
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
     * Finds a mixin metadata file based on the specified name and version, and ensures it exists. If the file does not
     * exist, it is created. The method searches for a file with a name formatted according to the provided parameters.
     *
     * @param files    the collection of existing files to search through
     * @param name     the name of the mixin for which to find or create the file
     * @param version  the version number to be included in the filename
     * @param rootPath the root path where the file should be located or created
     * @return the found or newly created File object
     */
    private File findAndEnsureMixinFile(final Collection<File> files, final String name, final int version,
                                        final String rootPath) {
        final var fName = String.format("mixin.%s.%s.yaml", name, version);
        File file;
        var optFile =
                files.stream().filter(f -> Strings.CI.equals(FilenameUtils.getName(f.getName()), fName)).findFirst();
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
     * Reads the scenario metadata from a JSON input stream and populates the scenarioDefinitionMap. The method uses
     * the Jackson ObjectMapper to deserialize the JSON data into a map of ExtendedScenarioDefinition objects, applying
     * custom deserialization rules defined in ExtendedScenarioDefinitionDeserializer.
     *
     * @param is the InputStream from which to read the JSON data
     * @throws Exception if any error occurs during reading or deserialization
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
     * Reads the mixin metadata from a JSON input stream and populates the mixinDefinitionMap. The method uses the
     * Jackson ObjectMapper to deserialize the JSON data into a map of MixinDefinition objects, applying custom
     * deserialization rules defined in MixinDefinitionDeserializer.
     *
     * @param is the InputStream from which to read the JSON data
     * @throws Exception if any error occurs during reading or deserialization
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
     * Recursively searches for the access class name for a given element in a list of elements.
     *
     * @param elements  the list of elements to search through
     * @param className the current class name to append to
     * @param search    the element definition to search for
     * @return the access class name if found, otherwise null
     */
    public String findAccessClassForElement(final List<ElementDefinition> elements, String className,
                                            final ElementDefinition search) {
        if (elements != null && !elements.isEmpty()) {
            for (var it : elements) {
                if (Strings.CI.equals(it.getKey(), search.getKey())) {
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
     * Sorts a list of ElementDefinition objects based on their sort value and name.
     * Elements with the same sort value are sorted by name in descending order.
     * Additionally, the method assigns new sort values to elements based on their type.
     *
     * @param elements the list of ElementDefinition objects to be sorted
     * @return the sorted list of ElementDefinition objects
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
     * Enum representing the type of metadata being processed, either Definition or Mixin.
     */
    public enum MetadataType {
        Definition,
        Mixin
    }
}
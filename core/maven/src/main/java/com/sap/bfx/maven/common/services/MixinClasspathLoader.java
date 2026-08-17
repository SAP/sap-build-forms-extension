package com.sap.bfx.maven.common.services;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;

import com.sap.bfx.definition.MetaFileElementDefinition;
import com.sap.bfx.definition.MixinDefinition;
import com.sap.bfx.utils.FileUtils;

/**
 * Important
 * <p>
 * - in definition file this needs to be declared as classpath:start-name-of-jar-dependency
 * .e.g. classpath:forms-scenario-common
 * <p>
 * - its possible to use replacements like $7{name} and define <name>...</name> in pom as mixinPaths
 * <p>
 */
class MixinClasspathLoader extends MixinLoader {

    MixinClasspathLoader(final MixinService.ProcessingInfo processingInfo, final MetaFileElementDefinition mixin,
                         final Log log) {
        super(processingInfo, mixin, log);
    }

    @Override
    public MixinDefinition load() throws Exception {

        final var rootPath = StringSubstitutor.replace(
                StringUtils.substring(((MetaFileElementDefinition) mixin).getPath(), 10),
                processingInfo.getParams().getMixinPaths(), "${", "}");
        log.debug("classpath is: " + rootPath);

        final var project = processingInfo.getParams().getProject();
        File artifcatFile = null;
        log.debug("Searching in:");
        for (Artifact it : ((Set<Artifact>) project.getDependencyArtifacts())) {
            if (it.getFile() == null) {
                // Direct dependency not yet resolved to a file (e.g. Mojo missing
                // requiresDependencyResolution). Skip rather than NPE.
                continue;
            }
            var fName = it.getFile().getName();
            log.debug("- " + fName);

            if (StringUtils.startsWithIgnoreCase(fName, rootPath)) {
                artifcatFile = it.getFile();
                log.debug("-> found -> breaking");
                break;
            }
        }

        if (artifcatFile == null) {
            throw new Exception("Cannot find a dependency artifact for '" + rootPath + "'!");
        }

        log.info("Try to load mixin resources from jar file: '" + artifcatFile.getPath() + "'.");
        final var jarFile = new JarFile(artifcatFile);

        final var defFilePattern = Pattern.compile("^mixin\\."
                + mixin.getMixinName() + "\\." + mixin.getVersion() + "\\.ya?ml");
        final var textFilePattern = Pattern.compile("^texts_(.*)\\."
                + mixin.getMixinName() + "\\." + mixin.getVersion() + "\\.properties");

        JarEntry mixinDefEntry = null;
        final var mixinPropEntries = new ArrayList<JarEntry>();

        for (var en = jarFile.entries(); en.hasMoreElements(); ) {
            var it = en.nextElement();
            var fName = it.getName();

            if (defFilePattern.matcher(fName).matches() || textFilePattern.matcher(fName).matches()) {
                log.debug("- matched: " + fName);

                if (FileUtils.isYamlFile(fName)) {
//                log.debug("Mixing: found metadata file!");
                    mixinDefEntry = it;
                } else if (FileUtils.isPropertyFile(fName)) {
                    mixinPropEntries.add(it);
                }
            }
        }

        MixinDefinition mixinDef = null;
        if (mixinDefEntry != null) {
            try (var is = new InputStreamReader(jarFile.getInputStream(mixinDefEntry), StandardCharsets.UTF_8)) {
                mixinDef = readElementDef(is);
            }
        }
        if (mixinDef == null) {
            throw new RuntimeException("cannot load mixin '" + mixin.getMixinName() + "' from '"
                    + artifcatFile.getName() + "'!");
        }
        for (var propEntry : mixinPropEntries) {
            final var matcher = textFilePattern.matcher(propEntry.getName());
            if (!matcher.matches()) {
                throw new Exception("Irregular properties file match for '" + propEntry.getName() + "'!");
            }
//                log.debug("Mixin: properties does match? " + matcher.matches());
//                log.debug("Mixin: groups for properties file: " + matcher.groupCount() + ", group: " + matcher.group(1));
            final var locale = new Locale(matcher.group(1));
            readTexts(jarFile.getInputStream(propEntry), locale, mixin.getName(), mixinDef);
        }

        return mixinDef;
    }
}

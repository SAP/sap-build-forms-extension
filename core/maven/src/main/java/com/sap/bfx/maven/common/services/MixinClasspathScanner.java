package com.sap.bfx.maven.common.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sap.bfx.definition.MixinDefinition;
import com.sap.bfx.definition.MixinDefinitionDeserializer;
import com.sap.bfx.utils.PropertyFileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enumerates all mixin YAML files (and their sibling text properties) at the root of every direct
 * dependency jar of a Maven project. Used by the dev server to surface classpath-provided mixins
 * in the metadata editor so that scenarios which reference shared mixins via `classpath:` still see
 * their structure in the UI.
 * <p>
 * This is a read-only view — the dev server never writes back into these jars.
 */
class MixinClasspathScanner {

    private static final Pattern MIXIN_FILE_PATTERN = Pattern.compile("^mixin\\.(.+)\\.(\\d+)\\.ya?ml$");

    /**
     * Scan every direct dependency jar of the given project for root-level mixin files.
     *
     * @param project the current Maven project (must not be {@code null})
     * @param log     Maven plugin log (may be {@code null} — logging is best-effort)
     * @return a list of loaded mixin definitions; empty if no matching entries were found
     */
    public List<MixinDefinition> scanAllMixins(final MavenProject project, final Log log) throws Exception {
        final var result = new ArrayList<MixinDefinition>();

        @SuppressWarnings("unchecked") final Set<Artifact> artifacts = project.getDependencyArtifacts();
        if (artifacts == null) {
            return result;
        }

        for (final Artifact artifact : artifacts) {
            final var file = artifact.getFile();
            if (file == null || !file.getName().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                continue;
            }

            try (JarFile jarFile = new JarFile(file)) {
                // First pass: collect all matching root-level mixin YAML entries by (name, version).
                final var mixinEntries = new ArrayList<JarEntry>();
                final var textEntries = new ArrayList<JarEntry>();

                for (var en = jarFile.entries(); en.hasMoreElements(); ) {
                    final var entry = en.nextElement();
                    if (entry.isDirectory()) continue;
                    final var name = entry.getName();
                    // Only look at jar-root entries; nested paths are not our concern here.
                    if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) continue;

                    if (MIXIN_FILE_PATTERN.matcher(name).matches()) {
                        mixinEntries.add(entry);
                    } else if (name.startsWith("texts_") && name.endsWith(".properties")) {
                        textEntries.add(entry);
                    }
                }

                if (mixinEntries.isEmpty()) continue;

                if (log != null) {
                    log.info("Scanning classpath mixins from jar: '" + file.getName() + "' ("
                            + mixinEntries.size() + " mixin file(s))");
                }

                for (final var mixinEntry : mixinEntries) {
                    final var matcher = MIXIN_FILE_PATTERN.matcher(mixinEntry.getName());
                    if (!matcher.matches()) continue; // impossible — already filtered
                    final var mixinName = matcher.group(1);
                    final var version = Integer.parseInt(matcher.group(2));

                    final MixinDefinition def;
                    try (var is = new InputStreamReader(jarFile.getInputStream(mixinEntry), StandardCharsets.UTF_8)) {
                        final var mapper = new ObjectMapper(new YAMLFactory())
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        final var module = new SimpleModule();
                        module.addDeserializer(MixinDefinition.class, new MixinDefinitionDeserializer<>(false));
                        mapper.registerModule(module);
                        def = mapper.readValue(is, MixinDefinition.class);
                    }
                    def.setName(mixinName);
                    def.setVersion(version);

                    // Load sibling text files: texts_<locale>.<name>.<version>.properties
                    final var textPattern = Pattern.compile("^texts_(.+)\\."
                            + Pattern.quote(mixinName) + "\\." + version + "\\.properties$");
                    for (final var textEntry : textEntries) {
                        final var textMatcher = textPattern.matcher(textEntry.getName());
                        if (!textMatcher.matches()) continue;
                        final var locale = new Locale(textMatcher.group(1));
                        final var texts = def.getTexts().computeIfAbsent(locale, k -> new HashMap<>());
                        PropertyFileUtils.readTexts(jarFile.getInputStream(textEntry), "", texts);
                    }

                    result.add(def);
                }
            }
        }

        return result;
    }
}

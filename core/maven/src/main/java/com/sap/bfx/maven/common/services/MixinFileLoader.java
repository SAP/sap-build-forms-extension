package com.sap.bfx.maven.common.services;

import com.sap.bfx.definition.MetaFileElementDefinition;
import com.sap.bfx.definition.MixinDefinition;
import com.sap.bfx.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.maven.plugin.logging.Log;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

class MixinFileLoader extends MixinLoader {

    MixinFileLoader(final MixinService.ProcessingInfo processingInfo, final MetaFileElementDefinition mixin,
                    final Log log) {
        super(processingInfo, mixin, log);
    }

    @Override
    public MixinDefinition load() throws Exception {
        final var rootPath = StringSubstitutor.replace(StringUtils.substring(mixin.getPath(), 5),
                processingInfo.getParams().getMixinPaths(), "${", "}");

        final var defFilePattern = Pattern.compile(
                "^.*[\\/\\\\]mixin\\." + mixin.getMixinName() + "\\." + mixin.getVersion() + "\\.ya?ml");
        final var textFilePattern = Pattern.compile(
                "^.*[\\/\\\\]texts_(.*)\\." + mixin.getMixinName() + "\\." + mixin.getVersion() + "\\.properties");

        final var pathQueue = new ConcurrentLinkedQueue<Path>();
        pathQueue.add(Path.of(rootPath));
        final var fileQueue = new ConcurrentLinkedQueue<Path>();
        // search given path and all children
        while (!pathQueue.isEmpty()) {
            final var pathTemplate = pathQueue.poll();
            final var pathTemplateName = pathTemplate.toString();
//            log.info("Seaching for mixin files in '" + pathTemplateName + "'...");
            if (pathTemplateName.startsWith("${") && pathTemplateName.endsWith("}")) {
                log.error("Missing value '{" + pathTemplateName.substring(3, pathTemplateName.length() - 2) +
                        "}' in pom.xml in plugin/configuration/mixinPaths. Skipping this path!");
                continue;
            }
            // ok, path template is set, try to read the value
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathTemplate)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        log.debug("    found folder " + path);
                        pathQueue.add(path);
                    } else {
                        final var fName = path.toString();
                        log.debug("    found file '" + fName + "'.");

                        if (defFilePattern.matcher(fName).matches() || textFilePattern.matcher(fName).matches()) {
                            fileQueue.add(path);
                        }
                    }
                }
            }
        }

        // Compute the found files. Text-files are directly merged into the texts properties of the
        // scenario definition. The metadata file is read and all elements are added as replacement of the
        // mixin element.
        MixinDefinition mixinDef = null;
        for (Path path : fileQueue) {
            final var fName = path.toString();
            if (FileUtils.isYamlFile(fName)) {
//                log.debug("Mixing: found metadata file!");
                try (var is = new InputStreamReader(new FileInputStream(fName), StandardCharsets.UTF_8)) {
                    mixinDef = readElementDef(is);
                }
            }
        }
        if (mixinDef == null) {
            log.error("cannot load mixin '" + mixin.getMixinName() + "' from file-system! Could be a follow up issue");
        }

        for (Path path : fileQueue) {
//            log.debug("Mixing: checking path '" + path.toString() + "'.");
            final var fName = path.toString();
            if (FileUtils.isPropertyFile(fName)) {
                final var matcher = textFilePattern.matcher(fName);
                if (!matcher.matches()) {
                    throw new Exception("Irregular properties file match for '" + fName + "'!");
                }
//                log.debug("Mixin: properties does match? " + matcher.matches());
//                log.debug("Mixin: groups for properties file: " + matcher.groupCount() + ", group: " + matcher.group(1));
                final var locale = new Locale(matcher.group(1));
                try (var is = new FileInputStream(fName)) {
                    readTexts(is, locale, mixin.getName(), mixinDef);
                }
            }
        }

        return mixinDef;
    }
}

package com.sap.bfx.maven.cmd.generate;

import com.sap.bfx.maven.cmd.AbstractFormsMojo;
import com.sap.bfx.maven.common.services.CheckAndProcessService;
import com.sap.bfx.maven.common.services.MetadataService;
import com.sap.bfx.maven.common.services.MixinService;
import com.sap.bfx.maven.common.services.TemplateService;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Mojo(name = "scenario-generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Configuration
public class FormsScenarioGeneratorMojo extends AbstractFormsMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    // The metadata folder.
    @Parameter(property = "metadataFolder", defaultValue = "${project.basedir}/src/main/metadata")
    String metadataFolder;

    @Parameter(property = "targetAccessClassFolder", defaultValue = "${project.basedir}/target/generated-sources")
    String tgtAccessClassFolder;

    @Parameter(property = "targetDefinitionsFolder", defaultValue = "${project.basedir}/target/classes")
    String tgtDefinitionsFolder;

    // See <a href="https://maven.apache.org/guides/mini/guide-configuring-plugins.html">Configuration</a>
    @Parameter(property = "mixinPaths")
    Map<String, String> mixinPaths;

    /**
     * Default execution method of the Mojo. It will be called by Maven when the plugin is executed. The method will
     * read the meta-data, do checks and processings, generate access-classes and other files, and write the meta-data
     * into a definition.json file.
     */
    public void execute() throws MojoExecutionException {
        final var appContext = this.createApplicationContext();
        final var log = this.getLog();

        AnsiConsole.systemInstall();
        log.info("Forms Scenario-Generate starting");

        log.info("Metadata Folder: " + metadataFolder);
        log.info("Target AccessClass Folder: " + tgtAccessClassFolder);
        log.info("Target Definitions Folder: " + tgtDefinitionsFolder);
        log.info("Include Paths: " + ((mixinPaths != null) ? mixinPaths.toString() : "{}"));

        try {
            for (var artifcat : project.getDependencyArtifacts()) {
                final var f = ((Artifact) artifcat).getFile();
                log.info("Depenedency: " + f.getName());
            }

            // Use meta-data-servier to read all meta-data
            final var metadataService = appContext.getBean(MetadataService.class);
            metadataService.scanDefinitionMetadata(metadataFolder);

            // Handle mixins
            final var mixinService = appContext.getBean(MixinService.class);
            final var mixinParams =
                    new MixinService.ServiceParameters(project, metadataFolder, tgtAccessClassFolder, mixinPaths);
            mixinService.execute(MixinService.MixinServiceType.ScenarioDefinition, mixinParams);

            // do checks and processings, e.g. generating keys
            final var checkAndProcessService = appContext.getBean(CheckAndProcessService.class);
            final var checkOk = checkAndProcessService.checkAndProcess(project);

            if (checkOk) {
                // create access-classes and other generated files
                final var templateService = appContext.getBean(TemplateService.class);
                templateService.generate(tgtAccessClassFolder);

                // write meta-data (into one large definition.json file for easier reading during runtime
                metadataService.writeMetadataToDefinitionJson(tgtDefinitionsFolder);
            } else {
                throw new MojoFailureException("Found errors in metadata!");
            }
        } catch (Exception e) {
            log.error(e);
            throw new MojoExecutionException(e);
        }

        this.getLog().info("Forms Scenario-Generate finished");
        AnsiConsole.systemUninstall();
    }

    @Bean public Log getMavenLog() {
        return this.getLog();
    }
}
